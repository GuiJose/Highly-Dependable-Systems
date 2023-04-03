package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
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
    private List<byte[]> messagesHistory;
    private int messageToServersID = 0;

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

    // SERVER_ID:MESSAGE_ID:PREPREPARE:lambda:value:signature
    // SERVER_ID:MESSAGE_ID:PREPARE:lambda:value:signature
    // SERVER_ID:MESSAGE_ID:COMMIT:lambda:value:signature
    // SERVER_ID:ACK:ID_MESSAGE_ACKED:signature
    // USER_ID:MESSAGE_ID:CHECK:ip:port:signature
    // USER_ID:MESSAGE_ID:BOOT:ip:port:signature
    // USER_ID:MESSAGE_ID:TRANSFER:ip:port:ammount:SPK|DPK|signature

    public synchronized void listening() throws Exception{    
        byte[] buffer = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] data = message.split(":", 7);
            System.out.println("recebi preprepare20");
            if (data[2].equals("TRANSFER")){
                sendACKtoUser(data[3], Integer.parseInt(data[4]), data[1]);
                if (Server.getIsMain()){
                    byte[] signature = new byte[512];
                    byte[] destinationKeyBytes = new byte[550];
                    byte[] sourceKeyBytes = new byte[550];
                    byte[] msg = new byte[packet.getLength()-512];
                    System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                    System.arraycopy(packet.getData(), packet.getLength()-1062, destinationKeyBytes, 0, 550);
                    System.arraycopy(packet.getData(), packet.getLength()-1612, sourceKeyBytes, 0, 550);
                    System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);
                    
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    X509EncodedKeySpec sourceKeySpec = new X509EncodedKeySpec(sourceKeyBytes);
                    PublicKey sourcePK = keyFactory.generatePublic(sourceKeySpec);
                    X509EncodedKeySpec destinationKeySpec = new X509EncodedKeySpec(destinationKeyBytes);
                    PublicKey destinationPK = keyFactory.generatePublic(destinationKeySpec);

                    if (DigitalSignature.VerifySignature(msg, signature, sourcePK)){
                        if (Server.validateTransfer(sourcePK, destinationPK, Integer.parseInt(data[5]))){
                            serverIbtf.addOperation(packet);
                        }
                        else{
                            System.out.println("OPERAÇAO INVALIDA");
                        }
                    }


                }
            }
            else if (data[2].equals("CHECK")){
                if (Server.getIsMain()){
                    byte[] signature = new byte[512];
                    byte[] keyBytes = new byte[550];
                    byte[] msg = new byte[packet.getLength()-512];
                    System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                    System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);
                    System.arraycopy(packet.getData(), packet.getLength()-1062, keyBytes, 0, 550);  

                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
                    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

                    if (DigitalSignature.VerifySignature(msg, signature, publicKey)){
                        sendACKtoUser(data[3], Integer.parseInt(data[4]), data[1]);
                        Server.checkBalance(publicKey,Integer.parseInt(data[4]));
                    }
                }
            }
            else if(data[2].equals("BOOT")){
                System.out.println("boot");
                byte[] signature = new byte[512];
                byte[] keyBytes = new byte[550];
                byte[] msg = new byte[packet.getLength()-512];
                System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);
                System.arraycopy(packet.getData(), packet.getLength()-1062, keyBytes, 0, 550);  

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                
                if (DigitalSignature.VerifySignature(msg, signature, publicKey)){
                    sendACKtoUser(data[3], Integer.parseInt(data[4]), data[1]);
                    Server.createAccount(publicKey, Integer.parseInt(data[4]));
                }
            }
            else{
                System.out.println("recebi preprepare4");
                String id = message.split(":")[0].split(":")[0];
                System.out.println(id);
                String keyPath = "../Common/resources/S" + id + "public.key";
                System.out.println(keyPath);
                PublicKey key = RSAKeyGenerator.readPublic(keyPath);

                byte[] signature = new byte[512];
                byte[] msg = new byte[packet.getLength()-512];
                System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512); 

                if (DigitalSignature.VerifySignature(msg, signature, key)){
                    System.out.println("recebi preprepare9");
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

    public synchronized void sendACKtoUser(String ip, int port, String msgId) throws Exception{
        InetAddress ip2 = InetAddress.getByName(ip);
        String message = Integer.toString(Server.getid()) + ":ACK:" + msgId + ":";
        byte[] buffer = message.getBytes();
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        byte[] signedMessage = DigitalSignature.CreateSignature(buffer, key);
        byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + signedMessage.length);
        System.arraycopy(signedMessage, 0, combinedMessage, buffer.length, signedMessage.length);
        DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip2, port);
        this.senderSocket.send(packet);
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
                byte[] buffer = messagesHistory.get(j);
                String keyPath = "resources/S" + Server.getid() + "private.key";
                PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
                byte[] signedMessage = DigitalSignature.CreateSignature(buffer, key);
                byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + signedMessage.length);
                System.arraycopy(signedMessage, 0, combinedMessage, buffer.length, signedMessage.length);
                DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
                this.senderSocket.send(packet);
            }
        }
    }

    public void sendMessage(String address, int port, String message) throws Exception{
        byte[] buffer = message.getBytes();
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        byte[] signature = DigitalSignature.CreateSignature(buffer, key);
        byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + signature.length);
        System.arraycopy(signature, 0, combinedMessage, buffer.length, signature.length);
        InetAddress ip = InetAddress.getByName(address);     
        DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
        this.senderSocket.send(packet);
        
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

    public synchronized void broadcast(byte[] b) throws Exception{
        
        String concatenatedString = new String(b, StandardCharsets.UTF_8) + ":";
        byte[] buffer = concatenatedString.getBytes(StandardCharsets.UTF_8);
        
        for (List<Integer> i : messagesNotACKED){
            i.add(this.messageToServersID);
        }
        this.messagesHistory.add(buffer);
        this.messageToServersID++;

        for (List<Object> address : Server.getAddresses()) {
            InetAddress ip = InetAddress.getByName((String)address.get(0));
            int port = (int)address.get(1);        
            String keyPath = "resources/S" + Server.getid() + "private.key";
            PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
            byte[] signedMessage = DigitalSignature.CreateSignature(buffer, key);
            byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + signedMessage.length);
            System.arraycopy(signedMessage, 0, combinedMessage, buffer.length, signedMessage.length);
            DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
            System.out.println("enviei2");
            this.senderSocket.send(packet);
        }
    }

    public int getMessageId(){
        return this.messageToServersID;
    }
}
