package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import hdl.RSAKeyGenerator;

public class User extends Thread {
    private static int id;
    private static int port;
    private static DatagramSocket senderSocket;
    private static List<List<Object>> addresses = new ArrayList<>();
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

        senderSocket = new DatagramSocket();
        frontend = new UserFrontend(port);
        User thread = new User();
        thread.start();

        frontend.sendBoot(Integer.toString(id) + ":BOOT:localhost:" + Integer.toString(port));

        while (true){
          Scanner sc= new Scanner(System.in);    //System.in is a standard input stream  
          System.out.println("Write the word to be appended(':' not allowed):");  
          String word = sc.nextLine();
          frontend.sendRequest(word);
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
