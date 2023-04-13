package hdl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import hdl.messages.ACKUSER_MESSAGE;
import hdl.messages.ACK_MESSAGE;
import hdl.messages.CHECK_MESSAGE;
import hdl.messages.CREATE_MESSAGE;
import hdl.messages.RESPONSE;
import hdl.messages.RESPONSE_CHECK;
import hdl.messages.RESPONSE_CREATE;
import hdl.messages.RESPONSE_TRANSFER;
import hdl.messages.TRANSFER_MESSAGE;

public class UserFrontend {
    private ConcurrentHashMap<Integer, List<List<Integer>>> transfers = new ConcurrentHashMap<>();
                            //transferId [[ids de servers sucesso], [ids de servers onde n teve sucesso]]
    private ConcurrentHashMap<Integer, List<List<Integer>>> checks = new ConcurrentHashMap<>();
                            //checkId  [ [state] [server_id, timestamp, balance]]
    private ConcurrentHashMap<Integer, List<List<Integer>>> creates = new ConcurrentHashMap<>();
                            //createsId [[ids de servers sucesso], [ids de servers onde n teve sucesso]]
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private int messageID = 0;
    private List<List<Integer>> messagesNotACKED = new ArrayList<>();
    private List<byte[]> messagesHistory = new ArrayList<>();
    private int quorum;
    private int readTimeStamp = 0;
    

    private List<List<Integer>> messagesReceived = new ArrayList<>();


    public UserFrontend(int port, int numServers) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);
        int byzantineServersSuported = (int) Math.floor((numServers-1)/3);
        this.quorum = 2 * byzantineServersSuported + 1;

        for (int i = 0; i < numServers; i++) {
            List<Integer> l = new ArrayList<>();
            messagesReceived.add(l);
        }

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    sendMessagesAgain();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, 2000);
    }

    public void listening() throws Exception{
        byte[] buffer = new byte[25000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            receiverSocket.receive(packet);
        
            byte[] signature = new byte[512];
            byte[] msg = new byte[packet.getLength()-512];
            System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);
            System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
            
            ByteArrayInputStream byteStream = new ByteArrayInputStream(msg);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            Object obj = objectStream.readObject();

            if (obj instanceof ACK_MESSAGE){
                ACK_MESSAGE M = (ACK_MESSAGE) obj;
                String keyPath = "../Common/resources/S" + M.getId() + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    receivedACK(M.getId(), M.getMessageACKED());
                }
            }

            else if (obj instanceof RESPONSE){
                RESPONSE M = (RESPONSE) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    sendAck(M.getServerId(), M.getMessageId());
                    if (!receivedMessage(M.getServerId(), M.getMessageId())){
                        System.out.println("Received message from server " + M.getServerId() + " with message: " + M.getMessage());
                    }
                }
            }

            else if (obj instanceof RESPONSE_CREATE){
                RESPONSE_CREATE M = (RESPONSE_CREATE) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    sendAck(M.getServerId(), M.getMessageId());
                    if (!receivedMessage(M.getServerId(), M.getMessageId())){
                        receivedResponseCreate(M);
                    }
                }
            }

            else if (obj instanceof RESPONSE_TRANSFER){
                RESPONSE_TRANSFER M = (RESPONSE_TRANSFER) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    sendAck(M.getServerId(), M.getMessageId());
                    if (!receivedMessage(M.getServerId(), M.getMessageId())){
                        receivedResponseTransfer(M);
                    }
                }
            }

            else if (obj instanceof RESPONSE_CHECK){
                RESPONSE_CHECK M = (RESPONSE_CHECK) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    sendAck(M.getServerId(), M.getMessageId());
                    if (!receivedMessage(M.getServerId(), M.getMessageId())){
                        if (M.getIsStrong()){
                            receivedResponseStrongCheck(M);
                        }
                        else{
                            receivedResponseWeakCheck(M);
                        }
                    }
                }
            }
            packet.setLength(buffer.length); 
        }
    }

    public synchronized void receivedResponseWeakCheck(RESPONSE_CHECK M) throws Exception{
        if (M.getIsFirstValue()){
            System.out.println("Your balance is 100 with timestamp 0.");
            return;
        }

        if(M.getBlock().getSignatures().size() >= quorum){
            int count = 0;
            for (Map.Entry<Integer, byte[]> entry : M.getBlock().getSignatures().entrySet()) {
                Integer serverId = entry.getKey();
                byte[] signature = entry.getValue();
                String keyPath = "../Common/resources/S" + serverId + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                byte[] bytes = ByteArraysOperations.SerializeObject(M.getBlock().getAccounts()); 
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(bytes);
                if (DigitalSignature.VerifySignature(hash, signature, key)){
                    count++;
                }
            }
            if (count>=quorum){
                if (M.getBlock().getAccounts().containsKey(User.getPubKey())){
                    System.out.println("Your balance is " + M.getBlock().getAccounts().get(User.getPubKey())[0] + " with timestamp " + M.getBlock().getAccounts().get(User.getPubKey())[1] + ".");
                }
                else{
                    System.out.println("Your balance is 100 with timestamp 0.");
                }
            }
        }
        else{
            System.out.println("Received a response to my weak read from a bizantine server!");
        }
    }

    public synchronized void receivedResponseCreate(RESPONSE_CREATE M){
        if (M.getSucceded()){
            if (!transfers.get(0).get(0).contains(M.getServerId())){
                transfers.get(0).get(0).add(M.getServerId());
                if (transfers.get(0).get(0).size() == this.quorum){
                    System.out.println("Your account was created!");
                }
            }
        }
        else{
            if (!transfers.get(0).get(1).contains(M.getServerId())){
                transfers.get(0).get(1).add(M.getServerId());
                if (transfers.get(0).get(1).size() == this.quorum){
                    System.out.println("Your account was not created!");
                }
            }
        }
    }

    public synchronized boolean receivedMessage(int serverId, int msgId){
            if (!messagesReceived.get(serverId).contains(msgId)){
                messagesReceived.get(serverId).add(msgId);
                return false;
            }
            return true;
    }

    public synchronized void receivedResponseStrongCheck(RESPONSE_CHECK M){
        if (M.getTimestamp() >= this.readTimeStamp){
            for (List<Integer> list : checks.get(M.getCheckId())){
                if (list.get(0) == M.getServerId()){
                    return;
                }
            }
            List<Integer> newList = new ArrayList<>();
            newList.add(M.getServerId());
            newList.add(M.getTimestamp());
            newList.add(M.getBalance());
            checks.get(M.getCheckId()).add(newList);

            if (checks.get(M.getCheckId()).size() >= quorum && checks.get(M.getCheckId()).get(0).get(0) == 0){
                int mostCommonTimestamp = mostCommonTimestamp(M);
                int mostCommonBalance = mostCommonBalance(M, mostCommonTimestamp);
                
                if (mostCommonTimestamp == -1 || mostCommonBalance == -1){
                    return;
                }
                
                checks.get(M.getCheckId()).get(0).set(0, 1);
                
                this.readTimeStamp = mostCommonTimestamp;
                System.out.println("Your balance is: " + mostCommonBalance + " with timestamp: " + this.readTimeStamp);                
            }
        }
    }

    public synchronized int mostCommonTimestamp(RESPONSE_CHECK M){
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (List<Integer> innerList : checks.get(M.getCheckId())) {
            if (innerList.size() == 3 ){
                frequencyMap.put(innerList.get(1), frequencyMap.getOrDefault(innerList.get(1), 0) + 1);
            }
        }
    
        int mostCommonTimestamp = 0;
        int highestFrequency = 0;
    
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
          int num = entry.getKey();
          int frequency = entry.getValue();
    
          if (frequency > highestFrequency) {
            mostCommonTimestamp = num;
            highestFrequency = frequency;
          }
        }
        if (highestFrequency >= this.quorum){
            return mostCommonTimestamp;
        }
        else{
            return -1;
        }
    }

    public synchronized int mostCommonBalance(RESPONSE_CHECK M, int timestamp){
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (List<Integer> innerList : checks.get(M.getCheckId())) {
            if (innerList.size() == 3){
                if (innerList.get(1) == timestamp){
                    frequencyMap.put(innerList.get(2), frequencyMap.getOrDefault(innerList.get(2), 0) + 1);
                }
            }
        }
    
        int mostCommonBalance = 0;
        int highestFrequency = 0;
    
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
          int num = entry.getKey();
          int frequency = entry.getValue();
    
          if (frequency > highestFrequency) {
            mostCommonBalance = num;
            highestFrequency = frequency;
          }
        }
        if (highestFrequency >= this.quorum){
            return mostCommonBalance;
        }
        else{
            return -1;
        }
    }

    public synchronized void sendAck(int serverId, int messageACKED) throws Exception{
        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        ACKUSER_MESSAGE message = new ACKUSER_MESSAGE(User.getid(), this.messageID, messageACKED);
        InetAddress ip = InetAddress.getByName((String) User.getServers().get(serverId).get(0)); 
        int leaderPort = (int) User.getServers().get(serverId).get(1);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, privKey);
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, leaderPort);
        this.senderSocket.send(packet);
    }

    public synchronized void receivedResponseTransfer(RESPONSE_TRANSFER M){
        if (M.getSucceded()){
            if (!transfers.get(M.getTranferId()).get(0).contains(M.getServerId())){
                transfers.get(M.getTranferId()).get(0).add(M.getServerId());
                if (transfers.get(M.getTranferId()).get(0).size() == this.quorum){
                    System.out.println("Your transfer of " + M.getAmount() + " to user " + M.getDestUserId() + " succeded.");
                }
            }
        }
        else{
            if (!transfers.get(M.getTranferId()).get(1).contains(M.getServerId())){
                transfers.get(M.getTranferId()).get(1).add(M.getServerId());
                if (transfers.get(M.getTranferId()).get(1).size() == this.quorum){
                    System.out.println("Your transfer of " + M.getAmount() + " to user " + M.getDestUserId() + " did not succed.");
                }
            }
        }
    }

    public synchronized void receivedACK(int serverid, int messageACKED){
        messagesNotACKED.get(messageACKED).remove(Integer.valueOf(serverid));
    }

    public void sendCheck(int port, PublicKey key, Boolean isStrong) throws Exception{
        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        CHECK_MESSAGE message = new CHECK_MESSAGE(User.getid(), this.messageID, "localhost", port, key, isStrong);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, privKey);

        if (isStrong){
            List<List<Integer>> newList = new ArrayList<>();
            List<Integer> newList2 = new ArrayList<>(); 
            newList2.add(0);
            newList.add(newList2);
            checks.put(messageID, newList);
            for(List<Object> server : User.getServers()){
                InetAddress ip = InetAddress.getByName((String) server.get(0)); 
                int serverPort = (int) server.get(1);        
                DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, serverPort);
                this.senderSocket.send(packet);
            }
            List<Integer> serverIds = new ArrayList<>();
            for (int i = 0; i<User.getServers().size();i++){
                serverIds.add(i);
            }
            this.messagesNotACKED.add(messageID, serverIds);
        }
        else{
            Random random = new Random();
            int randomNumber = random.nextInt(User.getServers().size());
            InetAddress ip = InetAddress.getByName((String) User.getServers().get(randomNumber).get(0)); 
            int leaderPort = (int) User.getServers().get(randomNumber).get(1);        
            DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, leaderPort);
            this.senderSocket.send(packet);
            List<Integer> serverIds = new ArrayList<>();
            serverIds.add(randomNumber);
            this.messagesNotACKED.add(messageID, serverIds);
        }       
        
        
        messagesHistory.add(messageID, signedMessage);
        this.messageID++;
    }

    public void sendCreateAccount(int port, PublicKey key) throws Exception{
        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        CREATE_MESSAGE message = new CREATE_MESSAGE(User.getid(), this.messageID, "localhost", port, key);
        
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, privKey);

        List<List<Integer>> newList = new ArrayList<>();
        newList.add(new ArrayList<>());
        newList.add(new ArrayList<>());
        transfers.put(0, newList);

        for(List<Object> server : User.getServers() ){
            InetAddress ip = InetAddress.getByName((String) server.get(0)); 
            int serverPort = (int) server.get(1);        
            DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, serverPort);
            this.senderSocket.send(packet);
        }
        List<Integer> serverIds = new ArrayList<>();
        for (int i = 0; i < User.getServers().size(); i++){
            serverIds.add(i);
        }
        messagesHistory.add(messageID, signedMessage);
        this.messagesNotACKED.add(messageID, serverIds);
        this.messageID++;
    }

    public void sendTransfer(String destUserID, int amount, PublicKey sourceKey, int port) throws Exception{
        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        PublicKey destinationPK = RSAKeyGenerator.readPublic("../Common/resources/U" + destUserID + "public.key");
        TRANSFER_MESSAGE message = new TRANSFER_MESSAGE(User.getid(), this.messageID, "localhost", port, sourceKey, destinationPK, amount, Integer.parseInt(destUserID));
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, privKey);
        
        List<List<Integer>> newList = new ArrayList<>();
        newList.add(new ArrayList<>());
        newList.add(new ArrayList<>());
        transfers.put(messageID, newList);

        for(List<Object> server : User.getServers() ){
            InetAddress ip = InetAddress.getByName((String) server.get(0)); 
            int serverPort = (int) server.get(1);        
            DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, serverPort);
            this.senderSocket.send(packet);
        }
        List<Integer> serverIds = new ArrayList<>();
        for (int i = 0; i < User.getServers().size(); i++){
            serverIds.add(i);
        }

        messagesHistory.add(messageID, signedMessage);
        this.messagesNotACKED.add(messageID, serverIds);
        this.messageID++;
    }

    public void sendMessagesAgain() throws Exception{
        for (int i = 0; i < messagesNotACKED.size(); i++){
            for (int j = 0; j < messagesNotACKED.get(i).size(); j++){
                List<Object> address = User.getServers().get(j);
                InetAddress ip = InetAddress.getByName((String)address.get(0));
                int port = (int)address.get(1);
                byte[] message = messagesHistory.get(i);
                DatagramPacket packet = new DatagramPacket(message, message.length, ip, port);
                this.senderSocket.send(packet);
            }
        }
    }
}