package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UserFrontend {
    private int port;
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private SecretKey key;
    private byte[] iv;
 
    public UserFrontend(int port) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);
        this.port = port; 
    }

    public void listening() throws Exception{
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] data = message.split(":", 2);

            if (data[0].equals("BOOT")){
                System.out.println("RECEBI A CHAVE");
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
            packet.setLength(buffer.length); 
        }
    }

    // USERID:ADD:string:ip:port 
    public void sendRequest(String message) throws Exception{
        String newMessage = Integer.toString(User.getid()) + ":ADD:" + message + ":localhost:" + Integer.toString(port); 
        for(List<Object> server : User.getServers()){
            InetAddress ip = InetAddress.getByName((String) server.get(0)); 
            int port = (int) server.get(1);        
            byte[] buffer = newMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            this.senderSocket.send(packet);
        }
    }
    public void sendBoot(String message) throws Exception{
        for(List<Object> server : User.getServers()){
            InetAddress ip = InetAddress.getByName((String) server.get(0)); 
            int port = (int) server.get(1);        
            byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
        this.senderSocket.send(packet);
        }
    }
}