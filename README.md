[![Build Status](https://travis-ci.org/cosu/vampires-akka.svg?branch=master)](https://travis-ci.org/cosu/vampires-akka)
[![Dependency Status](https://www.versioneye.com/user/projects/5652d6e1ff016c002c00057f/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5652d6e1ff016c002c00057f)
[![codecov.io](https://codecov.io/github/cosu/vampires-akka/coverage.svg?branch=master)](https://codecov.io/github/cosu/vampires-akka?branch=master)

# vampires-akka  

Vampires-akka is a task farming utility with support for various computing resources.
Currently supported "resources":

- Local - the local machine
- SSH - remote machines via ssh
- EC2 - Amazon EC2 resources
- DAS5 - Das5 is a Dutch research cluster. vampires uses slurm tooling to spawn clients

Clients can partition their resources by using cpusets in combination with either numactl or docker (where available)


Interesting features:

- Client host monitoring (CPU, network)
- Configurable CPU allocation policy for tasks


Configuration examples can be found `/conf/*.conf`
