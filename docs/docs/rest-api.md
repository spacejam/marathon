---
title: REST API
---

# Marathon REST API

* [Apps](#apps)
  * [POST /v2/apps](#post-/v2/apps): Create and start a new app
  * [GET /v2/apps](#get-/v2/apps): List all running apps
  * [GET /v2/apps/{appId}](#get-/v2/apps/{appid}): List the app `appId`
  * [GET /v2/apps/{appId}/versions](#get-/v2/apps/{appid}/versions): List the versions of the application with id `appId`.
  * [GET /v2/apps/{appId}/versions/{version}](#get-/v2/apps/{appid}/versions/{version}): List the configuration of the application with id `appId` at version `version`.
  * [PUT /v2/apps/{appId}](#put-/v2/apps/{appid}): Change config of the app
    `appId`
  * [POST /v2/apps/{appId}/restart](#post-/v2/apps/{appid}/restart): Rolling restart of all tasks of the given app
  * [DELETE /v2/apps/{appId}](#delete-/v2/apps/{appid}): Destroy app `appId`
  * [GET /v2/apps/{appId}/tasks](#get-/v2/apps/{appid}/tasks): List running tasks
    for app `appId`
  * [DELETE /v2/apps/{appId}/tasks](#delete-/v2/apps/{appid}/tasks):
    kill tasks belonging to app `appId`
  * [DELETE /v2/apps/{appId}/tasks/{taskId}](#delete-/v2/apps/{appid}/tasks/{taskid}):
    Kill the task `taskId` that belongs to the application `appId`
* [Groups](#groups) <span class="label label-default">v0.7.0</span>
  * [GET /v2/groups](#get-/v2/groups): List all groups
  * [GET /v2/groups/{groupId}](#get-/v2/groups/{groupid}): List the group with the specified ID
  * [POST /v2/groups](#post-/v2/groups): Create and start a new groups
  * [PUT /v2/groups/{groupId}](#put-/v2/groups/{groupid}): Change parameters of a deployed application group
  * [DELETE /v2/groups/{groupId}](#delete-/v2/groups/{groupid}): Destroy a group
* [Tasks](#tasks)
  * [GET /v2/tasks](#get-/v2/tasks): List all running tasks
  * [POST /v2/tasks/delete](#post-/v2/tasks/delete): Kill given list of tasks
* [Deployments](#deployments) <span class="label label-default">v0.7.0</span>
  * [GET /v2/deployments](#get-/v2/deployments): List running deployments
  * [DELETE /v2/deployments/{deploymentId}](#delete-/v2/deployments/{deploymentid}): Revert or cancel the deployment with `deploymentId`
* [Event Stream](#event-stream) <span class="label label-default">v0.9.0</span>
  * [GET /v2/events](#get-/v2/events): Attach to the event stream
* [Event Subscriptions](#event-subscriptions)
  * [POST /v2/eventSubscriptions](#post-/v2/eventsubscriptions): Register a callback URL as an event subscriber
  * [GET /v2/eventSubscriptions](#get-/v2/eventsubscriptions): List all event subscriber callback URLs
  * [DELETE /v2/eventSubscriptions](#delete-/v2/eventsubscriptions) Unregister a callback URL from the event subscribers list
* [Queue](#queue) <span class="label label-default">v0.7.0</span>
  * [GET /v2/queue](#get-/v2/queue): List content of the staging queue.
  * [DELETE /v2/queue/{appId}/delay](#delete-/v2/queue/{appId}/delay): <span class="label label-default">v0.10.0</span> Reset the application specific task launch delay.
* [Server Info](#server-info) <span class="label label-default">v0.7.0</span>
  * [GET /v2/info](#get-/v2/info): Get info about the Marathon Instance
  * [GET /v2/leader](#get-/v2/leader): Get the current leader
  * [DELETE /v2/leader](#delete-/v2/leader): Causes the current leader to abdicate, triggering a new election
* [Miscellaneous](#miscellaneous)
  * [GET /ping](#get-/ping)
  * [GET /logging](#get-/logging)
  * [GET /help](#get-/help)
  * [GET /metrics](#get-/metrics)

### Apps

#### POST `/v2/apps`

Create and start a new application.

The full JSON format of an application resource is as follows:

{% highlight javascript %}
{
    "id": "/product/service/my-app",
    "cmd": "env && sleep 300",
    "args": ["/bin/sh", "-c", "env && sleep 300"],
    "container": {
        "type": "DOCKER",
        "docker": {
            "image": "group/image",
            "network": "BRIDGE",
            "portMappings": [
                {
                    "containerPort": 8080,
                    "hostPort": 0,
                    "servicePort": 9000,
                    "protocol": "tcp"
                },
                {
                    "containerPort": 161,
                    "hostPort": 0,
                    "protocol": "udp"
                }
            ],
            "privileged": false,
            "parameters": [
                { "key": "a-docker-option", "value": "xxx" },
                { "key": "b-docker-option", "value": "yyy" }
            ]
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
    },
    "cpus": 1.5,
    "mem": 256.0,
    "deployments": [
        {
            "id": "5cd987cd-85ae-4e70-8df7-f1438367d9cb"
        }
    ],
    "env": {
        "LD_LIBRARY_PATH": "/usr/local/lib/myLib"
    },
    "executor": "",
    "constraints": [
        ["attribute", "OPERATOR", "value"]
    ],
    "acceptedResourceRoles": [ /* since 0.9.0 */
        "role1", "*"
    ],
    "labels": {
        "environment": "staging"
    },
    "healthChecks": [
        {
            "protocol": "HTTP",
            "path": "/health",
            "gracePeriodSeconds": 3,
            "intervalSeconds": 10,
            "portIndex": 0,
            "timeoutSeconds": 10,
            "maxConsecutiveFailures": 3
        },
        {
            "protocol": "TCP",
            "gracePeriodSeconds": 3,
            "intervalSeconds": 5,
            "portIndex": 1,
            "timeoutSeconds": 5,
            "maxConsecutiveFailures": 3
        },
        {
            "protocol": "COMMAND",
            "command": { "value": "curl -f -X GET http://$HOST:$PORT0/health" },
            "maxConsecutiveFailures": 3
        }
    ],
    "instances": 3,
    "ports": [
        8080,
        9000
    ],
    "backoffSeconds": 1,
    "backoffFactor": 1.15,
    "maxLaunchDelaySeconds": 3600,
    "tasksRunning": 3,
    "tasksHealthy": 3,
    "tasksUnhealthy": 0,
    "tasksStaged": 0,
    "uris": [
        "https://raw.github.com/mesosphere/marathon/master/README.md"
    ],
    "dependencies": ["/product/db/mongo", "/product/db", "../../db"],
    "upgradeStrategy": {
        "minimumHealthCapacity": 0.5,
        "maximumOverCapacity": 0.2
    },
    "version": "2014-03-01T23:29:30.158Z"
}
{% endhighlight %}

##### id

Unique identifier for the app consisting of a series of names separated by slashes.
Each name must be at least 1 character and may
only contain digits (`0-9`), dashes (`-`), dots (`.`), and lowercase letters
(`a-z`). The name may not begin or end with a dash.

The allowable format is represented by the following regular expression 
`^(([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)*([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])$`

##### args

An array of strings that represents an alternative mode of specifying the command to run. This was motivated by safe usage of containerizer features like a custom Docker ENTRYPOINT. This args field may be used in place of cmd even when using the default command executor. This change mirrors API and semantics changes in the Mesos CommandInfo protobuf message starting with version `0.20.0`.  Either `cmd` or `args` must be supplied. It is invalid to supply both `cmd` and `args` in the same app.

##### `backoffSeconds`, `backoffFactor` and `maxLaunchDelaySeconds`

Configures exponential backoff behavior when launching potentially sick apps.
This prevents sandboxes associated with consecutively failing tasks from
filling up the hard disk on Mesos slaves. The backoff period is multiplied by
the factor for each consecutive failure until it reaches maxLaunchDelaySeconds.
This applies also to tasks that are killed due to failing too many health checks.

##### cmd

The command that is executed.  This value is wrapped by Mesos via `/bin/sh -c ${app.cmd}`.  Either `cmd` or `args` must be supplied. It is invalid to supply both `cmd` and `args` in the same app.

##### constraints

Valid constraint operators are one of ["UNIQUE", "CLUSTER",
"GROUP_BY"]. For additional information on using placement constraints see
the [Constraints doc page]({{ site.baseurl }}/docs/constraints.html).

##### acceptedResourceRoles <span class="label label-default">v0.9.0</span>

Optional. A list of resource roles. Marathon considers only resource offers with roles in this list for launching
tasks of this app. If you do not specify this, Marathon considers all resource offers with roles that have been
configured by the `--default_accepted_resource_roles` command line flag. If no `--default_accepted_resource_roles` was
given on startup, Marathon considers all resource offers.

Example 1: `"acceptedResourceRoles": [ "production", "*" ]` Tasks of this app definition are launched either
on "production" or "*" resources.

Example 2: `"acceptedResourceRoles": [ "public" ]` Tasks of this app definition are launched only on "public"
resources.

Background: Mesos can assign roles to certain resource shares. Frameworks which are not explicitly registered for
a role do not see resources of that role. In this way, you can reserve resources for frameworks. Resources not reserved
for custom role, are available for all frameworks. Mesos assigns the special role "*" to them.

To register Marathon for a role, you need to specify the `--mesos_role` command line flag on startup.
If you want to assign all resources of a
slave to a role, you can use the `--default_role` argument when starting up the slave. If you need a more
fine-grained configuration, you can use the `--resources' argument to specify resource shares per role. The Mesos master
needs to be started with `--roles` followed by a comma-separated list of all roles you want to use across your cluster.
See
[the Mesos command line documentation](http://mesos.apache.org/documentation/latest/configuration/) for details.

##### labels

Attaching metadata to apps can be useful to expose additional information
to other services, so we added the ability to place labels on apps
(for example, you could label apps "staging" and "production" to mark
services by their position in the pipeline).

##### container

Additional data passed to the containerizer on application launch.  These
consist of a type, zero or more volumes, and additional type-specific options.
Volumes and type are optional (the default type is DOCKER).  In order to make
use of the docker containerizer, specify `--containerizers=docker,mesos` to
the Mesos slave.  For a discussion of docker-specific options, see the
[native docker document]({{site.baseurl}}/docs/native-docker.html).

##### dependencies

A list of services upon which this application depends. An order is derived from the dependencies for performing start/stop and upgrade of the application.  For example, an application `/a` relies on the services `/b` which itself relies on `/c`. To start all 3 applications, first `/c` is started than `/b` than `/a`.

##### deployments (read-only)

A list of currently running deployments that affect this application.
If this array is nonempty, then this app is locked for updates.

##### healthChecks

An array of checks to be performed on running tasks to determine if they are
operating as expected. Health checks begin immediately upon task launch. For
design details, refer to the [health checks]({{ site.baseurl}}/docs/health-checks.html)
doc.  By default, health checks are executed by the Marathon scheduler.
With Mesos `0.20.0` and higher, the COMMAND protocol can be used to
execute health checks on the hosts where the tasks are running.
Mesos currently supports only one such health check per task.
A COMMAND health check is considered passing if the command exits with status
`0` within the `timeoutSeconds` period.
COMMAND and other health checks can be defined on the same app as of
Marathon `0.7.4`.

An HTTP health check is considered passing if (1) its HTTP response code is between
200 and 399, inclusive, and (2) its response is received within the
`timeoutSeconds` period.

If a task fails more than `maxConsecutiveFailures`
health checks consecutively, that task is killed causing Marathon to start
more instances. These restarts are modulated like any other failing app
by `backoffSeconds`, `backoffFactor` and `maxLaunchDelaySeconds`.

###### HEALTH CHECK OPTIONS

* `command`: Command to run in order to determine the health of a task.
  _Note: only used if `protocol == "COMMAND"`._
* `gracePeriodSeconds` (Optional. Default: 15): Health check failures are
  ignored within this number of seconds of the task being started or until the
  task becomes healthy for the first time.
* `intervalSeconds` (Optional. Default: 10): Number of seconds to wait between
  health checks.
* `maxConsecutiveFailures`(Optional. Default: 3) : Number of consecutive health
  check failures after which the unhealthy task should be killed.
* `protocol` (Optional. Default: "HTTP"): Protocol of the requests to be
  performed. One of "HTTP", "TCP", or "COMMAND".
* `path` (Optional. Default: "/"): Path to endpoint exposed by the task that
  will provide health  status. Example: "/path/to/health".
  _Note: only used if `protocol == "HTTP"`._
* `portIndex` (Optional. Default: 0): Index in this app's `ports` array to be
  used for health requests. An index is used so the app can use random ports,
  like "[0, 0, 0]" for example, and tasks could be started with port environment
  variables like `$PORT1`.
* `timeoutSeconds` (Optional. Default: 20): Number of seconds after which a
  health check is considered a failure regardless of the response.

##### ports

An array of required port resources on the host.

The port array currently serves multiple roles:

* The number of items in the array determines how many dynamic ports are allocated
  for every task.
* For every port that is zero, a globally unique (cluster-wide) port is assigned and
  provided as part of the app definition to be used in load balancing definitions.
  See [Service Discovery Load Balancing doc page]({{ site.baseurl }}/docs/service-discovery-load-balancing.html)
  for details.

Since this is confusing, we recommend to configure ports assignment for docker
containers in `container.docker.portMappings` instead, see
[Docker Containers doc page]({{ site.baseurl }}/docs/native-docker.html#bridged-networking-mode)).

Alternatively or if you use the Mesos Containerizer, pass zeros as port values to generate one or more arbitrary
free ports for each application instance.
Each port value is exposed to the instance via environment variables
`$PORT0`, `$PORT1`, etc. Ports assigned to running instances are also available
via the task resource.

We will probably provide an alternative way to configure this for non-docker apps in the future
as well, see [Rethink ports API](https://github.com/mesosphere/marathon/issues/670).

##### requirePorts

Normally, the host ports of your tasks are automatically assigned. This corresponds to the
`requirePorts` value `false` which is the default.

If you need more control and want to specify your host ports in advance, you can
 set `requirePorts` to `true`. This way the ports you have specified are used as host ports. That also
 means that Marathon can schedule the associated tasks only on hosts that have the specified ports available.

 The specified ports need to be in the local port range specified by the
 `--local_port_min` and `--local_port_max` flags. See
 [Command Line Flags doc page]({{ site.baseurl }}/docs/command-line-flags.html)).

##### upgradeStrategy

During an upgrade all instances of an application get replaced by a new version.
The upgradeStrategy controls how Marathon stops old versions and launches
new versions. It consists of two values:

* `minimumHealthCapacity` (Optional. Default: 1.0) - a number between `0`and `1`
that is multiplied with the instance count. This is the minimum number of healthy
nodes that do not sacrifice overall application purpose. Marathon will make sure,
during the upgrade process, that at any point of time this number of healthy
instances are up.
* `maximumOverCapacity` (Optional. Default: 1.0) - a number between `0` and
`1` which is multiplied with the instance count. This is the maximum number of
additional instances launched at any point of time during the upgrade process.

The default `minimumHealthCapacity` is `1`, which means no old instance can be
stopped before another healthy new version is deployed.
A value of `0.5` means that during an upgrade half of the old version instances
are stopped first to make space for the new version.
A value of `0` means take all instances down immediately and replace with the
new application.

The default `maximumOverCapacity` is `1`, which means that all old and new
instances can co-exist during the upgrade process.
A value of `0.1` means that during the upgrade process 10% more capacity than
usual may be used for old and new instances.
A value of `0.0` means that even during the upgrade process no more capacity may
be used for the new instances than usual. Only when an old version is stopped,
a new instance can be deployed.

If `minimumHealthCapacity` is `1` and `maximumOverCapacity` is `0`, at least
one additional new instance is launched in the beginning of the upgrade process.
When it is healthy, one of the old instances is stopped. After it is stopped,
another new instance is started, and so on.

A combination of `minimumHealthCapacity` equal to `0.9` and
`maximumOverCapacity` equal to `0` results in a rolling update, replacing
10% of the instances at a time, keeping at least 90% of the app online at any
point of time during the upgrade.

A combination of `minimumHealthCapacity` equal to `1.0` and
`maximumOverCapacity` equal to `0.1` results in a rolling update, replacing
10% of the instances at a time and keeping at least 100% of the app online at
any point of time during the upgrade with 10% of additional capacity.

##### Example

**Request:**


{% highlight http %}
POST /v2/apps HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate
Content-Length: 562
Content-Type: application/json; charset=utf-8
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0

{
    "cmd": "env && python3 -m http.server $PORT0",
    "constraints": [
        [
            "hostname",
            "UNIQUE"
        ]
    ],
    "container": {
        "docker": {
            "image": "python:3"
        },
        "type": "DOCKER"
    },
    "cpus": 0.25,
    "healthChecks": [
        {
            "gracePeriodSeconds": 3,
            "intervalSeconds": 10,
            "maxConsecutiveFailures": 3,
            "path": "/",
            "portIndex": 0,
            "protocol": "HTTP",
            "timeoutSeconds": 5
        }
    ],
    "id": "my-app",
    "instances": 2,
    "mem": 50,
    "ports": [
        0
    ],
    "upgradeStrategy": {
        "minimumHealthCapacity": 0.5,
        "maximumOverCapacity": 0.5
    }
}
{% endhighlight json %}

**Response:**


{% highlight http %}
HTTP/1.1 201 Created
Content-Type: application/json
Location: http://mesos.vm:8080/v2/apps/my-app
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "args": null,
    "backoffFactor": 1.15,
    "backoffSeconds": 1,
    "maxLaunchDelaySeconds": 3600,
    "cmd": "env && python3 -m http.server $PORT0",
    "constraints": [
        [
            "hostname",
            "UNIQUE"
        ]
    ],
    "container": {
        "docker": {
            "image": "python:3"
        },
        "type": "DOCKER",
        "volumes": []
    },
    "cpus": 0.25,
    "dependencies": [],
    "deployments": [
        {
            "id": "f44fd4fc-4330-4600-a68b-99c7bd33014a"
        }
    ],
    "disk": 0.0,
    "env": {},
    "executor": "",
    "healthChecks": [
        {
            "command": null,
            "gracePeriodSeconds": 3,
            "intervalSeconds": 10,
            "maxConsecutiveFailures": 3,
            "path": "/",
            "portIndex": 0,
            "protocol": "HTTP",
            "timeoutSeconds": 5
        }
    ],
    "id": "/my-app",
    "instances": 2,
    "mem": 50.0,
    "ports": [
        0
    ],
    "requirePorts": false,
    "storeUrls": [],
    "upgradeStrategy": {
        "minimumHealthCapacity": 0.5,
        "maximumOverCapacity": 0.5
    },
    "uris": [],
    "user": null,
    "version": "2014-08-18T22:36:41.451Z"
}
{% endhighlight %}

#### GET `/v2/apps`

List all running applications.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>cmd</code></td>
      <td><code>string</code></td>
      <td>
        Filter apps to only those whose commands contain <code>cmd</code>.
        Default: <code>""</code>.
      </td>
    </tr>
    <tr>
      <td><code>embed</code></td>
      <td><code>string</code></td>
      <td>
        Embeds nested resources that match the supplied path.
        Default: <code>none</code>. Possible values:
        <ul>
          <li>
            <code>"apps.tasks"</code>. Apps' tasks are not embedded in the response
            by default.
          </li>
          <li>
            <code>"apps.failures"</code>. Apps' last failures are not embedded in
            the response by default.
          </li>
        </ul>
      </td>
    </tr>
  </tbody>
</table>

##### Example

**Request:**

{% highlight http %}
GET /v2/apps/ HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{%endhighlight%}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "apps": [
        {
            "args": null,
            "backoffFactor": 1.15,
            "backoffSeconds": 1,
            "maxLaunchDelaySeconds": 3600,
            "cmd": "python3 -m http.server 8080",
            "constraints": [],
            "container": {
                "docker": {
                    "image": "python:3",
                    "network": "BRIDGE",
                    "portMappings": [
                        {
                            "containerPort": 8080,
                            "hostPort": 0,
                            "servicePort": 9000,
                            "protocol": "tcp"
                        },
                        {
                            "containerPort": 161,
                            "hostPort": 0,
                            "protocol": "udp"
                        }
                    ]
                },
                "type": "DOCKER",
                "volumes": []
            },
            "cpus": 0.5,
            "dependencies": [],
            "deployments": [],
            "disk": 0.0,
            "env": {},
            "executor": "",
            "healthChecks": [
                {
                    "command": null,
                    "gracePeriodSeconds": 5,
                    "intervalSeconds": 20,
                    "maxConsecutiveFailures": 3,
                    "path": "/",
                    "portIndex": 0,
                    "protocol": "HTTP",
                    "timeoutSeconds": 20
                }
            ],
            "id": "/bridged-webapp",
            "instances": 2,
            "mem": 64.0,
            "ports": [
                10000,
                10001
            ],
            "requirePorts": false,
            "storeUrls": [],
            "tasksRunning": 2,
            "tasksHealthy": 2,
            "tasksUnhealthy": 0,
            "tasksStaged": 0,
            "upgradeStrategy": {
                "minimumHealthCapacity": 1.0
            },
            "uris": [],
            "user": null,
            "version": "2014-09-25T02:26:59.256Z"
        }
    ]
}
{%endhighlight%}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "apps": [
        {
            "args": null,
            "backoffFactor": 1.15,
            "backoffSeconds": 1,
            "maxLaunchDelaySeconds": 3600,
            "cmd": "while sleep 10; do date -u +%T; done",
            "constraints": [],
            "container": {
                "docker": {
                    "image": "libmesos/ubun2",
                    "network": null,
                    "portMappings": null
                },
                "type": "DOCKER",
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
            },
            "cpus": 0.5,
            "dependencies": [],
            "deployments": [
                {
                    "id": "f945548f-dd80-49a2-8b1a-d07ad9245688"
                }
            ],
            "disk": 0.0,
            "env": {},
            "executor": "",
            "healthChecks": [],
            "id": "/ubuntu",
            "instances": 1,
            "lastTaskFailure": {
                "appId": "/ubuntu",
                "host": "10.141.141.10",
                "message": "Abnormal executor termination",
                "state": "TASK_FAILED",
                "taskId": "ubuntu.4b20f4a9-503f-11e4-bd18-56847afe9799",
                "timestamp": "2014-10-10T05:35:52.025Z",
                "version": "2014-10-09T23:03:33.993Z"
            },
            "mem": 512.0,
            "ports": [10000],
            "requirePorts": false,
            "storeUrls": [],
            "tasks": [
                {
                    "appId": "/ubuntu",
                    "host": "10.141.141.10",
                    "id": "ubuntu.55de96ed-503f-11e4-bd18-56847afe9799",
                    "ports": [31467],
                    "stagedAt": "2014-10-10T05:36:08.178Z",
                    "startedAt": null,
                    "version": "2014-10-09T23:03:33.993Z"
                }
            ],
            "tasksRunning": 0,
            "tasksHealthy": 0,
            "tasksUnhealthy": 0,
            "tasksStaged": 1,
            "upgradeStrategy": {
                "minimumHealthCapacity": 1.0
            },
            "uris": [],
            "user": null,
            "version": "2014-10-09T23:03:33.993Z"
        }
    ]
}
{%endhighlight%}

#### GET `/v2/apps/{appId}`

List the application with id `appId`.

##### Example

**Request:**

{% highlight http %}
GET /v2/apps/toggle?embed=apps.tasks HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "app": {
        "args": null,
        "backoffFactor": 1.15,
        "backoffSeconds": 1,
        "maxLaunchDelaySeconds": 3600,
        "cmd": "python toggle.py $PORT0",
        "constraints": [],
        "container": null,
        "cpus": 0.2,
        "dependencies": [],
        "deployments": [],
        "disk": 0.0,
        "env": {},
        "executor": "",
        "healthChecks": [
            {
                "command": null,
                "gracePeriodSeconds": 5,
                "intervalSeconds": 10,
                "maxConsecutiveFailures": 3,
                "path": "/health",
                "portIndex": 0,
                "protocol": "HTTP",
                "timeoutSeconds": 10
            }
        ],
        "id": "/toggle",
        "instances": 2,
        "lastTaskFailure": {
            "appId": "/toggle",
            "host": "10.141.141.10",
            "message": "Abnormal executor termination",
            "state": "TASK_FAILED",
            "taskId": "toggle.cc427e60-5046-11e4-9e34-56847afe9799",
            "timestamp": "2014-09-12T23:23:41.711Z",
            "version": "2014-09-12T23:28:21.737Z"
        },
        "mem": 32.0,
        "ports": [
            10000
        ],
        "requirePorts": false,
        "storeUrls": [],
        "tasks": [
            {
                "appId": "/toggle",
                "healthCheckResults": [
                    {
                        "alive": true,
                        "consecutiveFailures": 0,
                        "firstSuccess": "2014-09-13T00:20:28.101Z",
                        "lastFailure": null,
                        "lastSuccess": "2014-09-13T00:25:07.506Z",
                        "taskId": "toggle.802df2ae-3ad4-11e4-a400-56847afe9799"
                    }
                ],
                "host": "10.141.141.10",
                "id": "toggle.802df2ae-3ad4-11e4-a400-56847afe9799",
                "ports": [
                    31045
                ],
                "stagedAt": "2014-09-12T23:28:28.594Z",
                "startedAt": "2014-09-13T00:24:46.959Z",
                "version": "2014-09-12T23:28:21.737Z"
            },
            {
                "appId": "/toggle",
                "healthCheckResults": [
                    {
                        "alive": true,
                        "consecutiveFailures": 0,
                        "firstSuccess": "2014-09-13T00:20:28.101Z",
                        "lastFailure": null,
                        "lastSuccess": "2014-09-13T00:25:07.508Z",
                        "taskId": "toggle.7c99814d-3ad4-11e4-a400-56847afe9799"
                    }
                ],
                "host": "10.141.141.10",
                "id": "toggle.7c99814d-3ad4-11e4-a400-56847afe9799",
                "ports": [
                    31234
                ],
                "stagedAt": "2014-09-12T23:28:22.587Z",
                "startedAt": "2014-09-13T00:24:46.965Z",
                "version": "2014-09-12T23:28:21.737Z"
            }
        ],
        "tasksRunning": 2,
        "tasksHealthy": 2,
        "tasksUnhealthy": 0,
        "tasksStaged": 0,
        "upgradeStrategy": {
            "minimumHealthCapacity": 1.0
        },
        "uris": [
            "http://downloads.mesosphere.com/misc/toggle.tgz"
        ],
        "user": null,
        "version": "2014-09-12T23:28:21.737Z"
    }
}
{% endhighlight %}

#### GET `/v2/apps/{appId}/versions`

List the versions of the application with id `appId`.

##### Example

**Request:**

{% highlight http %}
GET /v2/apps/my-app/versions HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "versions": [
        "2014-04-04T06:25:31.399Z"
    ]
}
{% endhighlight %}

#### GET `/v2/apps/{appId}/versions/{version}`

List the configuration of the application with id `appId` at version `version`.

##### Example

**Request:**

{% highlight http %}
GET /v2/apps/my-app/versions/2014-03-01T23:17:50.295Z HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate, compress
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "cmd": "sleep 60",
    "constraints": [],
    "container": null,
    "cpus": 0.1,
    "env": {},
    "executor": "",
    "id": "my-app",
    "instances": 4,
    "mem": 5.0,
    "ports": [
        18027,
        13200
    ],
    "uris": [
        "https://raw.github.com/mesosphere/marathon/master/README.md"
    ],
    "version": "2014-03-01T23:17:50.295Z"
}
{% endhighlight %}

#### PUT `/v2/apps/{appId}`

Change parameters of a running application.  The new application parameters
apply only to subsequently created tasks.  Currently running tasks are
restarted, while maintaining the `minimumHealthCapacity`

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>force</code></td>
      <td><code>boolean</code></td>
      <td>If the app is affected by a running deployment, then the update
        operation will fail. The current deployment can be overridden by setting
        the `force` query parameter.
        Default: <code>false</code>.</td>
    </tr>
  </tbody>
</table>

##### Example

**Request:**

{% highlight http %}
PUT /v2/apps/my-app HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 126
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2

{
    "cmd": "sleep 55",
    "constraints": [
        [
            "hostname",
            "UNIQUE",
            ""
        ]
    ],
    "cpus": "0.3",
    "instances": "2",
    "mem": "9",
    "ports": [
        9000
    ]
}
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "83b215a6-4e26-4e44-9333-5c385eda6438",
    "version": "2014-08-26T07:37:50.462Z"
}
{% endhighlight %}

##### Example (version rollback)

If the `version` key is supplied in the JSON body, the rest of the object is ignored.  If the supplied version is known, then the app is updated (a new version is created) with those parameters.  Otherwise, if the supplied version is not known Marathon responds with a 404.

**Request:**

{% highlight http %}
PUT /v2/apps/my-app HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 39
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2

{
    "version": "2014-03-01T23:17:50.295Z"
}
{% endhighlight %}

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "83b215a6-4e26-4e44-9333-5c385eda6438",
    "version": "2014-08-26T07:37:50.462Z"
}
{% endhighlight %}

##### Example (update an app that is locked by a running deployment)

If the app is affected by a currently running deployment, then the
update operation fails.  As indicated by the response message, the current
deployment can be overridden by setting the `force` query parameter in a
subsequent request.

**Request:**

{% highlight http %}
PUT /v2/apps/test HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate
Content-Length: 18
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.8.0

{
    "instances": "2"
}
{% endhighlight %}

{% highlight http %}
HTTP/1.1 409 Conflict
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deployments": [
        {
            "id": "5cd987cd-85ae-4e70-8df7-f1438367d9cb"
        }
    ],
    "message": "App is locked by one or more deployments. Override with the option '?force=true'. View details at '/v2/deployments/<DEPLOYMENT_ID>'."
}
{% endhighlight %}

#### POST `/v2/apps/{appId}/restart`

Initiates a rolling restart of all running tasks of the given app. This call respects the configured `minimumHealthCapacity`.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>force</code></td>
      <td><code>boolean</code></td>
      <td>If the app is affected by a running deployment, then the update
        operation will fail. The current deployment can be overridden by setting
        the `force` query parameter.
        Default: <code>false</code>.</td>
    </tr>
  </tbody>
</table>

##### Example

**Request:**

{% highlight http %}
POST /v2/apps/my-app/restart HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Host: localhost:8080
User-Agent: HTTPie/0.7.2

{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "83b215a6-4e26-4e44-9333-5c385eda6438",
    "version": "2014-08-26T07:37:50.462Z"
}
{% endhighlight %}

#### DELETE `/v2/apps/{appId}`

Destroy an application. All data about that application will be deleted.

##### Example

**Request:**

{% highlight http %}
DELETE /v2/apps/my-app HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}


**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, must-revalidate
Content-Type: application/json
Expires: 0
Pragma: no-cache
Server: Jetty(8.1.15.v20140411)
Transfer-Encoding: chunked

{
    "deploymentId": "14f48a7d-261e-4641-a158-8c5894c3116a",
    "version": "2015-04-21T10:34:13.646Z"
}
{% endhighlight %}


#### GET `/v2/apps/{appId}/tasks`

List all running tasks for application `appId`.

##### Example (as JSON)

**Request:**

{% highlight http %}
GET /v2/apps/my-app/tasks HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "tasks": [
        {
            "host": "agouti.local",
            "id": "my-app_1-1396592790353",
            "ports": [
                31336,
                31337
            ],
            "stagedAt": "2014-04-04T06:26:30.355Z",
            "startedAt": "2014-04-04T06:26:30.860Z",
            "version": "2014-04-04T06:26:23.051Z"
        },
        {
            "host": "agouti.local",
            "id": "my-app_0-1396592784349",
            "ports": [
                31382,
                31383
            ],
            "stagedAt": "2014-04-04T06:26:24.351Z",
            "startedAt": "2014-04-04T06:26:24.919Z",
            "version": "2014-04-04T06:26:23.051Z"
        }
    ]
}
{% endhighlight %}

##### Example (as text)

**Request:**

{% highlight http %}
GET /v2/apps/my-app/tasks HTTP/1.1
Accept: text/plain
Accept-Encoding: gzip, deflate, compress
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: text/plain
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

my-app  19385 agouti.local:31336  agouti.local:31364  agouti.local:31382 
my-app  11186 agouti.local:31337  agouti.local:31365  agouti.local:31383 
{% endhighlight %}

#### DELETE `/v2/apps/{appId}/tasks`

Kill tasks that belong to the application `appId`.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>host</code></td>
      <td><code>string</code></td>
      <td>Kill only those tasks running on host <code>host</code>.
        Default: <code>none</code>.</td>
    </tr>
    <tr>
      <td><code>scale</code></td>
      <td><code>boolean</code></td>
      <td>Scale the app down (i.e. decrement its <code>instances</code> setting
        by the number of tasks killed) after killing the specified tasks.
        Default: <code>false</code>.</td>
    </tr>
  </tbody>
</table>

##### Example

**Request:**

{% highlight http %}
DELETE /v2/apps/my-app/tasks?host=mesos.vm&scale=false HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "tasks": []
}
{% endhighlight %}

#### DELETE `/v2/apps/{appId}/tasks/{taskId}`

Kill the task with ID `taskId` that belongs to the application `appId`.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>scale</code></td>
      <td><code>boolean</code></td>
      <td>Scale the app down (i.e. decrement its <code>instances</code> setting
        by the number of tasks killed) after killing the specified task.
        Default: <code>false</code>.</td>
    </tr>
  </tbody>
</table>

##### Example

**Request:**

{% highlight http %}
DELETE /v2/apps/my-app/tasks/my-app_3-1389916890411 HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "task": {
        "host": "mesos.vm",
        "id": "my-app_3-1389916890411",
        "ports": [
            31509,
            31510
        ],
        "stagedAt": "2014-01-17T00:01+0000",
        "startedAt": "2014-01-17T00:01+0000"
    }
}
{% endhighlight %}

### Groups

#### GET `/v2/groups`

List all groups.

**Request:**

{% highlight http %}
GET /v2/groups HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "apps": [],
    "dependencies": [],
    "groups": [
        {
            "apps": [
                {
                    "args": null,
                    "backoffFactor": 1.15,
                    "backoffSeconds": 1,
                    "maxLaunchDelaySeconds": 3600,
                    "cmd": "sleep 30",
                    "constraints": [],
                    "container": null,
                    "cpus": 1.0,
                    "dependencies": [],
                    "disk": 0.0,
                    "env": {},
                    "executor": "",
                    "healthChecks": [],
                    "id": "/test/app",
                    "instances": 1,
                    "mem": 128.0,
                    "ports": [
                        10000
                    ],
                    "requirePorts": false,
                    "storeUrls": [],
                    "upgradeStrategy": {
                        "minimumHealthCapacity": 1.0
                    },
                    "uris": [],
                    "user": null,
                    "version": "2014-08-28T01:05:40.586Z"
                }
            ],
            "dependencies": [],
            "groups": [],
            "id": "/test",
            "version": "2014-08-28T01:09:46.212Z"
        }
    ],
    "id": "/",
    "version": "2014-08-28T01:09:46.212Z"
}
{% endhighlight %}

#### GET `/v2/groups/{groupId}`

List the group with the specified ID.

**Request:**

{% highlight http %}
GET /v2/groups/test HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "apps": [
        {
            "args": null,
            "backoffFactor": 1.15,
            "backoffSeconds": 1,
            "maxLaunchDelaySeconds": 3600,
            "cmd": "sleep 30",
            "constraints": [],
            "container": null,
            "cpus": 1.0,
            "dependencies": [],
            "disk": 0.0,
            "env": {},
            "executor": "",
            "healthChecks": [],
            "id": "/test/app",
            "instances": 1,
            "mem": 128.0,
            "ports": [
                10000
            ],
            "requirePorts": false,
            "storeUrls": [],
            "upgradeStrategy": {
                "minimumHealthCapacity": 1.0
            },
            "uris": [],
            "user": null,
            "version": "2014-08-28T01:05:40.586Z"
        }
    ],
    "dependencies": [],
    "groups": [],
    "id": "/test",
    "version": "2014-08-28T01:09:46.212Z"
}
{% endhighlight %}

#### POST `/v2/groups`

Create and start a new application group.
Application groups can contain other application groups.
An application group can either hold other groups or applications, but can not be mixed in one.

The JSON format of a group resource is as follows:

```json
{
  "id": "product",
  "groups": [{
    "id": "service",
    "groups": [{
      "id": "us-east",
      "apps": [{
        "id": "app1",
        "cmd": "someExecutable"
      }, 
      {
        "id": "app2",
        "cmd": "someOtherExecutable"
      }]
    }],
    "dependencies": ["/product/database", "../backend"]
  }
],
"version": "2014-03-01T23:29:30.158Z"
}
```

Since the deployment of the group can take a considerable amount of time, this endpoint returns immediatly with a version.
The failure or success of the action is signalled via event. There is a
`group_change_success` and `group_change_failed` event with the given version.

### Example

**Request:**

{% highlight http %}
POST /v2/groups HTTP/1.1
User-Agent: curl/7.35.0
Accept: application/json
Host: localhost:8080
Content-Type: application/json
Content-Length: 273

{
  "id" : "product",
  "apps":[
    {
      "id": "myapp",
      "cmd": "ruby app2.rb",
      "instances": 1
    }
  ]
}
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 201 Created
Location: http://localhost:8080/v2/groups/product
Content-Type: application/json
Transfer-Encoding: chunked
Server: Jetty(8.y.z-SNAPSHOT)
{"version":"2014-07-01T10:20:50.196Z"}
{% endhighlight %}

Create and start a new application group.
Application groups can contain other application groups.
An application group can either hold other groups or applications, but can not be mixed in one.

The JSON format of a group resource is as follows:

```json
{
  "id": "product",
  "groups": [{
    "id": "service",
    "groups": [{
      "id": "us-east",
      "apps": [{
        "id": "app1",
        "cmd": "someExecutable"
      }, 
      {
        "id": "app2",
        "cmd": "someOtherExecutable"
      }]
    }],
    "dependencies": ["/product/database", "../backend"]
  }
],
"version": "2014-03-01T23:29:30.158Z"
}
```

Since the deployment of the group can take a considerable amount of time, this endpoint returns immediatly with a version.
The failure or success of the action is signalled via event. There is a
`group_change_success` and `group_change_failed` event with the given version.


### Example

**Request:**

{% highlight http %}
POST /v2/groups HTTP/1.1
User-Agent: curl/7.35.0
Accept: application/json
Host: localhost:8080
Content-Type: application/json
Content-Length: 273

{
  "id" : "product",
  "apps":[
    {
      "id": "myapp",
      "cmd": "ruby app2.rb",
      "instances": 1
    }
  ]
}
{% endhighlight %}

**Response:**


{% highlight http %}
HTTP/1.1 201 Created
Location: http://localhost:8080/v2/groups/product
Content-Type: application/json
Transfer-Encoding: chunked
Server: Jetty(8.y.z-SNAPSHOT)
{"version":"2014-07-01T10:20:50.196Z"}
{% endhighlight %}

#### PUT `/v2/groups/{groupId}`

Change parameters of a deployed application group.

* Changes to application parameters will result in a restart of this application.
* A new application added to the group is started.
* An existing application removed from the group gets stopped.

If there are no changes to the application definition, no restart is triggered.
During restart marathon keeps track, that the configured amount of minimal running instances are _always_ available.

A deployment can run forever. This is the case, when the new application has a problem and does not become healthy.
In this case, human interaction is needed with 2 possible choices:

* Rollback to an existing older version (send an existing `version` in the body)
* Update with a newer version of the group which does not have the problems of the old one.

If there is an upgrade process already in progress, a new update will be rejected unless the force flag is set.
With the force flag given, a running upgrade is terminated and a new one is started.

Since the deployment of the group can take a considerable amount of time, this endpoint returns immediatly with a version.
The failure or success of the action is signalled via event. There is a
`group_change_success` and `group_change_failed` event with the given version.

### Example

**Request:**

{% highlight http %}
PUT /v2/groups/test/project HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate
Content-Length: 541
Content-Type: application/json; charset=utf-8
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0

{
    "apps": [
        {
            "cmd": "ruby app2.rb",
            "constraints": [],
            "container": null,
            "cpus": 0.2,
            "env": {},
            "executor": "//cmd",
            "healthChecks": [
                {
                    "initialDelaySeconds": 15,
                    "intervalSeconds": 5,
                    "path": "/health",
                    "portIndex": 0,
                    "protocol": "HTTP",
                    "timeoutSeconds": 15
                }
            ],
            "id": "app",
            "instances": 6,
            "mem": 128.0,
            "ports": [
                19970
            ],
            "uris": []
        }
    ]
}
{% endhighlight %}

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "c0e7434c-df47-4d23-99f1-78bd78662231",
    "version": "2014-08-28T16:45:41.063Z"
}
{% endhighlight %}

### Example

Scale a group.

The scaling affects apps directly in the group as well as all transitive applications referenced by subgroups of this group.
The scaling factor is applied to each individual instance count of each application.

Since the deployment of the group can take a considerable amount of time, this endpoint returns immediatly with a version.
The failure or success of the action is signalled via event. There is a
`group_change_success` and `group_change_failed` event with the given version.

**Request:**

{% highlight http %}
PUT /v2/groups/product/service HTTP/1.1
Content-Length: 123
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{ "scaleBy": 1.5 }
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "c0e7434c-df47-4d23-99f1-78bd78662231",
    "version": "2014-08-28T16:45:41.063Z"
}
{% endhighlight %}

### Example

Rollback a group.

In case of an erroneous update, a group can be rolled back by sending just a version, that is known to work, to the update
endpoint.

**Request:**

{% highlight http %}
PUT /v2/groups/product/service HTTP/1.1
Content-Length: 123
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{ "version": "2014-08-27T15:34:48.163Z" }
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "c0e7434c-df47-4d23-99f1-78bd78662231",
    "version": "2014-08-28T16:45:41.063Z"
}
{% endhighlight %}

### Example

Deployment dry run.

Get a preview of the deployment steps Marathon would run for a given group update.

**Request:**

{% highlight http %}
PUT /v2/groups/product?dryRun=true HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Content-Type: application/json
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0

{
    "id": "product",
    "groups": [{
        "id": "service",
        "groups": [{
            "id": "us-east",
            "apps": [
                {
                    "id": "app1",
                    "cmd": "someExecutable"
                },
                {
                    "id": "app2",
                    "cmd": "someOtherExecutable"
                }
            ]
        }],
        "dependencies": ["/product/database", "../backend"]
    }],
    "version": "2014-03-01T23:29:30.158Z"
}
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "steps" : [
        {
            "actions" : [
                {
                    "app" : "app1",
                    "type" : "StartApplication"
                },
                {
                    "app" : "app2",
                    "type" : "StartApplication"
                }
            ]
        },
        {
            "actions" : [
                {
                    "type" : "ScaleApplication",
                    "app" : "app1"
                }
            ]
        },
        {
            "actions" : [
                {
                    "app" : "app2",
                    "type" : "ScaleApplication"
                }
            ]
        }
    ]
}
{% endhighlight %}

#### DELETE `/v2/groups/{groupId}`

Destroy a group. All data about that group and all associated applications will be deleted.

Since the deployment of the group can take a considerable amount of time, this endpoint returns immediatly with a version.
The failure or success of the action is signalled via event. There is a
`group_change_success` and `group_change_failed` event with the given version.

**Request:**

{% highlight http %}
DELETE /v2/groups/product/service/app HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: curl/7.35.0
{% endhighlight %}


**Response:**

{% highlight http %}
HTTP/1.1 200 Ok
Content-Type: application/json
Transfer-Encoding: chunked
Server: Jetty(8.y.z-SNAPSHOT)
{"version":"2014-07-01T10:20:50.196Z"}
{% endhighlight %}

### Tasks

#### GET `/v2/tasks`

List tasks of all applications.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>status</code></td>
      <td><code>string</code></td>
      <td>Return only those tasks whose <code>status</code> matches this
        parameter. If not specified, all tasks are returned. Possible values:
        <code>running</code>, <code>staging</code>. Default: <code>none</code>.</td>
    </tr>
  </tbody>
</table>

##### Example (as JSON)

**Request:**

{% highlight http %}
GET /v2/tasks HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate
Content-Type: application/json; charset=utf-8
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "tasks": [
        {
            "appId": "/bridged-webapp",
            "healthCheckResults": [
                {
                    "alive": true,
                    "consecutiveFailures": 0,
                    "firstSuccess": "2014-10-03T22:57:02.246Z",
                    "lastFailure": null,
                    "lastSuccess": "2014-10-03T22:57:41.643Z",
                    "taskId": "bridged-webapp.eb76c51f-4b4a-11e4-ae49-56847afe9799"
                }
            ],
            "host": "10.141.141.10",
            "id": "bridged-webapp.eb76c51f-4b4a-11e4-ae49-56847afe9799",
            "ports": [
                31000
            ],
            "servicePorts": [
                9000
            ],
            "stagedAt": "2014-10-03T22:16:27.811Z",
            "startedAt": "2014-10-03T22:57:41.587Z",
            "version": "2014-10-03T22:16:23.634Z"
        },
        {
            "appId": "/bridged-webapp",
            "healthCheckResults": [
                {
                    "alive": true,
                    "consecutiveFailures": 0,
                    "firstSuccess": "2014-10-03T22:57:02.246Z",
                    "lastFailure": null,
                    "lastSuccess": "2014-10-03T22:57:41.649Z",
                    "taskId": "bridged-webapp.ef0b5d91-4b4a-11e4-ae49-56847afe9799"
                }
            ],
            "host": "10.141.141.10",
            "id": "bridged-webapp.ef0b5d91-4b4a-11e4-ae49-56847afe9799",
            "ports": [
                31001
            ],
            "servicePorts": [
                9000
            ],
            "stagedAt": "2014-10-03T22:16:33.814Z",
            "startedAt": "2014-10-03T22:57:41.593Z",
            "version": "2014-10-03T22:16:23.634Z"
        }
    ]
}

{% endhighlight %}

##### Example (as text)

In text/plain only tasks with status `running` will be returned.

**Request:**

{% highlight http %}
GET /v2/tasks HTTP/1.1
Accept: text/plain
Accept-Encoding: gzip, deflate, compress
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: text/plain
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

my-app  19385 agouti.local:31336  agouti.local:31364  agouti.local:31382 
my-app2  11186 agouti.local:31337  agouti.local:31365  agouti.local:31383 
{% endhighlight %}

#### POST `/v2/tasks/delete`

Kill the given list of tasks and scale apps if requested.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>scale</code></td>
      <td><code>boolean</code></td>
      <td>Scale the app down (i.e. decrement its <code>instances</code> setting
        by the number of tasks killed) after killing the specified tasks.
        Default: <code>false</code>.</td>
    </tr>
  </tbody>
</table>

##### Example (as JSON)

**Request:**

{% highlight http %}
POST /v2/tasks/delete HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate
Content-Type: application/json; charset=utf-8
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{
    "ids": [
        "task.25ab260e-b5ec-11e4-a4f4-685b35c8a22e",
        "task.5e7b39d4-b5f0-11e4-8021-685b35c8a22e",
        "task.a21cb64a-b5eb-11e4-a4f4-685b35c8a22e"
    ]
}

{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Length: 0
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
{% endhighlight %}

### Deployments

<span class="label label-default">v0.7.0</span>

#### GET /v2/deployments

List running deployments

**Request:**

{% highlight http %}
GET /v2/deployments HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

[
    {
        "affectedApps": [
            "/test"
        ],
        "id": "867ed450-f6a8-4d33-9b0e-e11c5513990b",
        "steps": [
            [
                {
                    "action": "ScaleApplication",
                    "app": "/test"
                }
            ]
        ],
        "currentActions": [
          {
            "action": "ScaleApplication",
            "app": "/test"
          }
        ],
        "version": "2014-08-26T08:18:03.595Z",
        "currentStep": 1,
        "totalSteps" 1
    }
]
{% endhighlight %}

#### DELETE /v2/deployments/{deploymentId}

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>force</code></td>
      <td><code>boolean</code></td>
      <td>
        If set to <code>false</code> (the default) then the deployment is
        canceled and a new deployment is created to revert the changes of this
        deployment. Without concurrent deployments, this restores the configuration before this
        deployment. If set to <code>true</code>, then the deployment
        is still canceled but no rollback deployment is created.
        Default: <code>false</code>.</td>
    </tr>
  </tbody>
</table>

##### Example

Revert the deployment with `deploymentId` by creating a new deployment which reverses
all changes.

**Request:**

{% highlight http %}
DELETE /v2/deployments/867ed450-f6a8-4d33-9b0e-e11c5513990b HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Content-Length: 0
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "deploymentId": "0b1467fc-d5cd-4bbc-bac2-2805351cee1e",
    "version": "2014-08-26T08:20:26.171Z"
}
{% endhighlight %}

##### Example

Cancel the deployment with `deploymentId`, and do not create a new rollback deployment.

**Request:**

{% highlight http %}
DELETE /v2/deployments/177b7556-1287-4e09-8432-3d862981a987?force=true HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Content-Length: 0
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 202 Accepted
Content-Length: 0
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
{% endhighlight %}


### Event Stream

#### GET `/v2/events`

<span class="label label-default">v0.9.0</span>

Attach to the marathon event stream.

To use this endpoint, the client has to accept the text/event-stream content type.
Please note: a request to this endpoint will not be closed by the server.
If an event happens on the server side, this event will be propagated to the client immediately.
See [Server Sent Events](http://www.w3schools.com/html/html5_serversentevents.asp) for a more detailed explanation.

**Request:**

```
GET /v2/events HTTP/1.1
Accept: text/event-stream
Accept-Encoding: gzip, deflate
Host: localhost:8080
User-Agent: HTTPie/0.8.0
```

**Response:**

```
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, must-revalidate
Connection: close
Content-Type: text/event-stream;charset=UTF-8
Expires: 0
Pragma: no-cache
Server: Jetty(8.1.15.v20140411)

```

If an event happens on the server side, it is sent as plain json prepended with the mandatory `data:` field.

**Response:**
```
data: {"remoteAddress":"96.23.11.158","eventType":"event_stream_attached","timestamp":"2015-04-28T12:14:57.812Z"}

data: {"groupId":"/","version":"2015-04-28T12:24:12.098Z","eventType":"group_change_success","timestamp":"2015-04-28T12:24:12.224Z"}
```

### Event Subscriptions

#### POST /v2/eventSubscriptions

Register a callback URL as an event subscriber.

NOTE: To activate this endpoint, you need to start Marathon with `--event_subscriber http_callback`.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>callbackUrl</code></td>
      <td><code>string</code></td>
      <td>URL to which events should be posted. <strong>Required.</strong></td>
    </tr>
  </tbody>
</table>

**Request:**

{% highlight http %}
POST /v2/eventSubscriptions?callbackUrl=http://localhost:9292/callback HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "callbackUrl": "http://localhost:9292/callback",
    "clientIp": "0:0:0:0:0:0:0:1",
    "eventType": "subscribe_event"
}
{% endhighlight %}

#### GET `/v2/eventSubscriptions`

List all event subscriber callback URLs.

NOTE: To activate this endpoint, you need to startup Marathon with `--event_subscriber http_callback`.

##### Example

**Request:**

{% highlight http %}
GET /v2/eventSubscriptions HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "callbackUrls": [
        "http://localhost:9292/callback"
    ]
}
{% endhighlight %}

#### DELETE `/v2/eventSubscriptions`

Unregister a callback URL from the event subscribers list.

NOTE: To activate this endpoint, you need to start Marathon with `--event_subscriber http_callback`.

##### Parameters

<table class="table table-bordered">
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>callbackUrl</code></td>
      <td><code>string</code></td>
      <td>URL passed when the event subscription was created. <strong>Required.</strong></td>
    </tr>
  </tbody>
</table>

##### Example

**Request:**

{% highlight http %}
DELETE /v2/eventSubscriptions?callbackUrl=http://localhost:9292/callback HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Length: 0
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "callbackUrl": "http://localhost:9292/callback",
    "clientIp": "0:0:0:0:0:0:0:1",
    "eventType": "unsubscribe_event"
}
{% endhighlight %}

### Queue

<span class="label label-default">v0.7.0</span>

#### GET `/v2/queue`

Show content of the task queue.

##### Example

{% highlight http %}
GET /v2/queue HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: localhost:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

{% highlight http %}
HTTP/1.1 200 OK
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "queue": [
        {
            "count" : 10,
            "delay": {
              "overdue": "true",
              "timeLeftSeconds": 784
            }
            "app" : {
                "cmd" : "tail -f /dev/null",
                "backoffSeconds" : 1,
                "healthChecks" : [],
                "storeUrls" : [],
                "constraints" : [],
                "env" : {},
                "cpus" : 0.1,
                "labels" : {},
                "instances" : 10,
                "ports" : [
                   10000
                ],
                "requirePorts" : false,
                "uris" : [],
                "container" : null,
                "backoffFactor" : 1.15,
                "args" : null,
                "version" : "2015-02-09T10:49:59.831Z",
                "maxLaunchDelaySeconds" : 3600,
                "upgradeStrategy" : {
                   "minimumHealthCapacity" : 1,
                   "maximumOverCapacity" : 1
                },
                "dependencies" : [],
                "mem" : 16,
                "id" : "/foo",
                "disk" : 0,
                "executor" : "",
                "user" : null
            }
        }
    ]
}
{% endhighlight %}

#### DELETE `/v2/queue/{appId}/delay`

The application specific task launch delay can be reset by calling this endpoint 

##### Example

{% highlight http %}
DELETE /v2/queue/myapp/delay HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 0
Host: localhost:8080
User-Agent: HTTPie/0.9.2
{% endhighlight %}

{% highlight http %}
HTTP/1.1 204 No Content
Cache-Control: no-cache, no-store, must-revalidate
Expires: 0
Pragma: no-cache
Server: Jetty(8.1.15.v20140411)
{% endhighlight %}

### Server Info

<span class="label label-default">v0.7.0</span>

#### GET `/v2/info`

Get info about the Marathon Instance

**Request:**

{% highlight http %}
GET /v2/info HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Content-Type: application/json; charset=utf-8
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Length: 872
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)

{
    "frameworkId": "20140730-222531-1863654316-5050-10422-0000",
    "leader": "127.0.0.1:8080",
    "http_config": {
        "assets_path": null,
        "http_port": 8080,
        "https_port": 8443
    },
    "event_subscriber": {
        "type": "http_callback",
        "http_endpoints": [
            "localhost:9999/events"
        ]
    },
    "marathon_config": {
        "checkpoint": false,
        "executor": "//cmd",
        "failover_timeout": 604800,
        "ha": true,
        "hostname": "127.0.0.1",
        "local_port_max": 49151,
        "local_port_min": 32767,
        "master": "zk://localhost:2181/mesos",
        "mesos_role": null,
        "mesos_user": "root",
        "reconciliation_initial_delay": 30000,
        "reconciliation_interval": 30000,
        "task_launch_timeout": 60000
    },
    "name": "marathon",
    "version": "0.7.0-SNAPSHOT",
    "zookeeper_config": {
        "zk": "zk://localhost:2181/marathon",
        "zk_timeout": 10000,
        "zk_session_timeout": 1800000,
        "zk_max_version": 5
    }
}
{% endhighlight %}

#### GET `/v2/leader`

Returns the current leader.
If no leader exists, Marathon will respond with a 404 error.

**Request:**

{% highlight http %}
GET /v2/leader HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Length: 872
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)

{
    "leader": "127.0.0.1:8080"
}
{% endhighlight %}

#### DELETE `/v2/leader`

<span class="label label-default">v0.7.7</span>

Causes the current leader to abdicate, triggering a new election.
If no leader exists, Marathon will respond with a 404 error.

**Request:**

{% highlight http %}
DELETE /v2/leader HTTP/1.1
Accept: application/json
Accept-Encoding: gzip, deflate, compress
Host: localhost:8080
User-Agent: HTTPie/0.7.2
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Content-Length: 872
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)

{
    "message": "Leadership abdicted"
}
{% endhighlight %}

### Miscellaneous

**Request:**

{% highlight http %}
{% endhighlight %}

**Response:**

{% highlight http %}
{% endhighlight %}

#### GET `/ping`

**Request:**

{% highlight http %}
GET /ping HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Access-Control-Allow-Credentials: true
Cache-Control: must-revalidate,no-cache,no-store
Content-Length: 5
Content-Type: text/plain;charset=ISO-8859-1
Server: Jetty(8.y.z-SNAPSHOT)

pong
{% endhighlight %}

#### GET `/logging`

**Request:**

{% highlight http %}
GET /logging HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0{% endhighlight %}

**Response:**

_HTML-only endpoint_

#### GET `/help`

**Request:**

{% highlight http %}
GET /help HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

_HTML-only endpoint_

#### GET `/metrics`

**Request:**

{% highlight http %}
GET /metrics HTTP/1.1
Accept: */*
Accept-Encoding: gzip, deflate
Host: mesos.vm:8080
User-Agent: HTTPie/0.8.0
{% endhighlight %}

**Response:**

{% highlight http %}
HTTP/1.1 200 OK
Cache-Control: must-revalidate,no-cache,no-store
Content-Type: application/json
Server: Jetty(8.y.z-SNAPSHOT)
Transfer-Encoding: chunked

{
    "counters": {
        ...
    },
    "gauges": {
        ...
    },
    "histograms": {},
    "meters": {
        ...
    },
    "timers": {
        ...
    },
    "version": "3.0.0"
}
{% endhighlight %}
