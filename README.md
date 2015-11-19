![Build Status](https://travis-ci.org/cosu/vampires-akka.svg)
# vampires-akka  

Vampires-akka is a task farming utility with support for various computing resources.
Currently supported "resources":

- Local - spawns _clients_ on the local machine
- SSH - spawns  _clients_ on remote machines via ssh
- EC2 - spawns _clients_ on Amazon EC2 resources
- DAS5 - Das5 is a Dutch research cluster. vampires uses slurm tooling to spawn clients

Clients can partition their resources by using cpusets in combination with either numactl or docker (where available)


Interesting features:

- Client host monitoring (CPU, network)
- Configurable CPU allocation policy for tasks


Configuration examples can be found `/resources/*.conf`
