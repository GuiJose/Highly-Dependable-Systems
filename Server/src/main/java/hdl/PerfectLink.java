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
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import hdl.messages.ACKUSER_MESSAGE;
import hdl.messages.ACK_MESSAGE;
import hdl.messages.CHECK_MESSAGE;
import hdl.messages.CREATE_MESSAGE;
import hdl.messages.RESPONSE;
import hdl.messages.SignatureRequest;
import hdl.messages.SignatureResponse;
import hdl.messages.TRANSFER_MESSAGE;
import hdl.messages.ibtfMessage;

public class PerfectLink extends Thread{
    private DatagramSocket receiverSocket;
    private DatagramSocket senderSocket;
    private ServerIBFT serverIbtf;

    private ConcurrentHashMap<Integer, List<Integer>> receivedMessagesFromUsers = new ConcurrentHashMap<>();
    private List<List<Object>> messagesToUsers = new ArrayList<>();
        //[[ip, port, byte[] da mensagem, message_ID]]
    private int messageToUsersId = 0;
    private List<Integer> messagesACKEDFromUsers = new ArrayList<>(); 

    private List<List<Integer>> receivedMessages;
    private List<List<Integer>> messagesNotACKED;
    private List<byte[]> messagesHistory;
    private int messageID = 0;

    private int counter = 0;

    public PerfectLink(int port, ServerIBFT ibtf, int numServers) throws Exception{
        this.receiverSocket = new DatagramSocket(port);
        this.senderSocket = new DatagramSocket();
        serverIbtf = ibtf;
        receivedMessages = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            List<Integer> l = new ArrayList<>();
            receivedMessages.add(l);
        }
        messagesNotACKED = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            List<Integer> l = new ArrayList<>();
            messagesNotACKED.add(l);
        }
        messagesHistory = new ArrayList<>();

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

    public synchronized void listening() throws Exception{    
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

            if (obj instanceof CREATE_MESSAGE){
                CREATE_MESSAGE M = (CREATE_MESSAGE) obj;
                if (DigitalSignature.VerifySignature(msg, signature, M.getKey())){
                    sendACKtoUser(M.getIp(), M.getPort(), M.getMessageId());
                    if (receivedMessagesFromUsers.containsKey(M.getId())){
                        if(!receivedMessagesFromUsers.get(M.getId()).contains(M.getMessageId())){
                            System.out.println("Received a CREATE_ACCOUNT request.");
                            Server.createAccount(M.getKey(), M.getPort(), M.getIp());
                        }
                    }
                    else{
                        System.out.println("Received a CREATE_ACCOUNT request.");
                        Server.createAccount(M.getKey(), M.getPort(), M.getIp());
                    }
                    receivedMessageFromUsers(M.getId(), M.getMessageId());
                }
            }
            else if (obj instanceof CHECK_MESSAGE){
                CHECK_MESSAGE M = (CHECK_MESSAGE) obj;
                if (DigitalSignature.VerifySignature(msg, signature, M.getKey())){
                    sendACKtoUser(M.getIp(), M.getPort(), M.getMessageId());
                    if (receivedMessagesFromUsers.containsKey(M.getId())){
                        if(!receivedMessagesFromUsers.get(M.getId()).contains(M.getMessageId())){
                            System.out.println("Received a CHECK request.");
                            Server.checkBalance(M);
                        }      
                    }
                    else {
                        System.out.println("Received a CHECK request.");
                        Server.checkBalance(M);
                    }
                    receivedMessageFromUsers(M.getId(), M.getMessageId());
                }
            }
            else if (obj instanceof TRANSFER_MESSAGE){
                TRANSFER_MESSAGE M = (TRANSFER_MESSAGE) obj;
                if (DigitalSignature.VerifySignature(msg, signature, M.getSPK())){
                    sendACKtoUser(M.getIp(), M.getPort(), M.getMessageId());
                    if (receivedMessagesFromUsers.containsKey(M.getUserId())){
                        if(!receivedMessagesFromUsers.get(M.getUserId()).contains(M.getMessageId())){
                            System.out.println("Received a TRANSFER request.");
                            if (Server.getIsMain()){
                                if (Server.validateTransfer(M.getSPK(), M.getDPK(), M.getAmount())){
                                    serverIbtf.addOperation(M, signature);
                                }
                                else{
                                    RESPONSE message = new RESPONSE(Server.getid(), this.messageToUsersId);
                                    message.setMessage("Error: Invalid Operation.");
                                    sendMessageToUser(M.getIp(), M.getPort(), message);
                                }
                            }
                        }
                    }
                    else{
                        System.out.println("Received a TRANSFER request.");
                        if (Server.getIsMain()){
                            if (Server.validateTransfer(M.getSPK(), M.getDPK(), M.getAmount())){
                                serverIbtf.addOperation(M, signature);
                            }
                            else{
                                RESPONSE message = new RESPONSE(Server.getid(), this.messageToUsersId);
                                message.setMessage("Error: Invalid Operation.");
                                sendMessageToUser(M.getIp(), M.getPort(), message);
                            }
                        }
                    }
                    receivedMessageFromUsers(M.getUserId(), M.getMessageId());
                }
            }
            else if (obj instanceof ibtfMessage){
                ibtfMessage M = (ibtfMessage) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    sendACK(M.getServerId(), M.getMessageId());
                    if (!receivedMessage(M.getServerId(), M.getMessageId())){
                        switch (M.getType()){
                            case "PREPREPARE":
                                serverIbtf.receivedPrePrepare(M);
                                break;
                            case "PREPARE":
                                serverIbtf.receivedPrepare(M);
                                break;
                            case "COMMIT":
                                serverIbtf.receivedCommit(M);
                                break;
                        }
                    }
                } 
            }
            else if (obj instanceof ACK_MESSAGE){
                ACK_MESSAGE M = (ACK_MESSAGE) obj;
                String keyPath = "../Common/resources/S" + M.getId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    receivedACK(M.getId(), M.getMessageACKED());
                }
            }
            else if (obj instanceof ACKUSER_MESSAGE){
                ACKUSER_MESSAGE M = (ACKUSER_MESSAGE) obj;
                String keyPath = "../Common/resources/U" + M.getId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    receivedACKFromUser(M.getMessageACKED());
                }
            }
            else if (obj instanceof SignatureRequest){
                SignatureRequest M = (SignatureRequest) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    sendACK(M.getServerId(), M.getMessageId());
                    byte[] sign = Server.getBlockchain().receivedSignatureRequest(M);
                    if (!(sign==null)){
                        sendSignatureResponse(sign, M.getBlockNumber(), M.getServerId());
                    }
                }
            }
            else if (obj instanceof SignatureResponse){
                SignatureResponse M = (SignatureResponse) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    sendACK(M.getServerId(), M.getMessageId());
                    Server.getBlockchain().receivedSignatureResponse(M);
                }
            }
        }
    }

    public synchronized void receivedACKFromUser(int messageACKED){
        messagesACKEDFromUsers.add(messageACKED);
    }

    public synchronized void receivedACK(int serverId, int messageACKED){
        messagesNotACKED.get(serverId).remove((Object)messageACKED);
    }

    public synchronized void receivedMessageFromUsers(int userId, int msgId){
        if (receivedMessagesFromUsers.containsKey(userId)){
            receivedMessagesFromUsers.get(userId).add(msgId);
        }
        else{
            List<Integer> newList = new ArrayList<>();
            newList.add(msgId);
            receivedMessagesFromUsers.put(userId, newList);            
        }
    }

    public synchronized boolean receivedMessage(int serverId, int msgId){
        if (!receivedMessages.get(serverId).contains(msgId)){
            receivedMessages.get(serverId).add(msgId);
            return false;
        }
        return true;
    }

    public synchronized void sendACKtoUser(String ip, int port, int msgId) throws Exception{
        InetAddress ip2 = InetAddress.getByName(ip);
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        ACK_MESSAGE message = new ACK_MESSAGE(Server.getid(), this.messageID , msgId);

        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);

        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip2, port);
        this.messageID++;
        this.senderSocket.send(packet);
    }
    
    public synchronized void sendACK(int ServerID, int msgId) throws Exception{
        List<Object> address = Server.getAddresses().get(ServerID);
        InetAddress ip = InetAddress.getByName((String)address.get(0));
        int port = (int)address.get(1);
        ACK_MESSAGE message = new ACK_MESSAGE(Server.getid(), this.messageID , msgId);

        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);

        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
        
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, port);
        this.senderSocket.send(packet);
    }

    public void sendMessagesAgain() throws Exception{
        for (int i = 0; i < messagesNotACKED.size(); i++){
            for (int j = 0; j < messagesNotACKED.get(i).size(); j++){
                List<Object> address = Server.getAddresses().get(i);
                InetAddress ip = InetAddress.getByName((String)address.get(0));
                int port = (int)address.get(1);
                byte[] buffer = messagesHistory.get(j);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
                this.senderSocket.send(packet);
            }
        }

        for (List<Object> o: messagesToUsers){
            if(!messagesACKEDFromUsers.contains(o.get(3))){
                InetAddress ip = InetAddress.getByName((String) o.get(0));
                int port = (int)o.get(1);
                byte[] buffer = (byte[]) o.get(2);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
                this.senderSocket.send(packet);
            }
        }

        //Send requests for digital signatures for special blocks.
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        int count = 0;
        for (Block b: Server.getBlockchain().getBlocks()){
            count++;
            if (b.getIsSpecial()){
                if (b.getSignatures().size() < Server.getQuorum()){
                    byte[] bytes = ByteArraysOperations.SerializeObject(b.getAccounts()); 
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(bytes);
                    SignatureRequest msg = new SignatureRequest(Server.getid(), this.messageID, hash, count);
                    byte[] msgBytes = ByteArraysOperations.SerializeObject(msg);
                    byte[] signedMessage = ByteArraysOperations.signMessage(msgBytes, key);
                    broadcast(signedMessage);                    
                }
            }
        }

        //Send tampered Prepreprares on the behalf of the leader.
        this.counter++;
        if (Server.getIsBizantine() && counter%20==0){
            System.out.println("I've sent a Preprepare pretending that i'm the server 0 who is the leader right now.");    
            Random random = new Random();
            ibtfMessage message = new ibtfMessage("PREPREPARE", 0, Server.getPerfectLink().getMessageId(), random.nextInt(20), new Block(0, false));
            byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
            byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
            broadcast(signedMessage);
            counter = 0;
        }
    }

    public void sendMessageToUser(String address, int port, Object message) throws Exception{
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
    
        InetAddress ip = InetAddress.getByName(address);     
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, port);
        
        List<Object> newList = new ArrayList<>();
        newList.add(address);
        newList.add(port); 
        newList.add(signedMessage); 
        newList.add(this.messageToUsersId); 

        this.messagesToUsers.add(newList);
        this.messageToUsersId++;
        this.senderSocket.send(packet);
    }

    public void sendSignatureResponse(byte[] signature, int blockNumber, int destServer) throws Exception{
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        
        SignatureResponse message = new SignatureResponse(Server.getid(), this.messageID, signature, blockNumber);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);

        this.messagesHistory.add(signedMessage);
        this.messageID++;

        InetAddress ip = InetAddress.getByName((String)Server.getAddresses().get(destServer).get(0));
        int port = (int)Server.getAddresses().get(destServer).get(1);       
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, port);
        this.senderSocket.send(packet);
    }

    public void broadcast(byte[] message) throws Exception{ 
        for (List<Integer> i : messagesNotACKED){
            i.add(this.messageID);
        }
        this.messagesHistory.add(message);
        this.messageID++;
        for (List<Object> address : Server.getAddresses()) {
            InetAddress ip = InetAddress.getByName((String)address.get(0));
            int port = (int)address.get(1);        
            DatagramPacket packet = new DatagramPacket(message, message.length, ip, port);
            this.senderSocket.send(packet);
        }
    }

    public int getMessageId(){
        return this.messageID;
    }
    public int getMessageToUsersId(){
        return this.messageToUsersId;
    }
}