package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import hdl.RSAKeyGenerator;

public class User extends Thread {
    private static int id;
    private static int port;
    private static List<List<Object>> ServerAddresses = new ArrayList<>();
    private static UserFrontend frontend;

    public void run(){
        try {
            frontend.listening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws Exception{
        id = Integer.parseInt(args[0]);
        port = Integer.parseInt(args[1]);
        getServersAdd();
        RSAKeyGenerator.write(id,"u");

        frontend = new UserFrontend(port);
        User thread = new User();
        thread.start();

        PublicKey pubKey = RSAKeyGenerator.readPublic("../Common/resources/U" + id + "public.key"); 
        //frontend.sendBoot(port, pubKey);

        while (true){
          Scanner sc= new Scanner(System.in);    //System.in is a standard input stream  
          System.out.println("Choose the operation:\n1. Check Balance\n2. Transfer Money");  
          String option = sc.nextLine();
          if(option.equals("1")){
            frontend.sendCheck(port,pubKey);
          }
          else if( option.equals("2")){
            System.out.println("Indicate User and ammount:");
            String option2 = sc.nextLine();
            System.out.println("USER = " + option2.split(" ")[0] + " Ammount = " + option2.split(" ")[1]);
            //frontend.sendTransfer(option2.split(" ")[0], option2.split(" ")[1]);
          }
          
        }
    }
    
    private static void getServersAdd(){
        try {
            File file = new File("../Common/Sconfiguration.txt");
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
              String line = reader.nextLine();
              String[] data = line.split(" ");
              ServerAddresses.add(Arrays.asList(data[1], Integer.parseInt(data[2])));
            }
            reader.close();
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
    }

    public static List<List<Object>> getServers(){
      return ServerAddresses;
    }

    public static int getid(){
      return id;
    }
}
