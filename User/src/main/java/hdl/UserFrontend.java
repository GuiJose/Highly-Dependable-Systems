package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UserFrontend {
    private int port;
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private SecretKey key;
    private byte[] iv;
    private int messageID = 0;
    private List<Integer> receivedACKs = new ArrayList<>();
    private List<byte[]> messagesHistory = new ArrayList<>();
    private List<Object> receivedMessagesFromServer = new ArrayList<>();
    //[Server_id, Message_id]
 
    public UserFrontend(int port) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);
        this.port = port; 

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

    //BOOT:key+iv
    public void listening() throws Exception{
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] data = message.split(":", 2);
            if (data[0].equals("BOOT")){
                String keyPath = "resources/U" + User.getid() + "private.key";
                PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);

                byte[] encryptedPart = Arrays.copyOfRange(packet.getData(), 5, packet.getLength());
                byte[] decrypted = RSAKeyGenerator.decrypt(encryptedPart, key);
                byte[] decryptedKey = new byte[32];
                byte[] iv = new byte[16];
                
                System.arraycopy(decrypted, 0, decryptedKey, 0, 32); 
                System.arraycopy(decrypted, decrypted.length-16, iv, 0, 16); 

                this.iv = iv;
                this.key = new SecretKeySpec(decryptedKey, "AES");
            }
            if (message.split(" ")[0].equals("Your")){
                byte[] encryptedMac = new byte[48];
                byte[] msg = new byte[packet.getLength()-48];
                System.arraycopy(packet.getData(), packet.getLength()-48, encryptedMac, 0, 48); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-48);
                byte[] decryptedMac = SymetricKey.decrypt(encryptedMac, this.key, this.iv);

                byte[] macToVerify = HMAC.createHMAC(new String(msg));
                if (Arrays.equals(macToVerify, decryptedMac)){
                    System.out.println(new String(msg));
                }
            }
            packet.setLength(buffer.length); 
        }
    }

    // USERID:ADD:string:ip:port:MAC 
    public void sendRequest(String message) throws Exception{
        String newMessage = Integer.toString(User.getid()) + ":" + Integer.toString(messageID) + ":ADD:" + message + ":localhost:" + Integer.toString(port) + ":"; 
        byte[] mac = HMAC.createHMAC(newMessage);
        byte[] buffer = newMessage.getBytes();
        byte[] encryptMac = SymetricKey.encrypt(mac, this.key, this.iv);
        byte[] combinedMessage = Arrays.copyOf(buffer, buffer.length + encryptMac.length);
        System.arraycopy(encryptMac, 0, combinedMessage, buffer.length, encryptMac.length);
        this.messagesHistory.add(combinedMessage);
        InetAddress ip = InetAddress.getByName((String) User.getServers().get(0).get(0)); 
        int port = (int) User.getServers().get(0).get(1);        
        DatagramPacket packet = new DatagramPacket(combinedMessage, combinedMessage.length, ip, port);
        this.senderSocket.send(packet);
        this.messageID++;
    }

    public void sendBoot(String message) throws Exception{
        this.messageID++;
        InetAddress ip = InetAddress.getByName((String) User.getServers().get(0).get(0)); 
        int port = (int) User.getServers().get(0).get(1);        
        byte[] buffer = message.getBytes();
        this.messagesHistory.add(buffer);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
        this.senderSocket.send(packet);
    }

    public void sendMessagesAgain() throws Exception{
        for (int i = 0; i < messagesHistory.size(); i++){
            if(!receivedACKs.contains(i)){
                InetAddress ip = InetAddress.getByName((String) User.getServers().get(0).get(0)); 
                int port = (int) User.getServers().get(0).get(1);
                DatagramPacket packet = new DatagramPacket(messagesHistory.get(i), messagesHistory.get(i).length, ip, port);
                this.senderSocket.send(packet);
            }
        }
    }
}