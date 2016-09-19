# Getting started

Vampires  is developed under OSX and Linux. Running under Windows is not supported. 
The only requierment is Java8 sdk available on the host.

For build and dependency management Vampire uses `gradle`. 
You can chose to use your local gradle installation or make use of the gradle wrapper.
The wrapper will download gradle and will allow you to build, compile and install the application.

## Compile and run tests

* clone this repo to vampires-akka.src
    * `git clone $REPO_URL vampires-akka.src`
*  Build using gradle.
    * `./gradlew build`
* Run the unit tests using gradle
    * `./gradlew test`

If everything works you can now use vampires to run bags-of-tasks.

## Installation
* . The `installLocal` gradle task will compile and package the server and client components and the it will install them in a directory called `vampires-akka-dist` in your homedir.
    * `./gradlew installLocal`

## TL;DR Build
Clone the repo , cd to it and run `./build.sh`

## Local execution
* Run a set of tasks using the local machine as a worker. Logs and results are saved in the home dir. Inspect the `conf/local.conf` file and `src/main/java/resources/application.conf` files to get more details.
    * `cd $HOME`
    * `vampires-akka-dist/server-1.0-SNAPSHOT/scripts/vampires_server.sh vampires-akka.src/conf/local.conf`


* Run a simple job:
    * `./run.sh -p ssh -t local -c 1 -n 1 -j 'ping google.com -c2'`
    * This runs a single ping command via ssh on the localhost.
    
## Running on  DAS5
You will need to configure your environment to be able to compile and run the server on the head node.

All of the following commands should be executed on the das5 headnode. 

Add the following lines to your `$HOME/.bashrc` file.
```
  module load java/jdk-1.8.0
  module load slurm
```
Compile, run the tests and install the client and server binaries just like in the local setup.

Vampires submits jobs to slurm, the queue manager of DAS5 via ssh. 
While this might sound silly at this point, this is to allow more flexibility when it comes to 
where to run the vampires server.
You could run the server on a remote machine and have cluster nodes connect to it. 
In the scenario presented here the vampires server will run on the headnode. 

In order to allow the server to interact with the head node and submit jobs, you have to point the 
server to a valid ssh key which is authorized to login to the head node. 

In `vampires-akka.src/conf/das5.conf` you can find a sample config for das5.  The relevant 
authentication section:

```
resources {
      das5.uva.address = fs2.das5.science.uva.nl
      das5.uva.privateKey = ${?HOME}"/.ssh/id_rsa"
    }
```

You should read this as: "to connect to das5.uva resources use `fs2.das5.science.uva.nl` as a 
hostname and the `id_rsa` file as a private ssh key".

Use `ssh-keygen` to generate a ssh key key-pair and place the public part in 
`.ssh/authorized_keys` on DAS5. 

If `ssh -i $HOME/.ssh/id_rsa fs2.das5.science.uva.nl` works, then you can move to running the 
sample workload on das5.

```
cd $HOME
vampires-akka-dist/server-1.0-SNAPSHOT/scripts/vampires_das5_.sh vampires-akka.src/conf/das5.conf
```

### DAS5 via a ssh tunnel

:TODO

## Running using SSH resources

SSH resources need to be statically configured in the config file. In the example config localhost 
is configured as a static resource.

    ssh {
      user = ${USER}
      privateKey = ${?HOME}"/.ssh/id_rsa"
      local {
        address = "localhost"
        cost=10
      }
    }

Here we've configured access to localhost. The username used will be the current user under which 
the server is runningand the private key will be the one in .ssh.

We can add more resources under the ssh provider (the same approach goes for other providers too)

    ssh {
      user = ${USER}
      privateKey = ${?HOME}"/.ssh/id_rsa"
      node1 {
        address = "node1"
        cost=10
      }
      node2 {
        user = "another-user"
        address = "node3"
        cost=10
      }
      node3 {
        user = "yet-another-user"
        privateKey = ${?HOME}"/.ssh/another_secret_key"
        address = "node3"
        cost=10
      }
    }


## Executors
An executor is the environment under which a job runs. The executor provides monitoring and resource
isolation (where available).

Vampires comes for now with two built in executors. 
* Fork
* Docker

Vampires will prefer Docker over Fork if both of them are available.  The docker executor can be 
disabled in the config file like this:

    vampires.executors = ["fork"]
    
### Fork
The Fork executor spawns the job as a new child process. The process is supervised/monitored and
at every 500ms host metrics are collected and appended to the job's metadata.

If `numactl` is available on the host then  vampires switches to resource isolation mode. The currently
available CPUS are split into CPUSets. CPUsets are a partition of the available CPUs. The executor
will allocate a job to one CPUSet. The size of the CPUSet is configurable  on the server using the 
environment variable `$CPU_SET_SIZE` or the config key `vampires.cpu-set-size`.
 

For example, if the host has 4 CPUs and the cpuset size is 2, then two CPUSets will be created.
This will allow the execution of two parallel jobs on that host.

This feature was introduces with cpu bound jobs in mind.

#### Monitoring keys for Fork 
 
##### Memory
* none
##### CPU
* cpus-average-load - average load of the host for the last minute
* cpus-load - "recent cpu usage" for the whole system from 
com.sun.management.OperatingSystemMXBean#getSystemCpuLoad() if a This value is a double in the 
[0.0,1.0] interval. A value of 0.0 means that all CPUs were idle during the recent period of time 
observed, while a value of 1.0 means that all CPUs were actively running 100% of the time during the recent period being observed. 
* cpus-load-tick-idle-0 -  cpu load ticks.  measuring the difference between ticks across a time 
interval, CPU load over that interval may be calculated. The metric is per CPU id for user, idle, system, nice
* cpus-load-tick-nice-0
* cpus-load-tick-system-0
* cpus-load-tick-user-0

##### Network

* network-rx-bytes-iface - byte counters for the network interface `iface`
* network-rx-speed-iface - network speed computed by the Linux kernel via  /sys/class/net/`iface`/speed"
* network-tx-bytes-iface
* network-tx-speed-iface


### Docker

The Docker executor is similar in behaviour to the Fork executor. Instead of running next to the 
client process if the Docker executor is used, each job will run in  its own docker container. 
CPUsets are employed by default if Docker is used. 

The Docker containers are not reused between jobs so each job will run in its own clean evironment.
In addition to CPU metrics the Docker container also provides metrics on the network behaviour of 
the jobs. This is extremely useful for profiling jobs.

#### Monitoring keys for Docker

##### Memory

* memory-usage  - This metrics reports the current memory (RAM) usage of the container, in bytes. 
Memory usage % = 100 * memory-usage / memory-limit
* memory-limit - memory limit of the container
* memory-max-usage - Maximum measured memory usage of the container, in bytes, during the lifetime of the container. 
* memory-failcnt  - field gives the number of times that the cgroup limit was
exceeded.

###### Memory stats as reported by cgroups

Details here [here](htps://www.kernel.org/doc/Documentation/cgroup-v1/memory.txt)

* memory-stats-rss-huge
* memory-stats-active-file
* memory-stats-total-pgpgout
* memory-stats-total-cache
* memory-stats-pgmajfault
* memory-stats-cache
* memory-stats-total-active-anon
* memory-stats-total-rss
* memory-stats-unevictable
* memory-stats-total-writeback
* memory-stats-total-pgmajfault
* memory-stats-inactive-anon
* memory-stats-total-inactive-file
* memory-stats-pgpgin
* memory-stats-total-pgpgin
* memory-stats-total-mapped-file
* memory-stats-hierarchical-memory-limit
* memory-stats-rss
* memory-stats-total-unevictable
* memory-stats-total-pgfault
* memory-stats-total-active-file
* memory-stats-total-dirty
* memory-stats-writeback
* memory-stats-active-anon
* memory-stats-total-inactive-anon
* memory-stats-pgfault
* memory-stats-pgpgout
* memory-stats-total-rss-huge
* memory-stats-inactive-file
* memory-stats-mapped-file
* memory-stats-dirty


##### CPU

CPU stats [datadog](https://www.datadoghq.com/blog/how-to-collect-docker-metrics)

* cpu-system-cpu-usage - how much CPU time, in jiffies, the system (Docker host) has used.  CPU usage % = 100 * cpu.usage.total / cpu.usage.system
* cpu-cpu-usage-percpu-usage-0 - nanoseconds CPU has been in use since boot
* cpu-cpu-usage-total-usage - total nanoseconds CPUs have been in use  
* cpu-cpu-usage-usage-in-kernelmode - time spent executing system calls since boot (in jiffies - 1jiffie = 10ms)
* cpu-cpu-usage-usage-in-usermode - time spent running processes since boot (in jiffies - 1jiffie = 10ms)
* cpu-throttling-data-throttled-time
* cpu-throttling-data-periods
* cpu-throttling-data-throttled-periods

##### Network

Network stats 

* network-eth0-tx-dropped
* network-eth0-rx-packets
* network-eth0-tx-errors
* network-eth0-rx-errors
* network-eth0-tx-packets
* network-eth0-rx-dropped
* network-eth0-tx-bytes
* network-eth0-rx-bytes


## Configuration 101

TODO
