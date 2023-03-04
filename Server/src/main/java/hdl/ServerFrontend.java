package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class ServerFrontend {
    private DatagramSocket socket;

    public ServerFrontend(int port) throws Exception{
        this.socket = new DatagramSocket(port);
    }

    public void listening() throws Exception{
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            
            if( !Server.HasMessage(message)){
                Server.AddMessage(message);
                byte[] messageBytes =  message.getBytes();

                for (List<Object> add : Server.getAddresses()){
                    InetAddress ip = InetAddress.getByName((String)add.get(0));
                    int port = (int)add.get(1); 
                    DatagramPacket broadcastPacket = new DatagramPacket(messageBytes, messageBytes.length, ip, port);
                    socket.send(broadcastPacket);
                } 
            }

        }
        
    }
}
