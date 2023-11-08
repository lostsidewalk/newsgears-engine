<link rel="stylesheet" type="text/css" href="style.css">

# newsgears-engine

newsgears is a multi-user, self-hosted all-in-one RSS reader/aggregator platform.

This repository contains the Engine server. The engine is responsible for running periodically executing jobs, such as 
purging of old/un-needed data.

## 1. Quick-start using pre-built containers:

If you don't want to do development, just start NewsGears using pre-built containers:

```
docker ...
```

<hr>

## 3. For local development:

If you don't want to use the pre-built containers (i.e., you want to make custom code changes and build your own containers), then use the following instructions.

### Setup command aliases:

A script called `build_module.sh` is provided to expedite image assembly.  Setup command aliases to run it to build the required images after you make code changes:

```
alias ng-engine='./build_module.sh newsgears-engine'
```

#### Alternately, setup aliases build debuggable containers:

```
alias ng-engine='./build_module.sh newsgears-api --debug 55005'
```

*Debuggable containers pause on startup until a remote debugger is attached on the specified port.*

### Build and run:

#### Run the following command in the directory that contains ```newsgears-engine```:

```
ng-engine && docker ...
```

Boot down in the regular way, by using ```docker ...``` in the ```newsgears-engine``` directory.

<hr> 

You can also use the `ng-engine` alias to rebuild the container (i.e., to deploy code changes).

```
$ ng-engine # rebuild the engine server container 
```
