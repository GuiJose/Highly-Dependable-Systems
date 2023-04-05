package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import hdl.messages.CHECK_MESSAGE;
import hdl.messages.RESPONSE;
import hdl.messages.RESPONSE_CHECK;
import hdl.messages.TRANSFER_MESSAGE;

public class Server extends Thread{
    private static int id;
    private static boolean isMain = false;
    private static List<List<Object>> addresses = new ArrayList<>();
    private static int numServers;
    private static Blockchain blockchain = new Blockchain();
    private static PerfectLink perfectLink;
    private static ServerIBFT ibtf;
    private static ConcurrentHashMap<PublicKey, int[]> accounts = new ConcurrentHashMap<>();
    private static List<List<Object>> keys = new ArrayList<>(); 
    // Object = [id, key, iv, address, port]
    private static int currentLeader = 0; 
    private static boolean isBizantine = false;
    public void run(){ 
        try {
            perfectLink.listening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]) throws Exception{
        id = Integer.parseInt(args[0]);
        if (id == 0){isMain = true;}
        readConfiguration();
        RSAKeyGenerator.write(id,"s");
        numServers = addresses.size();

        if (args[1].equals("1")){
            isBizantine = true;
        }        
        ibtf = new ServerIBFT(blockchain, numServers);
        perfectLink = new PerfectLink((int)(addresses.get(id).get(1)), ibtf, numServers);
        Server thread = new Server();
        thread.start();

        while(true){
            Scanner sc= new Scanner(System.in); 
            System.out.print("Enter '1' to print the ledger:");  
            int a = sc.nextInt();
            if (a == 1){
                System.out.println(accounts.size());
                accounts.forEach((key, value) -> {
                    System.out.println(value[0]);
                });
            }  
        }
    }

    private static void readConfiguration(){
        try {
            File file = new File("../Common/Sconfiguration.txt");
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
              String line = reader.nextLine();
              String[] data = line.split(" ");
              addresses.add(Arrays.asList(data[1], Integer.parseInt(data[2])));
            }
            reader.close();
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
    }

    public static List<List<Object>> getAddresses(){
        return addresses;
    }

    public static Boolean getIsMain(){
        return isMain;
    }
    public static Boolean getIsBizantine(){
        return isBizantine;
    }

    public static int getid(){
        return id;
    }

    public static List<List<Object>> getKeys(){
        return keys;
    }

    public static PerfectLink getPerfectLink(){
        return perfectLink;
    }

    public static int getCurrentLeader(){
        return currentLeader;
    }

    public static void createAccount(PublicKey pubKey, int port, String ip) throws Exception{
        int[] array = {100, 0};
        if (!accounts.containsKey(pubKey)){
            accounts.put(pubKey, array);
            RESPONSE msg = new RESPONSE(Server.getid(), perfectLink.getMessageId());
            msg.setMessage("Account was created!");
            perfectLink.sendMessage(ip, port, msg);
        }
    }

    public static void checkBalance(CHECK_MESSAGE M) throws Exception{
        if(accounts.containsKey(M.getKey())){
            int ammount = accounts.get(M.getKey())[0];
            int writeTimeStamp = accounts.get(M.getKey())[1];
            RESPONSE_CHECK msg = new RESPONSE_CHECK(Server.getid(), perfectLink.getMessageId(), ammount, writeTimeStamp, M.getMessageId());
            perfectLink.sendMessage(M.getIp(), M.getPort(), msg);
        }
        else{
            RESPONSE msg = new RESPONSE(Server.getid(), perfectLink.getMessageId());
            msg.setMessage("Account does not exist.");
            perfectLink.sendMessage(M.getIp(), M.getPort(), msg);
        }
    }

    public static boolean validateTransfer(PublicKey sourceKey, PublicKey destinationKey, int amount){
        return accounts.containsKey(sourceKey) && accounts.containsKey(destinationKey) && accounts.get(sourceKey)[0] >= (amount+1) && !sourceKey.equals(destinationKey); 
    }

    public static boolean validateBlock(Block b) throws Exception{
        for (List<Object> o : b.getOperations()) {
            byte[] signature = (byte[]) o.get(1);
            TRANSFER_MESSAGE message = (TRANSFER_MESSAGE) o.get(0);
            byte[] msg = ByteArraysOperations.SerializeObject(message);

            if (DigitalSignature.VerifySignature(msg, signature, message.getSPK())){
                if (validateTransfer(message.getSPK(), message.getDPK(), message.getAmount())){
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean transfer(PublicKey sourceKey, PublicKey destKey, int amount, int blockCreator) throws Exception{
        if (validateTransfer(sourceKey, destKey, amount)){
            accounts.get(sourceKey)[0] -= (amount+1);
            accounts.get(sourceKey)[1]++;
            accounts.get(destKey)[0] += (amount);
            accounts.get(destKey)[1]++;
            payCommission(blockCreator);
            return true;
        }
        else{
            return false;
        }
    }

    public static void payCommission(int blockCreator) throws Exception{
        String keyPath = "../Common/resources/S" + blockCreator + "public.key";
        PublicKey key = RSAKeyGenerator.readPublic(keyPath);
        int[] array = {100, 0};
        if (!accounts.containsKey(key)){
            accounts.put(key, array);
        }
        accounts.get(key)[0] += 1;
        accounts.get(key)[1] += 1;
    }
}
