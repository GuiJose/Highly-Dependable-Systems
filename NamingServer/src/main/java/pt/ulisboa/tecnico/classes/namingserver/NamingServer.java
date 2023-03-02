package pt.ulisboa.tecnico.classes.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class NamingServer {

  public static void main(String[] args) throws Exception {

		final BindableService impl = new NamingServerServiceImpl();

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(5000).addService(impl).build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
  }
}
