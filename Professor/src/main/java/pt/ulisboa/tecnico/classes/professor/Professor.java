package pt.ulisboa.tecnico.classes.professor;

import java.util.Scanner;

public class Professor {

  	private static final String ABRIR_INSCRICOES_CMD = "openEnrollments";
	private static final String FECHAR_INSCRICOES_CMD = "closeEnrollments";
	private static final String LISTAR_CMD = "list";
	private static final String CANCELAR_INSCRICAO_CMD = "cancelEnrollment";
	private static final String EXIT_CMD = "exit";
	private static boolean debug = false;
	public static void main(String[] args) {

		final String host = "localhost";
		final int port = 5000;
		
		for(int i= 0; i<args.length;i++){
			if(args[i].equals("-debug")) debug = true;
		}

    	Scanner scanner = new Scanner(System.in);
		ProfessorFrontend profEnd = new ProfessorFrontend(host, port, debug);

		while (true) {
			System.out.printf("> ");

			String line = scanner.nextLine();
      		String[] lineSplited = line.split(" ", 0);

			if (ABRIR_INSCRICOES_CMD.equals(lineSplited[0])) {
				System.out.println(profEnd.openEnrollments(Integer.parseInt(lineSplited[1])) + "\n");

			} else if (FECHAR_INSCRICOES_CMD.equals(lineSplited[0])) {
				System.out.println(profEnd.closeEnrollments() + "\n");
			}
			else if (LISTAR_CMD.equals(lineSplited[0])) {
				System.out.println(profEnd.listing() + "\n");
      		}
			else if (CANCELAR_INSCRICAO_CMD.equals(lineSplited[0])) {
				System.out.println(profEnd.cancelEnroll(lineSplited[1]) + "\n");
      		}
			else if(EXIT_CMD.equals(lineSplited[0])){
				scanner.close();
				System.exit(0);
			}
			else{
				System.out.println("Invalid comand!\nValid comands: openEnrollments <capacity>, closeEnrollments, list, cancelEnrollment <StudentId>, exit");
			}
		}
	}
}
