package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
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
    private static List<List<Object>> keys = new ArrayList<>();  
    // Object = [id, key, iv, address, port]
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
        
        ibtf = new ServerIBFT(blockchain, numServers);
        perfectLink = new PerfectLink((int)(addresses.get(id).get(1)), ibtf, numServers);
        Server thread = new Server();
        thread.start();

        while(true){
            Scanner sc= new Scanner(System.in);    //System.in is a standard input stream  
            System.out.print("Enter '1' to print the ledger:");  
            int a = sc.nextInt();
            if (a == 1){
                blockchain.print();
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

    public static int getid(){
        return id;
    }

    public static PerfectLink getPerfectLink(){
        return perfectLink;
    }

    public static void generateUserKey(String id, String address, String port) throws Exception{
        SecretKey key = SymetricKey.createKey();
        System.out.println("Chave:" + new String(key.getEncoded())); 
        byte[] iv = SymetricKey.createIV();
        System.out.println("IV:" + new String(iv));
        List<Object> newList = new ArrayList<>();
        newList.addAll(Arrays.asList(id, key, iv, address, port));
        keys.add(newList);
        perfectLink.sendBootMessage(id, address, port, key, iv);
    }
}
