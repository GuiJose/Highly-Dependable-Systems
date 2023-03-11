package hdl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PerfectLink extends Thread{
    private DatagramSocket receiverSocket;
    private DatagramSocket senderSocket;
    private ServerIBFT serverIbtf;
    private DatagramPacket packet;
    private List<List<Integer>> receivedMessages;
    private List<List<Integer>> messagesNotACKED;
    private List<String> messagesHistory;
    private int messageID = 0;

    public void run(){ 
        try {
            serverIbtf.receivedMessage(this.packet);
            System.out.println("A thread começou");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PerfectLink(DatagramPacket packet2, ServerIBFT serverIbtf2){
        serverIbtf = serverIbtf2;
        packet = packet2;
    }

    public PerfectLink(int port, ServerIBFT ibtf, int numServers) throws Exception{
        this.receiverSocket = new DatagramSocket(port);
        this.senderSocket = new DatagramSocket();
        serverIbtf = ibtf;
        receivedMessages = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            List<Integer> l = new ArrayList<>();
            receivedMessages.add(l);
        }
        messagesNotACKED = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            List<Integer> l = new ArrayList<>();
            messagesNotACKED.add(l);
        }
        messagesHistory = new ArrayList<>();

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

    // SERVER_ID:MESSAGE_ID:PREPREPARE:lambda:value
    // SERVER_ID:MESSAGE_ID:PREPARE:lambda:value
    // SERVER_ID:MESSAGE_ID:COMMIT:lambda:value
    // SERVER_ID:ACK:ID_MESSAGE_ACKED
    // USERID:ADD:string:ip:port 

    public void listening() throws Exception{    
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while(true){
            receiverSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("ReceivedMessage:" + message);
            String[] data = message.split(":");
            if (data[1].equals("ACK")){
                receivedACK(Integer.parseInt(data[0]), Integer.parseInt(data[2]));
            }
            else if (data[1].equals("ADD")){
                PerfectLink thread = new PerfectLink(packet, this.serverIbtf);
                thread.start();
            }
            else{
                boolean received = receivedMessage(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                sendACK(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                if (!received){
                    PerfectLink thread = new PerfectLink(packet, this.serverIbtf);
                    thread.start();
                }
            }
            packet.setLength(buffer.length); 
        }
    }

    public void receivedACK(int serverid, int messageACKED){
        messagesNotACKED.get(serverid).remove((Object)messageACKED);
    }

    public boolean receivedMessage(int serverId, int msgID){
        if (!receivedMessages.get(serverId).contains(msgID)){
            receivedMessages.get(serverId).add(msgID);
            return false;
        }
        return true;
    }

    public void sendACK(int ServerID, int msgID) throws Exception{
        List<Object> address = Server.getAddresses().get(ServerID);
        InetAddress ip = InetAddress.getByName((String)address.get(0));
        int port = (int)address.get(1);
        String message = Integer.toString(Server.getid()) + ":ACK:" + Integer.toString(msgID);
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
        this.senderSocket.send(packet);
    }

    public void sendMessagesAgain() throws Exception{
        for (int i = 0; i < messagesNotACKED.size(); i++){
            for (int j = 0; j < messagesNotACKED.get(i).size(); j++){
                List<Object> address = Server.getAddresses().get(i);
                InetAddress ip = InetAddress.getByName((String)address.get(0));
                int port = (int)address.get(1);
                String message = messagesHistory.get(j);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
                this.senderSocket.send(packet);
            }
        }
    }

    public void broadcast(String message) throws Exception{
        for (List<Integer> i : messagesNotACKED){
            i.add(messageID);
        }
        messagesHistory.add(message);
        messageID++;
        for (List<Object> address : Server.getAddresses()) {
            InetAddress ip = InetAddress.getByName((String)address.get(0));
            int port = (int)address.get(1);        
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            this.senderSocket.send(packet);
        }
    }

    public int getMessageId(){
        return this.messageID;
    }
}
