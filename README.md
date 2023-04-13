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


# TESTING
In order to test the system we recommend you to lauch 4 servers, 1 bizantine and two users.
```
./ServerInit.bash 4 1 2 
```

First of all, notice that the bizantine server, periodically, tries to send a Preprepare message on the behalf of the leader, but the other servers ignore this behavior, because the messages are protected and authenticated by digital signatures and the bizantine server cannot forge the leader's signature.

We recommend you to test the weak and strong check balance in each user. Follow the instructions printed in the user's terminals. Both reads should return the value 100.

In the weak read, the user contacts only one replica, if it contacts a bizantine server, the bizantine server will try to send a fake response. The user can understand this and will print an error message. Try the weak read until the read goes to a legitimate server.

Then, you can try to make an invalid operation, in the user 0 try to send money to himself. Enter "2" in its terminal and then "0 10". The servers' leader will return an error message because the operation is invalid.

Make a valid operation. In user 0, make a tranfer to user 1 and send him one token. Enter "2" in the terminal and then "1 1". Make a strong read. It should return 100, because the servers have not processed the block yet, because they are waiting for more operations to fill up the first block. 

After this, make 2 valid operations. In user 0, make a tranfer to user 1 and send him one token, do this 2 times. Make a strong read. It should return 94, because the servers processed the block, each block takes 3 transfer operations, the user has 94 tokens because it transfered 3 tokens to user 1 and paid 1 token as fee to the servers' leader per operation. Make a weak read, it should return 100, because the blockchain does not have special blocks yet.

You can also test the reads in the user 1.

After this make more 9 valid operations. In user 0, make a tranfer to user 1 and send him one token, do this 9 times. Make a strong read. It should return 76, because the servers processed all the operations, the user has 94 tokens because it transfered 12 tokens to user 1 and paid 1 token as fee to the servers' leader per operation. Make a weak read, it should return 76, because after the last tranfer operation, and after the block where this operation was inserted in, was appended to the blockchain, it was also appended a special block to the blockchain.

Do some reads in the user 1 and check if the values are coherent.
