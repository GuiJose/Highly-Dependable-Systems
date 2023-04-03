package hdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerIBFT {
    private List<String[]> requests = new ArrayList<>();
    private List<List<Object>> instances = new ArrayList<>();
    //[number da instance, [lista-prepares-recebidos], [lista-commits-recebidos], block, hora que começou, estado]
    private Blockchain blockchain; 
    private int quorum;
    private int currentInstance = 0;
    private int writtenInstance = -1;
    private Block block = new Block(); 
    private final Lock lock = new ReentrantLock();


    public ServerIBFT(Blockchain b, int numServers) throws Exception{
        this.blockchain = b;
        int byzantineServersSuported = (int) Math.floor((numServers-1)/3);
        this.quorum = 2 * byzantineServersSuported + 1; 

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

    // SERVER_ID:MESSAGE_ID:PREPREPARE:lambda:block
    // SERVER_ID:MESSAGE_ID:PREPARE:lambda:block
    // SERVER_ID:MESSAGE_ID:COMMIT:lambda:block
    // USERID:MESSAGE_ID:ADD:string:ip:port 

    public synchronized void receivedMessage(DatagramPacket packet) throws Exception{
        lock.lock();
        String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        String[] data = message.split(":");
        if (data[2].equals("PREPREPARE")){System.out.println("recebi preprepare2"); receivedPrePrepare(data);}
        else if (data[2].equals("PREPARE")){receivedPrepare(data);}
        else if (data[2].equals("COMMIT")){receivedCommit(data);}
        else if (data[2].equals("ADD")){
            System.out.println("recebi pedido de operação");
            addOperation(packet);
        }
        lock.unlock();
    }
    
    public synchronized Block getBlock(){
        return this.block;
    }

    public synchronized void addOperation(DatagramPacket packet) throws Exception{
        this.block.appendOperation(packet.getData());
        System.out.println("APPEND OP");
        if (this.block.getSize() == 1) {
            start(this.block);
            this.block.clearBlock();
        }
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda block
    public synchronized void receivedPrePrepare(String[] data) throws Exception{
        System.out.println("recebi preprepare");
        if (Integer.parseInt(data[0]) != Server.getCurrentLeader()){
            System.out.println("Recebi um preprepare de um bizantino.");
            return;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data[4].getBytes());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Block b = (Block) ois.readObject();

        if (!Server.validateBlock(b)){
            System.out.println("Recebi um preprepare, mas o bloco tava adulterado.");
            return;
        }

        instances.add(Arrays.asList(Integer.parseInt(data[3]), new ArrayList<>(), new ArrayList<>(), b, LocalTime.now(), 0));

        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                sendPrepare(data[4], data[3]);
                return;
            }
        }
    }

    //[number da instance, [lista-prepares-recebidos], [lista-commits-recebidos], block, hora que começou, estado] 0 1 2
    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void receivedPrepare(String[] data) throws Exception{
        for (List<Object> instance : instances){
            if (instance.get(0).equals(Integer.parseInt(data[3]))){
                if ((int) instance.get(5) == 0){
                    if (!((List<String>) instance.get(1)).contains(data[0])){
                        ((List<String>) instance.get(1)).add(data[0]);
                        if (((List<String>) instance.get(1)).size() >= this.quorum){
                            sendCommit(data[4], data[3]);
                        }
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
                    if (!((List<String>) instance.get(2)).contains(data[0])){
                        ((List<String>) instance.get(2)).add(data[0]);
                        if (((List<String>) instance.get(2)).size() >= this.quorum){
                            instance.set(5, 1);
                            decide();
                        }
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
    public synchronized void sendPrepare(String b, String numInstance) throws Exception{
        if (Server.getIsBizantine()){
            Random random = new Random();
            int randomNumber = random.nextInt(2);
            if (randomNumber == 0){
                System.out.println("Não enviei Prepare porque sou bizantino.");
                return;
            }
            else{
                System.out.println("Enviei Prepare adulterado.");
                String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":PREPARE:" + numInstance + ":" + "Bloco_Adulterada";
                Server.getPerfectLink().broadcast(message.getBytes());
            }
        }
        else {
            System.out.println("Enviei Prepare");
            String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":PREPARE:" + numInstance + ":";
            
            // juntar o bloco à mensagem principal.
            byte[] messageBytes = message.getBytes();
            byte[] finalMessage = Arrays.copyOf(messageBytes, messageBytes.length + b.getBytes().length);
            System.arraycopy(b.getBytes(), 0, finalMessage, messageBytes.length, b.getBytes().length);

            Server.getPerfectLink().broadcast(finalMessage);
        }
    }

    // SERVER_ID MESSAGE_ID COMMIT lambda value
    public synchronized void sendCommit(String b, String numInstance) throws Exception{
        if (Server.getIsBizantine()){
            Random random = new Random();
            int randomNumber = random.nextInt(2);
            if (randomNumber == 0){
                System.out.println("Não enviei Commit porque sou bizantino.");
                return;
            }
            else{
                System.out.println("Enviei Commit adulterado.");
                String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":COMMIT:" + numInstance + ":" + "String_Adulterada";
                Server.getPerfectLink().broadcast(message.getBytes());
            }
        }
        else {
            System.out.println("Enviei Commit");
            String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":COMMIT:" + numInstance + ":";
            // juntar o bloco à mensagem principal.
            byte[] messageBytes = message.getBytes();
            byte[] finalMessage = Arrays.copyOf(messageBytes, messageBytes.length + b.getBytes().length);
            System.arraycopy(b.getBytes(), 0, finalMessage, messageBytes.length, b.getBytes().length);
            Server.getPerfectLink().broadcast(finalMessage);
        }
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    public synchronized void start(Block block2) throws Exception{
        System.out.println("Fiz start");

        //Serializar o Bloco.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(block2);
        byte[] data = baos.toByteArray();

        String message = Integer.toString(Server.getid()) + ":" + Integer.toString(Server.getPerfectLink().getMessageId()) + ":PREPREPARE:" + Integer.toString(currentInstance) + ":";

        // juntar o bloco à mensagem principal.

        byte[] messageBytes = message.getBytes();
        byte[] finalMessage = Arrays.copyOf(messageBytes, messageBytes.length + data.length);
        System.arraycopy(data, 0, finalMessage, messageBytes.length, data.length);

        System.out.println("enviei1");
        Server.getPerfectLink().broadcast(finalMessage);
        currentInstance++;        
    }
    
    public synchronized void decide() throws Exception{
        while(true){
            boolean nextDecidedOrAborted = false; 
            for (List<Object> instance : instances){
                if ((int) instance.get(0) == writtenInstance+1){
                    if ((int) instance.get(5) == 1){
                        //blockchain.appendString((String) instance.get(3));
                        if(Server.getIsMain()){
                            respondToUser((int)instance.get(0), 0);
                        }
                        writtenInstance = (int) instance.get(0);
                        nextDecidedOrAborted = true;
                        break;
                    }
                    if ((int) instance.get(5) == 2){
                        if(Server.getIsMain()){
                            respondToUser((int)instance.get(0), 1);
                        }
                        writtenInstance = (int) instance.get(0);
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

    public synchronized void respondToUser(int instance, int mode) throws Exception{
        System.out.println("Enviei resposta ao cliente.");
        for (String[] request: requests){
            if (Integer.parseInt(request[3]) == instance){
                if (mode == 0){
                    String message = "Your request for string: " + request[2] + " was appended at: " + LocalTime.now(); 
                    Server.getPerfectLink().sendMessage(request[0], Integer.parseInt(request[1]), message);
                }
                else{
                    String message = "It was not possible to attend to your request for string: " + request[2]; 
                    Server.getPerfectLink().sendMessage(request[0], Integer.parseInt(request[1]), message);
                }
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