package mesosphere.marathon.state

import mesosphere.marathon.MarathonSpec
import mesosphere.marathon.Protos
import org.scalatest.Matchers
import org.apache.mesos.{ Protos => mesos }

import scala.collection.immutable.Seq
import scala.collection.JavaConverters._

class ContainerTest extends MarathonSpec with Matchers {

  class Fixture {
    lazy val volumes = Seq(
      Container.Volume("/etc/a", "/var/data/a", mesos.Volume.Mode.RO),
      Container.Volume("/etc/b", "/var/data/b", mesos.Volume.Mode.RW)
    )

    lazy val container = Container(
      `type` = mesos.ContainerInfo.Type.DOCKER,
      volumes = volumes,
      docker = Some(Container.Docker(image = "group/image"))
    )

    lazy val container2 = Container(
      `type` = mesos.ContainerInfo.Type.DOCKER,
      volumes = Nil,
      docker = Some(
        Container.Docker(
          image = "group/image",
          network = Some(mesos.ContainerInfo.DockerInfo.Network.BRIDGE),
          portMappings = Some(Seq(
            Container.Docker.PortMapping(8080, 32001, 9000, "tcp"),
            Container.Docker.PortMapping(8081, 32002, 9001, "udp")
          ))
        )
      )
    )

    lazy val container3 = Container(
      `type` = mesos.ContainerInfo.Type.DOCKER,
      volumes = Nil,
      docker = Some(
        Container.Docker(
          image = "group/image",
          network = Some(mesos.ContainerInfo.DockerInfo.Network.NONE),
          privileged = true,
          parameters = Seq(
            Parameter("abc", "123"),
            Parameter("def", "456")
          )
        )
      )
    )

    lazy val container4 = Container(
      `type` = mesos.ContainerInfo.Type.DOCKER,
      volumes = Nil,
      docker = Some(
        Container.Docker(
          image = "group/image",
          network = Some(mesos.ContainerInfo.DockerInfo.Network.NONE),
          privileged = true,
          parameters = Seq(
            Parameter("abc", "123"),
            Parameter("def", "456"),
            Parameter("def", "789")
          ),
          forcePullImage = true
        )
      )
    )

  }

  def fixture(): Fixture = new Fixture

  test("ToProto") {
    val f = fixture()
    val proto = f.container.toProto
    assert(mesos.ContainerInfo.Type.DOCKER == proto.getType)
    assert("group/image" == proto.getDocker.getImage)
    assert(f.container.volumes == proto.getVolumesList.asScala.map(Container.Volume(_)))
    assert(proto.getDocker.hasForcePullImage)
    assert(f.container.docker.get.forcePullImage == proto.getDocker.getForcePullImage)

    val proto2: mesosphere.marathon.Protos.ExtendedContainerInfo = f.container2.toProto
    assert(mesos.ContainerInfo.Type.DOCKER == proto2.getType)
    assert("group/image" == proto2.getDocker.getImage)
    assert(f.container2.docker.get.network == Some(proto2.getDocker.getNetwork))
    val portMappings = proto2.getDocker.getPortMappingsList.asScala
    assert(f.container2.docker.get.portMappings == Some(portMappings.map(Container.Docker.PortMapping.apply)))
    assert(proto2.getDocker.hasForcePullImage)
    assert(f.container2.docker.get.forcePullImage == proto2.getDocker.getForcePullImage)

    val proto3 = f.container3.toProto
    assert(mesos.ContainerInfo.Type.DOCKER == proto3.getType)
    assert("group/image" == proto3.getDocker.getImage)
    assert(f.container3.docker.get.network == Some(proto3.getDocker.getNetwork))
    assert(f.container3.docker.get.privileged == proto3.getDocker.getPrivileged)
    assert(f.container3.docker.get.parameters.map(_.key) == proto3.getDocker.getParametersList.asScala.map(_.getKey))
    assert(f.container3.docker.get.parameters.map(_.value) == proto3.getDocker.getParametersList.asScala.map(_.getValue))
    assert(proto3.getDocker.hasForcePullImage)
    assert(f.container3.docker.get.forcePullImage == proto3.getDocker.getForcePullImage)

  }

  test("ToMesos") {
    val f = fixture()
    val proto = f.container.toMesos
    assert(mesos.ContainerInfo.Type.DOCKER == proto.getType)
    assert("group/image" == proto.getDocker.getImage)
    assert(f.container.volumes == proto.getVolumesList.asScala.map(Container.Volume(_)))
    assert(proto.getDocker.hasForcePullImage)
    assert(f.container.docker.get.forcePullImage == proto.getDocker.getForcePullImage)

    val proto2 = f.container2.toMesos
    assert(mesos.ContainerInfo.Type.DOCKER == proto2.getType)
    assert("group/image" == proto2.getDocker.getImage)
    assert(f.container2.docker.get.network == Some(proto2.getDocker.getNetwork))

    val expectedPortMappings = Seq(
      mesos.ContainerInfo.DockerInfo.PortMapping.newBuilder
        .setContainerPort(8080)
        .setHostPort(32001)
        .setProtocol("tcp")
        .build,
      mesos.ContainerInfo.DockerInfo.PortMapping.newBuilder
        .setContainerPort(8081)
        .setHostPort(32002)
        .setProtocol("udp")
        .build
    )

    assert(expectedPortMappings == proto2.getDocker.getPortMappingsList.asScala)
    assert(proto2.getDocker.hasForcePullImage)
    assert(f.container2.docker.get.forcePullImage == proto2.getDocker.getForcePullImage)

    val proto3 = f.container3.toMesos
    assert(mesos.ContainerInfo.Type.DOCKER == proto3.getType)
    assert("group/image" == proto3.getDocker.getImage)
    assert(f.container3.docker.get.network == Some(proto3.getDocker.getNetwork))
    assert(f.container3.docker.get.privileged == proto3.getDocker.getPrivileged)
    assert(f.container3.docker.get.parameters.map(_.key) == proto3.getDocker.getParametersList.asScala.map(_.getKey))
    assert(f.container3.docker.get.parameters.map(_.value) == proto3.getDocker.getParametersList.asScala.map(_.getValue))
    assert(proto3.getDocker.hasForcePullImage)
    assert(f.container3.docker.get.forcePullImage == proto3.getDocker.getForcePullImage)
  }

  test("ConstructFromProto") {
    val f = fixture()

    val containerInfo = Protos.ExtendedContainerInfo.newBuilder
      .setType(mesos.ContainerInfo.Type.DOCKER)
      .addAllVolumes(f.volumes.map(_.toProto).asJava)
      .setDocker(f.container.docker.get.toProto)
      .build

    val container = Container(containerInfo)
    assert(container == f.container)

    val containerInfo2 = Protos.ExtendedContainerInfo.newBuilder
      .setType(mesos.ContainerInfo.Type.DOCKER)
      .setDocker(f.container2.docker.get.toProto)
      .build

    val container2 = Container(containerInfo2)
    assert(container2 == f.container2)

    val containerInfo3 = Protos.ExtendedContainerInfo.newBuilder
      .setType(mesos.ContainerInfo.Type.DOCKER)
      .setDocker(f.container3.docker.get.toProto)
      .build

    val container3 = Container(containerInfo3)
    assert(container3 == f.container3)
  }

  test("SerializationRoundtrip") {
    import com.fasterxml.jackson.databind.ObjectMapper
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
    import mesosphere.jackson.CaseClassModule
    import mesosphere.marathon.api.v2.json.MarathonModule

    val f = fixture()

    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new MarathonModule)
    mapper.registerModule(CaseClassModule)

    val container1 = Container(`type` = mesos.ContainerInfo.Type.DOCKER)
    val json1 = mapper.writeValueAsString(container1)
    val readResult1 = mapper.readValue(json1, classOf[Container])
    assert(readResult1 == container1)

    val json2 = mapper.writeValueAsString(f.container)
    val readResult2 = mapper.readValue(json2, classOf[Container])
    assert(readResult2 == f.container)

    val json3 =
      """
      {
        "type": "DOCKER",
        "docker": {
          "image": "group/image"
        },
        "volumes": [
          {
            "containerPath": "/etc/a",
            "hostPath": "/var/data/a",
            "mode": "RO"
          },
          {
            "containerPath": "/etc/b",
            "hostPath": "/var/data/b",
            "mode": "RW"
          }
        ]
      }
      """

    val readResult3 = mapper.readValue(json3, classOf[Container])
    assert(readResult3 == f.container)

    val json4 =
      """
      {
        "type": "DOCKER",
        "docker": {
          "image": "group/image",
          "network": "BRIDGE",
          "portMappings": [
            { "containerPort": 8080, "hostPort": 32001, "servicePort": 9000, "protocol": "tcp"},
            { "containerPort": 8081, "hostPort": 32002, "servicePort": 9001, "protocol": "udp"}
          ]
        }
      }
      """

    val readResult4 = mapper.readValue(json4, classOf[Container])
    assert(readResult4 == f.container2)

    val json5 = mapper.writeValueAsString(f.container3)
    val readResult5 = mapper.readValue(json5, classOf[Container])
    assert(readResult5 == f.container3)

    val json6 =
      """
      {
        "type": "DOCKER",
        "docker": {
          "image": "group/image",
          "network": "NONE",
          "privileged": true,
          "parameters": [
            { "key": "abc", "value": "123" },
            { "key": "def", "value": "456" }
          ]
        }
      }
      """

    val readResult6 = mapper.readValue(json6, classOf[Container])
    assert(readResult6 == f.container3)

    // Multiple values for a given key.
    val json7 =
      """
      {
        "type": "DOCKER",
        "docker": {
          "image": "group/image",
          "network": "NONE",
          "privileged": true,
          "parameters": [
            { "key": "abc", "value": "123" },
            { "key": "def", "value": "456" },
            { "key": "def", "value": "789" }
          ],
          "forcePullImage": true
        }
      }
      """

    val readResult7 = mapper.readValue(json7, classOf[Container])
    assert(readResult7 == f.container4)

  }

}
