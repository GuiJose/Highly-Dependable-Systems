package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerFrontend extends Thread{
    private DatagramSocket socket;
    private ServerIBFT serverIbtf;

    public void run(String message){ 
        try {
            serverIbtf.receivedMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerFrontend(DatagramSocket socket2, ServerIBFT ibtf2){
        socket = socket2;
        serverIbtf = ibtf2;
    }

    public ServerFrontend(int port, ServerIBFT ibtf) throws Exception{
        this.socket = new DatagramSocket(port);
        serverIbtf = ibtf;
    }

    // PRE-PREPARE lambda round value
    // PREPARE lambda round value
    // COMMIT lambda round value
    // ADD string

    public void listening() throws Exception{
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while(true){
            socket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            ServerFrontend thread = new ServerFrontend(socket, serverIbtf);
            //thread.start(message);
            }
    }
}
