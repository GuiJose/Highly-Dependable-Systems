package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server extends Thread{
    private static int id;
    private static boolean isMain = false;
    private static List<List<Object>> addresses = new ArrayList<>();
    private static int numServers;
    private static List<String> messageList = new ArrayList<>();
    private static Blockchain blockchain = new Blockchain();
    private static ServerFrontend frontend;
    private static ServerIBFT ibtf;

    public void run(){ 
        try {
            frontend.listening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws Exception{
        id = Integer.parseInt(args[0]);
        if (id == 0){isMain = true;}
        readConfiguration();
        numServers = addresses.size();
        
        ibtf = new ServerIBFT(blockchain);
        frontend = new ServerFrontend((int)(addresses.get(id).get(1)), ibtf);
        Server thread = new Server();
        thread.start();

        Scanner sc = new Scanner(System.in);    //System.in is a standard input stream  
        System.out.print("Enter '1' to say hello to all the other servers - ");  
        int a = sc.nextInt();
        if (a == 1){
            //sayHi();
        }  
    }

    private static void readConfiguration(){
        try {
            File file = new File("src/main/java/hdl/configuration.txt");
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
}
