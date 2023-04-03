package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import hdl.RSAKeyGenerator;

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
                //blockchain.print();
                System.out.println(accounts.size());
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

    public static void createAccount(PublicKey pubKey, int port) throws Exception{
        int[] array = {100, 0};
        if (!accounts.containsKey(pubKey)){
            accounts.put(pubKey, array);
            perfectLink.sendMessage("localhost", port, id + ":BOOT:");
        }
    }

    public static void checkBalance(PublicKey pubKey, int port) throws Exception{
        if(accounts.containsKey(pubKey)){
            int ammount = accounts.get(pubKey)[0];
            int writeTimeStamp = accounts.get(pubKey)[1];
            perfectLink.sendMessage("localhost", port, "CHECK:" + ammount + ":" + writeTimeStamp + ":" + id + ":");
        }
        else{
            perfectLink.sendMessage("localhost", port, "CHECK:Account does not exist:" + id + ":");
        }
    }

    public static boolean validateTransfer(PublicKey sourceKey, PublicKey destinationKey, int amount){
        return accounts.containsKey(sourceKey) && accounts.containsKey(destinationKey) && accounts.get(sourceKey)[0] >= (amount+1); 
    }

    public static boolean validateBlock(Block b) throws Exception{
        for (byte[] o : b.getOperations()) {
            byte[] signature = new byte[512];
            byte[] destinationKeyBytes = new byte[550];
            byte[] sourceKeyBytes = new byte[550];
            byte[] msg = new byte[o.length-512];
            System.arraycopy(o, o.length-512, signature, 0, 512); 
            System.arraycopy(o, o.length-1062, destinationKeyBytes, 0, 550);
            System.arraycopy(o, o.length-1612, sourceKeyBytes, 0, 550);
            System.arraycopy(o, 0, msg, 0, o.length-512);
            
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec sourceKeySpec = new X509EncodedKeySpec(sourceKeyBytes);
            PublicKey sourcePK = keyFactory.generatePublic(sourceKeySpec);
            X509EncodedKeySpec destinationKeySpec = new X509EncodedKeySpec(destinationKeyBytes);
            PublicKey destinationPK = keyFactory.generatePublic(destinationKeySpec);

            if (DigitalSignature.VerifySignature(msg, signature, sourcePK)){
                String newString = new String(o);
                if (validateTransfer(sourcePK, destinationPK, Integer.parseInt(newString.split(":")[5]))){
                    continue;
                }
            }
            return false;
        }
        return true;
    }
}
