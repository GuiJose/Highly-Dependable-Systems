# Highly-Dependable-Systems
Istanbul BFT Consensus Algorithm Implementation

### Requirements

- Maven 3.9.0
- Java 17

## Base Setup

```shell
$ mvn clean install 
```

---

## To Run Servers

```shell
$ cd Server
$ ./ServerInit.bash <NServers> <NByzantineServers> 
```

- NServers is the total number of servers.
- NByzantineServers is the total number of byzantine servers.
- NOTE: NServers must be equal to 3*NByzantineServers + 1, and NByzantineServers can vary between 0 and NByzantineServers. 

### Example Parameters
```
./ServerInit.bash 7 2
```

When the servers are running you can press "1" and then "ENTER" to see the contents of the blockchain.

## To Run Users


```shell
$ cd User
$ ./UserInit.bash <NUsers> 
```

### Example Parameters

```
./UserInit.bash 1
```

When the user is running you can type the string you want to append and then press "ENTER" to send the request.