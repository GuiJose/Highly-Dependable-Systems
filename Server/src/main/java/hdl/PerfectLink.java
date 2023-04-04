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

import hdl.messages.ACK_MESSAGE;
import hdl.messages.CHECK_MESSAGE;
import hdl.messages.CREATE_MESSAGE;
import hdl.messages.TRANSFER_MESSAGE;
import hdl.messages.ibtfMessage;

public class PerfectLink extends Thread{
    private DatagramSocket receiverSocket;
    private DatagramSocket senderSocket;
    private ServerIBFT serverIbtf;
    private List<List<Integer>> receivedMessages;
    private List<List<Integer>> messagesNotACKED;
    private List<byte[]> messagesHistory;
    private int messageID = 0;

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
        timer.schedule(task, 0, 3000);
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
                    System.out.println("RECEBI PEDIDO DE BOOT");
                    Server.createAccount(M.getKey(), M.getPort(), M.getIp());
                }
            }
            else if (obj instanceof CHECK_MESSAGE){
                CHECK_MESSAGE M = (CHECK_MESSAGE) obj;
                if (DigitalSignature.VerifySignature(msg, signature, M.getKey())){
                    sendACKtoUser(M.getIp(), M.getPort(), M.getMessageId());
                    System.out.println("RECEBI PEDIDO DE CHECK");
                    Server.checkBalance(M.getKey(), M.getPort(), M.getIp());
                }
            }
            else if (obj instanceof TRANSFER_MESSAGE){
                TRANSFER_MESSAGE M = (TRANSFER_MESSAGE) obj;
                if (DigitalSignature.VerifySignature(msg, signature, M.getSPK())){
                    sendACKtoUser(M.getIp(), M.getPort(), M.getMessageId());
                    System.out.println("RECEBI PEDIDO DE TRANSFER");
                    if (Server.getIsMain()){
                        if (Server.validateTransfer(M.getSPK(), M.getDPK(), M.getAmount())){
                            serverIbtf.addOperation(M, signature);
                        }
                        else{
                            sendMessage(M.getIp(), M.getPort(), "Error: Operação inválida.");
                        }
                    }
                }
            }
            else if (obj instanceof ibtfMessage){
                ibtfMessage M = (ibtfMessage) obj;
                String keyPath = "../Common/resources/S" + M.getServerId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    sendACK(M.getServerId(), M.getMessageId());
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
            else if (obj instanceof ACK_MESSAGE){
                ACK_MESSAGE M = (ACK_MESSAGE) obj;
                String keyPath = "../Common/resources/S" + M.getId() + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);
                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    receivedACK(M.getId(), M.getMessageACKED());
                }
            }
        }
    }

    public synchronized void receivedACK(int serverid, int messageACKED){
        messagesNotACKED.get(serverid).remove((Object)messageACKED);
    }

    public synchronized boolean receivedMessage(int serverId, int msgID){
        if (!receivedMessages.get(serverId).contains(msgID)){
            receivedMessages.get(serverId).add(msgID);
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
    }

    public void sendMessage(String address, int port, Object message) throws Exception{
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
    
        InetAddress ip = InetAddress.getByName(address);     
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, port);
        this.messageID++;
        this.senderSocket.send(packet);
    }

    public synchronized void broadcast(byte[] message) throws Exception{ 
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
}
