package pt.ulisboa.tecnico.classes.namingserver;

import java.util.Set;
import java.util.stream.Collectors;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse;;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase{
    private NamingServices namingServices;

    public NamingServerServiceImpl(){
        this.namingServices = new NamingServices();
    }

	@Override
	public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
		int position = namingServices.register(request.getName(), request.getHost(), request.getPort(), request.getQualifiers());

		responseObserver.onNext(RegisterResponse.newBuilder().setPosition(position).build());
		responseObserver.onCompleted();
	}

	@Override
	public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
		Set<ServerEntry> servers = namingServices.lookup(request.getName(), request.getQualifiers());
		LookupResponse response = LookupResponse.newBuilder()
								.addAllServers(servers.stream().map(ServerEntry::proto).collect(Collectors.toList()))
								.build();;
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void remove(RemoveRequest request, StreamObserver<RemoveResponse> responseObserver) {
		namingServices.remove(request.getName(), request.getHost(), request.getPort());

		responseObserver.onNext(RemoveResponse.getDefaultInstance());
		responseObserver.onCompleted();
	}
}
