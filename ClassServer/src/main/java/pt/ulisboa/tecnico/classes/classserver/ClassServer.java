package pt.ulisboa.tecnico.classes.classserver;


import java.util.Timer;
import java.util.TimerTask;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;

public class ClassServer {

  private static int port;
  private static boolean flag = false;
  private static ClassServerFrontend cEnd;

  public static void main(String[] args) throws Exception {

    	for(int i= 0; i<args.length;i++){
			if(args[i].equals("-debug")) flag = true;
		}
		
		final String host = args[0];
		port = Integer.parseInt(args[1]);
		final String qualifier = args[2];

		final String hostN = "localhost";
    	final int portN = 5000;
		ManagedChannel channelN = ManagedChannelBuilder.forAddress(hostN, portN).usePlaintext().build();
		NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channelN);
		
		Class class_ = new Class(); 
		cEnd = new ClassServerFrontend(hostN, portN, class_, qualifier, flag);
		class_.setFlag(flag);

		final BindableService pImpl = new ProfessorServiceImpl(class_);
		final BindableService sImpl = new StudentServiceImpl(class_);
		final BindableService aImpl = new AdminServiceImpl(class_, cEnd);
		final BindableService cImpl = new ClassServerServiceImpl(class_, flag);

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(port).addService(pImpl).addService(sImpl).addService(aImpl).addService(cImpl).build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Server started");
		System.out.println("Press ENTER to close server");

		//5 em 5 segundos tenta registar no servidor de nomes
		while(!cEnd.register("Turmas", host, port, qualifier)) Thread.sleep(5000);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run(){
				if(flag && class_.haveGossip()) System.out.println(Stringify.format(cEnd.propagateState()));
				else if (class_.haveGossip()) cEnd.propagateState();
			}
		}, 10000,10000);
		
		System.in.read();
		cEnd.remove("Turmas",host,port);
		server.shutdown();
		System.exit(0);
		// Do not exit the main thread. Wait until server is terminated.
  }
}
