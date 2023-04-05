package hdl;

import java.security.PrivateKey;
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

import hdl.messages.RESPONSE_TRANSFER;
import hdl.messages.TRANSFER_MESSAGE;
import hdl.messages.ibtfMessage;

public class ServerIBFT {
    private List<String[]> requests = new ArrayList<>();
    private List<List<Object>> instances = new ArrayList<>();
    //[number da instance, [lista-prepares-recebidos], [lista-commits-recebidos], block, hora que começou, estado]
    private Blockchain blockchain; 
    private int quorum;
    private int currentInstance = 0;
    private int writtenInstance = -1;
    private Block block = new Block(Server.getid()); 
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

    public synchronized Block getBlock(){
        return this.block;
    }

    public synchronized void addOperation(TRANSFER_MESSAGE msg, byte[] signature) throws Exception{
        this.block.appendOperation(msg, signature);
        System.out.println("APPEND OP");
        if (this.block.getSize() == 3) {
            start(this.block);
            this.block.clearBlock();
        }
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda block
    public synchronized void receivedPrePrepare(ibtfMessage M) throws Exception{
        System.out.println("recebi preprepare");
        if (M.getServerId() != Server.getCurrentLeader()){
            System.out.println("Recebi um preprepare de um bizantino.");
            return;
        }

        if (!Server.validateBlock(M.getBlock())){
            System.out.println("Recebi um preprepare, mas o bloco tava adulterado.");
            return;
        }

        instances.add(Arrays.asList(M.getLambda(), new ArrayList<>(), new ArrayList<>(), M.getBlock(), LocalTime.now(), 0));

        for (List<Object> instance : instances){
            if (instance.get(0).equals(M.getLambda())){
                sendPrepare(M.getBlock(), M.getLambda());
                return;
            }
        }
    }

    //[number da instance, [lista-prepares-recebidos], [lista-commits-recebidos], block, hora que começou, estado] 0 1 2
    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void receivedPrepare(ibtfMessage M) throws Exception{
        System.out.println("recebi prepare");
        for (List<Object> instance : instances){
            if (instance.get(0).equals(M.getLambda())){
                if ((int) instance.get(5) == 0){
                    if (!((List<Integer>) instance.get(1)).contains(M.getServerId())){
                        ((List<Integer>) instance.get(1)).add(M.getServerId());
                        if (((List<Integer>) instance.get(1)).size() >= this.quorum){
                            sendCommit(M.getBlock(), M.getLambda());
                        }
                    }
                }
                return;
            }
        }
        List<Integer> newList = new ArrayList<>();
        newList.add(M.getServerId());
        instances.add(Arrays.asList(M.getLambda(), newList, new ArrayList<>(), M.getBlock(), LocalTime.now(), 0));
    }

    public synchronized void receivedCommit(ibtfMessage M) throws Exception{
        System.out.println("recebi commit");
        for (List<Object> instance : instances){
            if (instance.get(0).equals(M.getLambda())){
                if ((int) instance.get(5) == 0){
                    if (!((List<Integer>) instance.get(2)).contains(M.getServerId())){
                        ((List<Integer>) instance.get(2)).add(M.getServerId());
                        if (((List<Integer>) instance.get(2)).size() >= this.quorum){
                            instance.set(5, 1);
                            decide();
                        }
                    }
                }
                return;
            }
        }
        List<Integer> newList = new ArrayList<>();
        newList.add(M.getServerId());
        instances.add(Arrays.asList(M.getLambda(), new ArrayList<>(), newList, M.getBlock(), LocalTime.now(), 0));
    }

    // SERVER_ID MESSAGE_ID PREPARE lambda value
    public synchronized void sendPrepare(Block b, int lambda) throws Exception{
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        if (Server.getIsBizantine()){
            Random random = new Random();
            int randomNumber = random.nextInt(2);
            if (randomNumber == 0){
                System.out.println("Não enviei Prepare porque sou bizantino.");
                return;
            }
            else{
                System.out.println("Enviei Prepare adulterado.");
                Block b2 = new Block(Server.getid());
                ibtfMessage message = new ibtfMessage("PREPARE", Server.getid(), Server.getPerfectLink().getMessageId(), lambda, b2);
                byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
                byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
                Server.getPerfectLink().broadcast(signedMessage);
            }
        }
        else {
            System.out.println("Enviei Prepare");
            ibtfMessage message = new ibtfMessage("PREPARE", Server.getid(), Server.getPerfectLink().getMessageId(), lambda, b);
            byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
            byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
            Server.getPerfectLink().broadcast(signedMessage);
        }
    }

    // SERVER_ID MESSAGE_ID COMMIT lambda value
    public synchronized void sendCommit(Block b, int lambda) throws Exception{
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);
        if (Server.getIsBizantine()){
            Random random = new Random();
            int randomNumber = random.nextInt(2);
            if (randomNumber == 0){
                System.out.println("Não enviei Commit porque sou bizantino.");
                return;
            }
            else{
                System.out.println("Enviei Commit adulterado.");
                Block b2 = new Block(Server.getid());
                ibtfMessage message = new ibtfMessage("COMMIT", Server.getid(), Server.getPerfectLink().getMessageId(), lambda, b2);
                byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
                byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
                Server.getPerfectLink().broadcast(signedMessage);
            }
        }
        else {
            System.out.println("Enviei Commit");
            ibtfMessage message = new ibtfMessage("COMMIT", Server.getid(), Server.getPerfectLink().getMessageId(), lambda, b);
            byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
            byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);
            Server.getPerfectLink().broadcast(signedMessage);
        }
    }

    // SERVER_ID MESSAGE_ID PREPREPARE lambda value
    public synchronized void start(Block b) throws Exception{
        System.out.println("Fiz start");
        String keyPath = "resources/S" + Server.getid() + "private.key";
        PrivateKey key = RSAKeyGenerator.readPrivate(keyPath);

        ibtfMessage message = new ibtfMessage("PREPREPARE", Server.getid(), Server.getPerfectLink().getMessageId(), currentInstance, b);
        byte[] messageBytes = ByteArraysOperations.SerializeObject(message);
        byte[] signedMessage = ByteArraysOperations.signMessage(messageBytes, key);

        Server.getPerfectLink().broadcast(signedMessage);
        currentInstance++;        
    }
    
    public synchronized void decide() throws Exception{
        while(true){
            boolean nextDecidedOrAborted = false; 
            for (List<Object> instance : instances){
                if ((int) instance.get(0) == writtenInstance+1){
                    if ((int) instance.get(5) == 1){
                        blockchain.appendBlock((Block) instance.get(3));
                        processBlock((Block) instance.get(3));
                        writtenInstance = (int) instance.get(0);
                        nextDecidedOrAborted = true;
                        break;
                    }
                    if ((int) instance.get(5) == 2){
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

    public synchronized void processBlock(Block block) throws Exception{
        for (List<Object> o : block.getOperations()){
            TRANSFER_MESSAGE msg = (TRANSFER_MESSAGE) o.get(0);
            if (Server.transfer(msg.getSPK(), msg.getDPK(), msg.getAmount(), block.getServerId())){
                RESPONSE_TRANSFER message = new RESPONSE_TRANSFER(Server.getid(), Server.getPerfectLink().getMessageId(), msg.getMessageId(), msg.getDestUserId(), true, msg.getAmount());
                Server.getPerfectLink().sendMessage(msg.getIp(), msg.getPort(), message);
            }
            else{
                RESPONSE_TRANSFER message = new RESPONSE_TRANSFER(Server.getid(), Server.getPerfectLink().getMessageId(), msg.getMessageId(), msg.getDestUserId(), false, msg.getAmount());
                Server.getPerfectLink().sendMessage(msg.getIp(), msg.getPort(), message);
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