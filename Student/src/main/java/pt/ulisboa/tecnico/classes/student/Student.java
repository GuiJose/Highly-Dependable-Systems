package pt.ulisboa.tecnico.classes.student;

import java.util.Scanner;


public class Student {

  private static final String INSCREVER_CMD = "enroll";
  private static final String LISTAR_CMD = "list";
  private static final String EXIT_CMD = "exit";
  private static boolean debug = false;

    
  public static void main(String[] args) {


    for(int i= 0; i<args.length;i++){
			if(args[i].equals("-debug")) debug = true;
		}

    String name = "";
    for (int i = 0; i < args.length; i++) {
      if( i>= 1 && !args[i].equals("-debug")) name += args[i] + " ";
    }
    
    name = name.substring(0, name.length()-1);
    if(name.length() < 3 || name.length() > 30){
      System.err.println("Invalid student name!\n");
      return;
    }
    
    // check arguments
    if (args.length < 2) {
      System.err.println("Argument(s) missing!");
      System.err.printf("Usage: java %s host port%n", Student.class.getName());
      return;
    }

    final String host = "localhost";
    final int port = 5000;
    final String id = args[0];
    
    Scanner scanner = new Scanner(System.in);
    StudentFrontend stuEnd = new StudentFrontend(host, port,debug);
  
		while (true) {
			System.out.printf("> ");

			String line = scanner.nextLine();
      String[] lineSplited = line.split(" ", 0);

			if (INSCREVER_CMD.equals(lineSplited[0])) {
				System.out.println(stuEnd.enrollment(id,name) + "\n");
      }
      else if (LISTAR_CMD.equals(lineSplited[0])) {
				System.out.println(stuEnd.listing() + "\n");
      }
      else if (EXIT_CMD.equals(lineSplited[0])){
        scanner.close();
        System.exit(0);
      }
      else{
				System.out.println("Invalid comand!\nValid comands: enroll, list, exit");
			}
    }
  }
}
