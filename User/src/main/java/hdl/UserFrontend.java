package hdl;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import hdl.messages.ACK_MESSAGE;
import hdl.messages.CHECK_MESSAGE;
import hdl.messages.CREATE_MESSAGE;
import hdl.messages.RESPONSE;
import hdl.messages.RESPONSE_TRANSFER;
import hdl.messages.TRANSFER_MESSAGE;

public class UserFrontend {
    private ConcurrentHashMap<Integer, List<List<Integer>>> transfers = new ConcurrentHashMap<>();
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private int messageID = 0;
    private List<List<Integer>> messagesNotACKED = new ArrayList<>();
    private List<byte[]> messagesHistory = new ArrayList<>();
    private int quorum;
    private int readTimeStamp = 0;
    
    public UserFrontend(int port, int numServers) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);
        int byzantineServersSuported = (int) Math.floor((numServers-1)/3);
        this.quorum = byzantineServersSuported + 1;
        System.out.println(this.quorum);

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
        byte[] buffer = new byte[4096];
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
                    System.out.println("Received message from server " + M.getServerId() + " with message: " + M.getMessage());
                }
            }

            else if (obj instanceof RESPONSE_TRANSFER){
                RESPONSE_TRANSFER M = (RESPONSE_TRANSFER) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    receivedResponseTransfer(M);
                }
            }

            packet.setLength(buffer.length); 
        }
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

    //MessageID:CHECK:ip:port:Assinatura
    public void sendCheck(int port, PublicKey key) throws Exception{
        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        CHECK_MESSAGE message = new CHECK_MESSAGE(User.getid(), this.messageID, "localhost", port, key);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, privKey);

        InetAddress ip = InetAddress.getByName((String) User.getServers().get(0).get(0)); 
        int leaderPort = (int) User.getServers().get(0).get(1);        
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, leaderPort);
        
        messagesHistory.add(messageID, signedMessage);
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(0);
        this.messagesNotACKED.add(messageID, serverIds);
        this.messageID++;
        this.senderSocket.send(packet);
    }

    //BOOT:ip:port:pubkey:Assinatura

    public void sendCreateAccount(int port, PublicKey key) throws Exception{
        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        CREATE_MESSAGE message = new CREATE_MESSAGE(User.getid(), this.messageID, "localhost", port, key);
        
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, privKey);

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

    // USER_ID:MESSAGE_ID:TRANSFER:ip:port:ammount:SPK|DPK|signature
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