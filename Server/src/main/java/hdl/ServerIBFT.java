package hdl;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class ServerIBFT {
    private List<String[]> requests = new ArrayList<>();
    private Blockchain blockchain; 
    private int instanceConsensus = 0;
    private int round = 1;
    private int pr = 0;
    private String pv = null;
    private String inputValue = null;

    public ServerIBFT(Blockchain b) throws Exception{
        blockchain = b;
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda round value
    // SERVER_ID MESSAGE_ID PREPARE lambda round value
    // SERVER_ID MESSAGE_ID COMMIT lambda round value
    // SERVER_ID MESSAGE_ID ACK ID_MESSAGE_ACKED
    // ADD string

    public synchronized void receivedMessage(DatagramPacket packet){
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] data = message.split(" ");

        if (data[0].equals("PREPREPARE")){receivedPrePrepare(data);}
        else if (data[0].equals("PREPARE")){receivedPrepare(data);}
        else if (data[0].equals("COMMIT")){receivedCommit(data);}
        else if (data[0].equals("ADD")){
            if (Server.getIsMain()){
                String address = packet.getAddress().getHostAddress();
                int port = packet.getPort();
                String[] request = {address, Integer.toString(port), data[1]};
                requests.add(request);
                start(data);
            }
        }
    }

    public synchronized void receivedPrePrepare(String[] data){
    }
    public synchronized void receivedPrepare(String[] data){
    }
    public synchronized void receivedCommit(String[] data){
    }
    public synchronized void doPrepare(){
    }
    public synchronized void doCommit(){
    }
    public synchronized void start(String[] data){
    }
    public synchronized void decide(String word){
        blockchain.appendString(word);
    }
}