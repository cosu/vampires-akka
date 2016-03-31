# Getting started

Vampires  is developed under OSX and Linux. Running under Windows is not supported. 
The only requierment is Java8 sdk available on the host.

For build and dependency management Vampire uses `gradle`. 
You can chose to use your local gradle installation or make use of the gradle wrapper.
The wrapper will download gradle and will allow you to build, compile and install the application.

## Compile and run tests

* clone this repo to vampires-akka.src
    * `git clone $REPO_URL vampies-akka.src`
*  Build using gradle.
    * `./gradlew build`
* Run the unit tests using gradle
    * `./gradlew test`

If everything works you can now use vampires to run bags-of-tasks.

## Installation
* Install the binaries to your home directory. The `installLocal` task will compile and package the server and client components and the it will install them in a directory called `vampires-akka-dist` in your homedir.
    * `./gradlew installLocal`

## Local execution
* Run a set of tasks using the local machine as a worker. Logs and results are saved in the home dir. Inspect the `conf/local.conf` file and `src/main/java/resources/application.conf` files to get more details.
    * `cd $HOME`
    * `vampires-akka-dist/server-1.0-SNAPSHOT/scripts/vampires_server.sh vampires-akka.src/conf/local.conf`


## Running on  DAS5
You will need to configure your environment to be able to compile and run the server on the head node.

All of the following commands should be executed on the das5 headnode. 

Add the following lines to your `$HOME/.bashrc` file.
```
  module load java/jdk-1.8.0
  module load slurm
```
Compile, run the tests and install the client and server binaries just like in the local setup.

Vampires submits jobs to slurm, the queue manager of DAS5 via ssh. While this might sound silly at this point,
this is to allow more flexibility when it comes to where to run the vampires server.
You could run the server on your home machine and have DAS5 workers connect to it. In the scenario presented here the vampires server will run on the headnode. 

In order to allow the server to interact with the head node and submit jobs, you have to point the server to a valid ssh key which is authorized to login to the head node. 

In `vampires-akka.src/conf/das5.conf` you can find a sample config for das5.  The relevant authentication section:

```
resources {
      das5.uva.address = fs2.das5.science.uva.nl
      das5.uva.privateKey = ${?HOME}"/.ssh/id_rsa"
    }
```

You should read this as: "to connect to das5.uva resources use `fs2.das5.science.uva.nl` as a hostname and the `id_rsa` file as a private ssh key".

Use `ssh-keygen` to generate a ssh key key-pair and place the public part in `.ssh/authorized_keys` on DAS5. 

If `ssh -i $HOME/.ssh/id_rsa fs2.das5.science.uva.nl` works, then you can move to running the sample workload on das5.

```
cd $HOME
vampires-akka-dist/server-1.0-SNAPSHOT/scripts/vampires_das5_.sh vampires-akka.src/conf/das5.conf
```


### DAS5 via a ssh tunnel

:TODO

## Running using SSH resources

:TODO

## Executors

* Fork
:TODO 
* Docker
:TODO 


