package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UserFrontend {
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private int messageID = 0;
    private List<List<Integer>> messagesNotACKED = new ArrayList<>();
    private List<byte[]> messagesHistory = new ArrayList<>();
    
    public UserFrontend(int port) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);

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

    //CHECK:ammount:id:signature
    //SERVER_ID:ACK:ID_MESSAGE_ACKED:signature
    //SERVERID:BOOT:SIGNATURE

    public void listening() throws Exception{
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            String[] data = message.split(":", 4);

            if (data[1].equals("BOOT")){
                byte[] signature = new byte[512];
                byte[] msg = new byte[packet.getLength()-512];
                System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);

                String keyPath = "../Common/resources/S" + data[0] + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);

                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    System.out.println("Your account was created in server: " + data[0]);
                }
            }
            else if (data[0].equals("CHECK")){
                byte[] signature = new byte[512];
                byte[] msg = new byte[packet.getLength()-512];
                System.arraycopy(packet.getData(), packet.getLength()-512, signature, 0, 512); 
                System.arraycopy(packet.getData(), 0, msg, 0, packet.getLength()-512);

                String keyPath = "../Common/resources/S" + data[2] + "public.key";
                PublicKey serverKey = RSAKeyGenerator.readPublic(keyPath);


                if (DigitalSignature.VerifySignature(msg, signature, serverKey)){
                    System.out.println("Your current balance is : " + data[1]);
                }
            }

            else if (data[1].equals("ACK")){
                receivedACK(Integer.parseInt(data[0]), Integer.parseInt(data[2]));
            }

            packet.setLength(buffer.length); 
        }
    }

    public synchronized void receivedACK(int serverid, int messageACKED){
        messagesNotACKED.get(messageACKED).remove(serverid);
    }

    //MessageID:CHECK:ip:port:Assinatura
    public void sendCheck(int port, PublicKey key) throws Exception{
        String message = User.getid() + ":" + this.messageID + ":CHECK:localhost:" + port + ":"; 
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
        
        messagesHistory.add(messageID, signedMessage);
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(0);
        this.messagesNotACKED.add(messageID, serverIds);
        this.messageID++;
        this.senderSocket.send(packet);
    }

    //BOOT:ip:port:pubkey:Assinatura

    public void sendBoot(int port, PublicKey key) throws Exception{
        String message = User.getid() + ":" + this.messageID + ":BOOT:localhost:" + port + ":";
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
                System.out.println("enviei");
                this.senderSocket.send(packet);
            }
        }
    }
}