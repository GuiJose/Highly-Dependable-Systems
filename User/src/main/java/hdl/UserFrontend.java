package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UserFrontend {
    private DatagramSocket socket;

    public UserFrontend(int port) throws Exception{
        this.socket = new DatagramSocket(port);
    }

    public void listening() throws Exception{
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received message: " + message);
        }
    }
}
