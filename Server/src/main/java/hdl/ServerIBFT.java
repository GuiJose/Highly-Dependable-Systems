package hdl;

import java.net.DatagramPacket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerIBFT {
    private List<String[]> requests = new ArrayList<>();
    private List<List<Object>> instances = new ArrayList<>();
    private PerfectLink perfectLink;
    private Blockchain blockchain; 
    private int quorum;
    private int currentInstance = 0;
    private int writtenInstance = -1;

    public ServerIBFT(Blockchain b, int numServers, PerfectLink perfectLink) throws Exception{
        this.blockchain = b;
        this.quorum = (int) Math.floor(2 * ((numServers-1)/3) + 1);
        this.perfectLink = perfectLink;

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    checkExpiredInstances();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 0, 15000);
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    // SERVER_ID MESSAGE_ID PREPARE lambda value
    // SERVER_ID MESSAGE_ID COMMIT lambda value
    // ADD string

    public synchronized void receivedMessage(DatagramPacket packet) throws Exception{
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] data = message.split(" ");

        if (data[2].equals("PREPREPARE")){receivedPrePrepare(data);}
        else if (data[2].equals("PREPARE")){receivedPrepare(data);}
        else if (data[2].equals("COMMIT")){receivedCommit(data);}
        else if (data[0].equals("ADD")){
            if (Server.getIsMain()){
                String address = packet.getAddress().getHostAddress();
                int port = packet.getPort();
                String[] request = {address, Integer.toString(port), data[1], Integer.toString(currentInstance)};
                requests.add(request);
                start(data[1]);
            }
        }
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    public synchronized void receivedPrePrepare(String[] data) throws Exception{
        instances.add(Arrays.asList(Integer.parseInt(data[3]), new ArrayList<>(), new ArrayList<>(), data[4], LocalTime.now(), 0));
        sendPrepare(data[4], data[3]);
    }

    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void receivedPrepare(String[] data) throws Exception{
        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                if ((int) instance.get(5) == 0){
                    ((List<String>) instance.get(1)).add(data[0]);
                    if (((List<String>) instance.get(1)).size() >= this.quorum){
                        sendCommit(data[4], data[3]);
                    }
                    break;
                }
            }
        }
    }

    public synchronized void receivedCommit(String[] data){
        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                if ((int) instance.get(5) == 0){
                    ((List<String>) instance.get(2)).add(data[0]);
                    if (((List<String>) instance.get(2)).size() >= this.quorum){
                        instance.set(5, 1);
                        decide();
                    }
                    break;
                }
            }
        }
    }

    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void sendPrepare(String word, String numInstance) throws Exception{
        String message = Integer.toString(Server.getid()) + " " + Integer.toString(perfectLink.getMessageId()) + " PREPARE " + numInstance + " " + word;
        perfectLink.broadcast(message);
    }

    // SERVER_ID MESSAGE_ID COMMIT lambda value
    public synchronized void sendCommit(String word, String numInstance) throws Exception{
        String message = Integer.toString(Server.getid()) + " " + Integer.toString(perfectLink.getMessageId()) + " COMMIT " + numInstance + " " + word;
        perfectLink.broadcast(message);
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    public synchronized void start(String word) throws Exception{
        String message = Integer.toString(Server.getid()) + " " + Integer.toString(perfectLink.getMessageId()) + " PREPREPARE " + Integer.toString(currentInstance) + " " + word;
        perfectLink.broadcast(message);
        currentInstance++;
    }
    public synchronized void decide(){
        while(true){
            boolean nextDecidedOrAborted = false; 
            for (List<Object> instance : instances){
                if ((int) instance.get(0) == writtenInstance+1){
                    if ((int) instance.get(5) == 1){
                        blockchain.appendString((String) instance.get(3));
                        nextDecidedOrAborted = true;
                    }
                    if ((int) instance.get(5) == 2){
                        nextDecidedOrAborted = true;
                    }
                }
            }
            if (!nextDecidedOrAborted){
                break;
            }
        }
    }

    public void checkExpiredInstances(){
        LocalTime time = LocalTime.now(); 

        for (List<Object> instance : instances){
            Duration duration = Duration.between(time, (LocalTime) instance.get(4));
            if (duration.toSeconds() > 30 ){
                if ((int) instance.get(5) == 0){
                    instance.set(5, 2);
                }
            }
        }
        decide();
    }
}