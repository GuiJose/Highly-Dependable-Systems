# Highly-Dependable-Systems
Istanbul BFT Consensus Algorithm Implementation

### Requirements

- Maven 3.9.0
- Java 17

## Base Setup

```shell
$ mvn clean install 
```

## Running the system.

```shell
$ ./InitSystem.sh <NServers> <NByzantineServers> <NUsers> 
```

- NServers is the total number of servers.
- NByzantineServers is the total number of byzantine servers.
- NUsers is the total number of users.
- NOTE: NServers must be equal to 3*NByzantineServers + 1, and NByzantineServers can vary between 0 and NByzantineServers. 

### Example Parameters
```
./ServerInit.bash 7 2 2
```
