package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.SecretKey;

public class PerfectLink extends Thread{
    private DatagramSocket receiverSocket;
    private DatagramSocket senderSocket;
    private ServerIBFT serverIbtf;
    private List<List<Integer>> receivedMessages;
    private List<List<Integer>> messagesNotACKED;
    private List<String> messagesHistory;
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
        timer.schedule(task, 0, 2000);
    }

    // SERVER_ID:MESSAGE_ID:PREPREPARE:lambda:value
    // SERVER_ID:MESSAGE_ID:PREPARE:lambda:value
    // SERVER_ID:MESSAGE_ID:COMMIT:lambda:value
    // SERVER_ID:ACK:ID_MESSAGE_ACKED
    // USERID:ADD:string:ip:port:MAC
    // USERID:BOOT:ip:port

    public synchronized void listening() throws Exception{    
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] data = message.split(":");

            if (data[1].equals("ADD")){
                if (Server.getIsMain()){
                    for(List<Object> key : Server.getKeys()){
                        if (((String)key.get(0)).equals(data[0])){
                            SecretKey userKey = (SecretKey)key.get(1);
                            byte[] iv = (byte[])key.get(2);

                            byte[] encryptedMac = new byte[48];
                            System.arraycopy(packet.getData(), packet.getLength()-48, encryptedMac, 0, 48); 
                            byte[] decryptedMac = SymetricKey.decrypt(encryptedMac, userKey, iv);

                            String message2 = data[0] + ":" + data[1] + ":" + data[2] + ":" + data[3] + ":" + data[4] + ":";
                            
                            byte[] macToVerify = HMAC.createHMAC(message2);
                            if (Arrays.equals(macToVerify, decryptedMac)){
                                serverIbtf.receivedMessage(packet);
                            }
                            break;
                        }
                    }
                }
            }
            else if(data[1].equals("BOOT")){
                if (Server.getIsMain()){
                    Server.generateUserKey(data[0], data[2], data[3]);
                }
            }
            else{
                String id = message.split(":")[0].split(":")[0];
                String keyPath = "../Common/resources/S" + id + "public.key";
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);

                byte[] signature = new byte[512];
                byte[] msg = new byte[packet.getLength()-512];
                System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512); 

                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    if (data[1].equals("ACK")){
                        receivedACK(Integer.parseInt(data[0]), Integer.parseInt(data[2]));
                    }
                    else{
                        boolean received = receivedMessage(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                        sendACK(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                        if (!received){
                            serverIbtf.receivedMessage(packet);
                        }
                    }
                }
            }
            packet.setLength(buffer.length); 
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

    public synchronized void sendACK(int ServerID, int msgID) throws Exception{
        List<Object> address = Server.getAddresses().get(ServerID);
        InetAddress ip = InetAddress.getByName((String)address.get(0));
        int port = (int)address.get(1);
        String message = Integer.toString(Server.getid()) + ":ACK:" + Integer.toString(msgID) + ":";
        byte[] buffer = message.getBytes();
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        byte[] signedMessage = DigitalSignature.CreateSignature(buffer, key);
        byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + signedMessage.length);
        System.arraycopy(signedMessage, 0, combinedMessage, buffer.length, signedMessage.length);
        DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
        this.senderSocket.send(packet);
    }

    public synchronized void sendMessagesAgain() throws Exception{
        for (int i = 0; i < messagesNotACKED.size(); i++){
            for (int j = 0; j < messagesNotACKED.get(i).size(); j++){
                List<Object> address = Server.getAddresses().get(i);
                InetAddress ip = InetAddress.getByName((String)address.get(0));
                int port = (int)address.get(1);
                String message = messagesHistory.get(j);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
                this.senderSocket.send(packet);
            }
        }
    }

    public synchronized void sendMessage(String address, int port, String message) throws Exception{
        byte[] mac = HMAC.createHMAC(message);
        byte[] buffer = message.getBytes();
        for (List<Object> x: Server.getKeys()){
            if (port == Integer.parseInt((String)x.get(4))){
                SecretKey key = (SecretKey) x.get(1);
                byte[] iv = (byte[]) x.get(2); 
                byte[] encryptMac = SymetricKey.encrypt(mac, key, iv);
                byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + encryptMac.length);
                System.arraycopy(encryptMac, 0, combinedMessage, buffer.length, encryptMac.length);
                InetAddress ip = InetAddress.getByName(address);     
                DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
                this.senderSocket.send(packet);
                break;
            }
        }
    }

    //BOOT:key:iv
    public synchronized void sendBootMessage(String id, String address, String port, SecretKey key, byte[] iv) throws Exception{
        String keyPath = "../Common/resources/U" + id + "public.key";
        PublicKey publicKey = RSAKeyGenerator.readPublic(keyPath);
        
        byte[] keyBytes = key.getEncoded();
        
        byte[] combinedMessage = Arrays.copyOf(keyBytes, keyBytes.length + iv.length);
        System.arraycopy(iv, 0, combinedMessage, keyBytes.length, iv.length);

        byte[] encryptedMessage = RSAKeyGenerator.encrypt(combinedMessage, publicKey);

        byte[] messageBytes = "BOOT:".getBytes();
        byte[] combinedMessage2 = Arrays.copyOf(messageBytes, messageBytes.length + encryptedMessage.length);
        System.arraycopy(encryptedMessage, 0, combinedMessage2, messageBytes.length, encryptedMessage.length);

        InetAddress ip = InetAddress.getByName(address);
        DatagramPacket packet = new DatagramPacket(combinedMessage2, combinedMessage2.length, ip, Integer.parseInt(port));
        this.senderSocket.send(packet);
    }

    public synchronized void broadcast(String message) throws Exception{
        message = message + ':';
        for (List<Integer> i : messagesNotACKED){
            i.add(this.messageID);
        }
        this.messagesHistory.add(message);
        this.messageID++;
        for (List<Object> address : Server.getAddresses()) {
            InetAddress ip = InetAddress.getByName((String)address.get(0));
            int port = (int)address.get(1);        
            byte[] buffer = message.getBytes();
            String keyPath = "resources/S" + Server.getid() + "private.key";
            PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
            byte[] signedMessage = DigitalSignature.CreateSignature(buffer, key);
            byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + signedMessage.length);
            System.arraycopy(signedMessage, 0, combinedMessage, buffer.length, signedMessage.length);
            DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
            this.senderSocket.send(packet);
        }
    }

    public int getMessageId(){
        return this.messageID;
    }
}
