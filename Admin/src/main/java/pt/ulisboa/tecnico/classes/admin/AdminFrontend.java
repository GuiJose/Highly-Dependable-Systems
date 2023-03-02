package pt.ulisboa.tecnico.classes.admin;

import java.util.Random;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateGossipRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateGossipResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.ActivateResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateGossipRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateGossipResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DeactivateResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DumpRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.DumpResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.GossipRequest;
import pt.ulisboa.tecnico.classes.contract.admin.AdminClassServer.GossipResponse;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

public class AdminFrontend {
    
    private final ManagedChannel channelN;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stubN;
    private final String service = "Turmas";
    private final boolean debug;

    public AdminFrontend(String host, int port, boolean debug){

        channelN = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
	    stubN = NamingServerServiceGrpc.newBlockingStub(channelN);
        this.debug = debug;
    }

    public ManagedChannel SearchServer(String service, String qualifier){
       // Se servidor de nomes nao estiver ativo ignora exeçao e devolve que server nao esta ativo
        if(debug) System.err.println("INF: Searching server");
        try{
            LookupResponse response =  stubN.lookup(LookupRequest.newBuilder().setName(service).setQualifiers(qualifier).build());
            if(response.getServersCount() > 0){
                Random rand = new Random();
                int serverNumber = rand.nextInt(response.getServersCount());
                ManagedChannel channel = ManagedChannelBuilder.forAddress(response.getServers(serverNumber).getHost(), response.getServers(serverNumber).getPort()).usePlaintext().build();
                return channel;
            }
            return null;
        }catch(Exception e){return null;}
    }


    public String dump(String qualifier){
        ManagedChannel channel = SearchServer(service, qualifier);
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        DumpResponse response = stub.dump(DumpRequest.newBuilder().build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channel.shutdown();
        return code == ResponseCode.OK? Stringify.format(response.getClassState()) : Stringify.format(code);
    }

    public String activate(String qualifier){
        ManagedChannel channel = SearchServer(service, qualifier);
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        ActivateResponse response = stub.activate(ActivateRequest.newBuilder().build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channel.shutdown();
        return Stringify.format(code);
    }

    public String deactivate(String qualifier){
        ManagedChannel channel = SearchServer(service, qualifier);
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        DeactivateResponse response = stub.deactivate(DeactivateRequest.newBuilder().build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channel.shutdown();
        return Stringify.format(code);
    }

    public String activateGossip(String qualifier){
        ManagedChannel channel = SearchServer(service, qualifier);
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        ActivateGossipResponse response = stub.activateGossip(ActivateGossipRequest.newBuilder().build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channel.shutdown();
        return Stringify.format(code);
    }

    public String deactivateGossip(String qualifier){
        ManagedChannel channel = SearchServer(service, qualifier);
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        DeactivateGossipResponse response = stub.deactivateGossip(DeactivateGossipRequest.newBuilder().build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channel.shutdown();
        return Stringify.format(code);
    }

    public String gossip(String qualifier){
        ManagedChannel channel = SearchServer(service, qualifier);
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        GossipResponse response = stub.gossip(GossipRequest.newBuilder().build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channel.shutdown();
        return Stringify.format(code);
    }
}

