package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UserFrontend {
    private int port;
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private SecretKey key;
    private byte[] iv;
    private int messageID = 0;
 
    public UserFrontend(int port) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);
        this.port = port; 
    }

    //CHECK:ammount:id:signature
    //BOOT:key+iv
    public void listening() throws Exception{
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] data = message.split(":", 4);

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
            if (data[0].equals("CHECK")){
                byte[] signature = new byte[512];
                byte[] msg = new byte[packet.getLength()-512];
                System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);

                String keyPath = "../Common/resources/S" + data[2] + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);


                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    System.out.println("Your current balance is : " + data[1]);
                }
                else{
                    System.out.println("ASSINATURA DEU MERDA");
                }
            }
            packet.setLength(buffer.length); 
        }
    }

    //MessageID:CHECK:ip:port:Assinatura
    public void sendCheck(int port, PublicKey key) throws Exception{
        String message = this.messageID + ":CHECK:localhost:" + port + ":"; 
        byte[] messageBytes = message.getBytes();
        byte[] keyBytes = key.getEncoded();
        byte[] combinedMessage = Arrays.copyOf(messageBytes, messageBytes.length + keyBytes.length);
        System.arraycopy(keyBytes, 0, combinedMessage, messageBytes.length, keyBytes.length);

        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        
        byte[] signature = DigitalSignature.CreateSignature(combinedMessage, privKey);
        byte[] signedMessage = Arrays.copyOf(combinedMessage, combinedMessage.length + signature.length);
        System.arraycopy(signature, 0, signedMessage, combinedMessage.length, signature.length);



        InetAddress ip = InetAddress.getByName((String) User.getServers().get(0).get(0)); 
        int leaderPort = (int) User.getServers().get(0).get(1);        
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, leaderPort);
        this.senderSocket.send(packet);
        this.messageID++;
    }

    //BOOT:ip:port:pubkey:Assinatura

    public void sendBoot(int port, PublicKey key) throws Exception{
        String message = "BOOT:localhost:" + port + ":";
        byte[] messageBytes = message.getBytes();
        byte[] keyBytes = key.getEncoded();
        byte[] combinedMessage = Arrays.copyOf(messageBytes, messageBytes.length + keyBytes.length);
        System.arraycopy(keyBytes, 0, combinedMessage, messageBytes.length, keyBytes.length);

        PrivateKey privKey = RSAKeyGenerator.readPrivate("resources/U" + User.getid() + "private.key");
        
        byte[] signature = DigitalSignature.CreateSignature(combinedMessage, privKey);
        byte[] signedMessage = Arrays.copyOf(combinedMessage, combinedMessage.length + signature.length);
        System.arraycopy(signature, 0, signedMessage, combinedMessage.length, signature.length);

        InetAddress ip = InetAddress.getByName((String) User.getServers().get(0).get(0)); 
        int leaderPort = (int) User.getServers().get(0).get(1);        
        DatagramPacket packet = new DatagramPacket(signedMessage, signedMessage.length, ip, leaderPort);
        this.senderSocket.send(packet);
    }
}