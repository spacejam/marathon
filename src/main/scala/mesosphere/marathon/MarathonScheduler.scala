package mesosphere.marathon

import javax.inject.{ Inject, Named }

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.EventStream
import mesosphere.marathon.MarathonSchedulerActor.ScaleApp
import mesosphere.marathon.Protos.MarathonTask
import mesosphere.marathon.event._
import mesosphere.marathon.health.HealthCheckManager
import mesosphere.marathon.state.{ AppDefinition, AppRepository, PathId, Timestamp }
import mesosphere.marathon.tasks.TaskQueue.QueuedTask
import mesosphere.marathon.tasks._
import mesosphere.mesos.protos
import mesosphere.util.state.FrameworkIdUtil
import org.apache.log4j.Logger
import org.apache.mesos.Protos._
import org.apache.mesos.{ Scheduler, SchedulerDriver }

import scala.collection.JavaConverters._
import scala.concurrent.{ Await, Future }
import scala.util.control.NonFatal
import scala.util.{ Failure, Success }

trait SchedulerCallbacks {
  def disconnected(): Unit
}

class MarathonScheduler @Inject() (
    @Named(EventModule.busName) eventBus: EventStream,
    offerMatcher: OfferMatcher,
    @Named("schedulerActor") schedulerActor: ActorRef,
    appRepo: AppRepository,
    healthCheckManager: HealthCheckManager,
    taskTracker: TaskTracker,
    taskQueue: TaskQueue,
    frameworkIdUtil: FrameworkIdUtil,
    taskIdUtil: TaskIdUtil,
    system: ActorSystem,
    config: MarathonConf,
    offerReviver: OfferReviver,
    schedulerCallbacks: SchedulerCallbacks) extends Scheduler {

  private[this] val log = Logger.getLogger(getClass.getName)

  import mesosphere.mesos.protos.Implicits._
  import mesosphere.util.ThreadPoolContext.context

  implicit val zkTimeout = config.zkTimeoutDuration

  override def registered(
    driver: SchedulerDriver,
    frameworkId: FrameworkID,
    master: MasterInfo): Unit = {
    log.info(s"Registered as ${frameworkId.getValue} to master '${master.getId}'")
    frameworkIdUtil.store(frameworkId)

    eventBus.publish(SchedulerRegisteredEvent(frameworkId.getValue, master.getHostname))
  }

  override def reregistered(driver: SchedulerDriver, master: MasterInfo): Unit = {
    log.info("Re-registered to %s".format(master))

    eventBus.publish(SchedulerReregisteredEvent(master.getHostname))
  }

  override def resourceOffers(driver: SchedulerDriver, offers: java.util.List[Offer]): Unit = {
    // Check for any tasks which were started but never entered TASK_RUNNING
    // TODO resourceOffers() doesn't feel like the right place to run this
    val toKill = taskTracker.checkStagedTasks

    if (toKill.nonEmpty) {
      log.warn(s"There are ${toKill.size} tasks stuck in staging for more " +
        s"than ${config.taskLaunchTimeout()}ms which will be killed")
      log.info(s"About to kill these tasks: $toKill")
      for (task <- toKill)
        driver.killTask(protos.TaskID(task.getId))
    }

    // remove queued tasks with stale (non-current) app definition versions
    val appVersions: Map[PathId, Timestamp] =
      Await.result(appRepo.currentAppVersions(), config.zkTimeoutDuration)

    taskQueue.retain {
      case QueuedTask(app, _) =>
        appVersions.get(app.id) contains app.version
    }

    offerMatcher.processResourceOffers(driver, offers.asScala)
  }

  override def offerRescinded(driver: SchedulerDriver, offer: OfferID): Unit = {
    log.info("Offer %s rescinded".format(offer))
  }

  //TODO: fix style issue and enable this scalastyle check
  //scalastyle:off cyclomatic.complexity method.length
  override def statusUpdate(driver: SchedulerDriver, status: TaskStatus): Unit = {

    log.info("Received status update for task %s: %s (%s)"
      .format(status.getTaskId.getValue, status.getState, status.getMessage))

    val appId = taskIdUtil.appId(status.getTaskId)

    // forward health changes to the health check manager
    val maybeTask = taskTracker.fetchTask(appId, status.getTaskId.getValue)
    for (marathonTask <- maybeTask)
      healthCheckManager.update(status, Timestamp(marathonTask.getVersion))

    import org.apache.mesos.Protos.TaskState._

    val killedForFailingHealthChecks =
      status.getState == TASK_KILLED && status.hasHealthy && !status.getHealthy

    lazy val maybeApp: Future[Option[AppDefinition]] = appRepo.currentVersion(appId)

    if (status.getState == TASK_ERROR || status.getState == TASK_FAILED || killedForFailingHealthChecks)
      maybeApp.foreach {
        _.foreach(taskQueue.rateLimiter.addDelay)
      }
    status.getState match {
      case TASK_ERROR | TASK_FAILED | TASK_FINISHED | TASK_KILLED | TASK_LOST =>
        if (config.reviveOffersForNewApps()) {
          maybeApp.foreach(_.foreach { (app: AppDefinition) =>
            if (app.constraints.nonEmpty) {
              log.info(s"Reviving offers because a task was killed for an app with a constraint (${app.id}).")
              offerReviver.reviveOffers()
            }
          })
        }

        // Remove from our internal list
        taskTracker.terminated(appId, status).foreach { taskOption =>
          taskOption match {
            case Some(task) => postEvent(status, task)
            case None       => log.warn(s"Couldn't post event for ${status.getTaskId}")
          }

          schedulerActor ! ScaleApp(appId)
        }

      case TASK_RUNNING if !maybeTask.exists(_.hasStartedAt) => // staged, not running
        taskTracker.running(appId, status).onComplete {
          case Success(task) =>
            appRepo.app(appId, Timestamp(task.getVersion)).onSuccess {
              case maybeApp: Option[AppDefinition] => maybeApp.foreach(taskQueue.rateLimiter.resetDelay)
            }
            postEvent(status, task)

          case Failure(t) =>
            log.warn(s"Couldn't post event for ${status.getTaskId}", t)
            log.warn(s"Killing task ${status.getTaskId}")
            driver.killTask(status.getTaskId)
        }

      case TASK_STAGING if !taskTracker.contains(appId) =>
        log.warn(s"Received status update for unknown app $appId")
        log.warn(s"Killing task ${status.getTaskId}")
        driver.killTask(status.getTaskId)

      case _ =>
        taskTracker.statusUpdate(appId, status).onSuccess {
          case None =>
            log.warn(s"Killing task ${status.getTaskId}")
            driver.killTask(status.getTaskId)
        }
    }
  }

  override def frameworkMessage(
    driver: SchedulerDriver,
    executor: ExecutorID,
    slave: SlaveID,
    message: Array[Byte]): Unit = {
    log.info("Received framework message %s %s %s ".format(executor, slave, message))
    eventBus.publish(MesosFrameworkMessageEvent(executor.getValue, slave.getValue, message))
  }

  override def disconnected(driver: SchedulerDriver) {
    log.warn("Disconnected")

    eventBus.publish(SchedulerDisconnectedEvent())

    // Disconnection from the Mesos master has occurred.
    // Thus, call the scheduler callbacks.
    schedulerCallbacks.disconnected()
  }

  override def slaveLost(driver: SchedulerDriver, slave: SlaveID) {
    log.info(s"Lost slave $slave")
  }

  override def executorLost(
    driver: SchedulerDriver,
    executor: ExecutorID,
    slave: SlaveID,
    p4: Int) {
    log.info(s"Lost executor $executor slave $p4")
  }

  override def error(driver: SchedulerDriver, message: String) {
    log.warn("Error: %s".format(message))

    // Currently, it's pretty hard to disambiguate this error from other causes of framework errors.
    // Watch MESOS-2522 which will add a reason field for framework errors to help with this.
    // For now the frameworkId is removed for all messages.
    val removeFrameworkId = true
    suicide(removeFrameworkId)
  }

  /**
    * Exits the JVM process, optionally deleting Marathon's FrameworkID
    * from the backing persistence store.
    *
    * If `removeFrameworkId` is set, the next Marathon process elected
    * leader will fail to find a stored FrameworkID and invoke `register`
    * instead of `reregister`.  This is important because on certain kinds
    * of framework errors (such as exceeding the framework failover timeout),
    * the scheduler may never re-register with the saved FrameworkID until
    * the leading Mesos master process is killed.
    */
  private def suicide(removeFrameworkId: Boolean): Unit = {
    log.fatal(s"Committing suicide!")

    if (removeFrameworkId) Await.ready(frameworkIdUtil.expunge(), config.zkTimeoutDuration)

    // Asynchronously call sys.exit() to avoid deadlock due to the JVM shutdown hooks
    // scalastyle:off magic.number
    Future(sys.exit(9)).onFailure {
      case NonFatal(t) => log.fatal("Exception while committing suicide", t)
    }
    // scalastyle:on
  }

  private def postEvent(status: TaskStatus, task: MarathonTask): Unit = {
    log.info("Sending event notification.")
    eventBus.publish(
      MesosStatusUpdateEvent(
        status.getSlaveId.getValue,
        status.getTaskId.getValue,
        status.getState.name,
        if (status.hasMessage) status.getMessage else "",
        taskIdUtil.appId(status.getTaskId),
        task.getHost,
        task.getPortsList.asScala,
        task.getVersion
      )
    )
  }
}
