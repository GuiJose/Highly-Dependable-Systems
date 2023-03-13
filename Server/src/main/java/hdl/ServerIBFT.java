package hdl;

import java.net.DatagramPacket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerIBFT {
    private List<String[]> requests = new ArrayList<>();
    //
    private List<List<Object>> instances = new ArrayList<>();
    //[number da instance, [lista-prepares-recebidos], [lista-commits-recebidos], string, hora que começou, estado] 0 1 2
    private Blockchain blockchain; 
    private int quorum;
    private int currentInstance = 0;
    private int writtenInstance = -1;
    private final Lock lock = new ReentrantLock();

    public ServerIBFT(Blockchain b, int numServers) throws Exception{
        this.blockchain = b;
        //this.quorum = (int) Math.floor(2 * ((numServers-1)/3) + 1);
        this.quorum = 3;
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

    // SERVER_ID:MESSAGE_ID:PREPREPARE:lambda:value
    // SERVER_ID:MESSAGE_ID:PREPARE:lambda:value
    // SERVER_ID:MESSAGE_ID:COMMIT:lambda:value
    // USERID:ADD:string:ip:port 

    public synchronized void receivedMessage(DatagramPacket packet) throws Exception{
        lock.lock();
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] data = message.split(":");
        if (data[2].equals("PREPREPARE")){receivedPrePrepare(data);}
        else if (data[2].equals("PREPARE")){receivedPrepare(data);}
        else if (data[2].equals("COMMIT")){receivedCommit(data);}
        else if (data[1].equals("ADD")){
            System.out.println("recebi pedido de add");
            String[] request = {data[3], data[4], data[2], Integer.toString(currentInstance)};
            requests.add(request);
            start(data[2]);
        }
        lock.unlock();
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    public synchronized void receivedPrePrepare(String[] data) throws Exception{
        System.out.println("recebi prepreare");
        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                sendPrepare(data[4], data[3]);
                return;
            }
        }
        //instances.add(Arrays.asList(Integer.parseInt(data[3]), new ArrayList<>(), new ArrayList<>(), data[4], LocalTime.now(), 0));
        sendPrepare(data[4], data[3]);
    }

    //[number da instance, [lista-prepares-recebidos], [lista-commits-recebidos], string, hora que começou, estado] 0 1 2
    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void receivedPrepare(String[] data) throws Exception{
        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                if ((int) instance.get(5) == 0){
                    ((List<String>) instance.get(1)).add(data[0]);
                    if (((List<String>) instance.get(1)).size() >= this.quorum){
                        sendCommit(data[4], data[3]);
                    }
                    
                }
                return;
            }
        }
        List<String> newList = new ArrayList<>();
        newList.add(data[0]);
        instances.add(Arrays.asList(Integer.parseInt(data[3]), newList, new ArrayList<>(), data[4], LocalTime.now(), 0));
    }

    public synchronized void receivedCommit(String[] data) throws Exception{
        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                if ((int) instance.get(5) == 0){
                    ((List<String>) instance.get(2)).add(data[0]);
                    if (((List<String>) instance.get(2)).size() >= this.quorum){
                        instance.set(5, 1);
                        decide();
                    }
                }
                return;
            }
        }
        List<String> newList = new ArrayList<>();
        newList.add(data[0]);
        instances.add(Arrays.asList(Integer.parseInt(data[3]), new ArrayList<>(), newList, data[4], LocalTime.now(), 0));
    }

    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void sendPrepare(String word, String numInstance) throws Exception{
        System.out.println("Enviei Prepare");
        String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":PREPARE:" + numInstance + ":" + word;
        Server.getPerfectLink().broadcast(message);
    }

    // SERVER_ID MESSAGE_ID COMMIT lambda value
    public synchronized void sendCommit(String word, String numInstance) throws Exception{
        System.out.println("Enviei Commit");
        String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":COMMIT:" + numInstance + ":" + word;
        Server.getPerfectLink().broadcast(message);
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    public synchronized void start(String word) throws Exception{
        System.out.println("Fiz start");
        String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":PREPREPARE:" + Integer.toString(currentInstance) + ":" + word;
        Server.getPerfectLink().broadcast(message);
        currentInstance++;        
    }
    public synchronized void decide() throws Exception{
        while(true){
            boolean nextDecidedOrAborted = false; 
            for (List<Object> instance : instances){
                if ((int) instance.get(0) == writtenInstance+1){
                    if ((int) instance.get(5) == 1){
                        blockchain.appendString((String) instance.get(3));
                        if(Server.getIsMain()){
                            respondToUser((int)instance.get(0));
                        }
                        writtenInstance = (int) instance.get(0);
                        nextDecidedOrAborted = true;
                        break;
                    }
                    if ((int) instance.get(5) == 2){
                        nextDecidedOrAborted = true;
                        break;
                    }
                }
            }
            if (!nextDecidedOrAborted){
                break;
            }
        }
    }

    public synchronized void respondToUser(int instance) throws Exception{
        System.out.println("Enviei resposta ao cliente.");
        for (String[] request: requests){
            if (Integer.parseInt(request[3]) == instance){
                String message = "Your request for string: " + request[2] + " was appended at: " + LocalTime.now(); 
                Server.getPerfectLink().sendMessage(request[0], Integer.parseInt(request[1]), message);
            } 
        }
    }

    public synchronized void checkExpiredInstances() throws Exception{
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