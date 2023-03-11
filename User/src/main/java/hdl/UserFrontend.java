package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UserFrontend {
    private int port;
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
 
    public UserFrontend(int port) throws Exception{
        this.senderSocket = new DatagramSocket();
        this.receiverSocket = new DatagramSocket(port);
        this.port = port;
    }

    public void listening() throws Exception{
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received message: " + message);
        }
    }

    // USERID:ADD:string:ip:port 
    public void sendRequest(String message) throws Exception{
        String newMessage = Integer.toString(User.getid()) + ":ADD:" + message + ":localhost:" + Integer.toString(port); 
        InetAddress ip = InetAddress.getByName((String)User.getServers().get(0).get(0)); 
        int port = (int) User.getServers().get(0).get(1);        
        byte[] buffer = newMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
        this.senderSocket.send(packet);
    }
}
