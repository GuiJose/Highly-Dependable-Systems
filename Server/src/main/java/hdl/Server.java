package hdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server extends Thread{
    private static int id;
    private static DatagramSocket senderSocket;
    private static boolean isMain = false;
    private static List<List<Object>> addresses = new ArrayList<>();
    private static ServerFrontend frontend;

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

        senderSocket = new DatagramSocket();
        frontend = new ServerFrontend((int)(addresses.get(id).get(1)));
        Server thread = new Server();
        thread.start();

        Scanner sc= new Scanner(System.in);    //System.in is a standard input stream  
        System.out.print("Enter '1' to say hello to all the other servers - ");  
        int a = sc.nextInt();
        if (a == 1){
            sayHi();
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
    
    private static void sayHi() throws Exception{
        for (List<Object> address : addresses) {
            InetAddress ip = InetAddress.getByName((String)address.get(0));
            int port = (int)address.get(1);        
            String message = "Hello! I'm the server " + String.valueOf(id);
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            senderSocket.send(packet);
        }
    }
}
