package pt.ulisboa.tecnico.classes.admin;

import java.util.Scanner;

public class Admin {

  private static final String ACTIVATE_CMD = "activate";
  private static final String DEACTIVATE_CMD = "deactivate";
  private static final String ACTIVATE_GOSSIP_CMD = "activateGossip";
  private static final String DEACTIVATE_GOSSIP_CMD = "deactivateGossip";
  private static final String GOSSIP_CMD = "gossip";
  private static final String DUMP_CMD = "dump";
  private static final String EXIT_CMD = "exit";
  private static boolean debug = false;
  public static void main(String[] args) {
    
    final String host = "localhost";
		final int port = 5000;
		
    for(int i= 0; i<args.length;i++){
			if(args[i].equals("-debug")) debug = true;
		}
    
    Scanner scanner = new Scanner(System.in);
		AdminFrontend admEnd = new AdminFrontend(host, port, debug);


		while (true) {
			System.out.printf("> ");
			String line = scanner.nextLine();
      String[] lineSplited = line.split(" ", 0);

			if (DUMP_CMD.equals(lineSplited[0])) {
				System.out.println(admEnd.dump(lineSplited[1])+ "\n");
      }
      else if (ACTIVATE_CMD.equals(lineSplited[0])) {
				System.out.println(admEnd.activate(lineSplited[1])+ "\n");
      }
      else if (DEACTIVATE_CMD.equals(lineSplited[0])) {
				System.out.println(admEnd.deactivate(lineSplited[1])+ "\n");
      }
      else if (ACTIVATE_GOSSIP_CMD.equals(lineSplited[0])) {
				System.out.println(admEnd.activateGossip(lineSplited[1])+ "\n");
      }
      else if (DEACTIVATE_GOSSIP_CMD.equals(lineSplited[0])) {
				System.out.println(admEnd.deactivateGossip(lineSplited[1])+ "\n");
      }
      else if (GOSSIP_CMD.equals(lineSplited[0])) {
				System.out.println(admEnd.gossip(lineSplited[1])+ "\n");
      }
      else if (EXIT_CMD.equals(lineSplited[0])){
        scanner.close();
        System.exit(0);
      }
      else{
				System.out.println("Invalid comand!\nValid comands: dump, activate, deactivate, activateGossip, deactivateGossip, gossip exit");
			}
    }
  }
}
