package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class ServerIBFT {
    private DatagramSocket senderSocket;
    private Blockchain blockchain; 

    public ServerIBFT(Blockchain b) throws Exception{
        senderSocket = new DatagramSocket();
        blockchain = b;
    }


    public void receivedMessage(String message){

    }


    public void broadcast(String message) throws Exception{
        for (List<Object> address : Server.getAddresses()) {
            InetAddress ip = InetAddress.getByName((String)address.get(0));
            int port = (int)address.get(1);        
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            this.senderSocket.send(packet);
        }
    }
}
