package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerFrontend {
    private DatagramSocket socket;
    private ServerIBFT serverIbtf;

    public ServerFrontend(int port, ServerIBFT ibtf) throws Exception{
        this.socket = new DatagramSocket(port);
        serverIbtf = ibtf;
    }

    public void listening() throws Exception{
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println(message);
        }
    }
}
