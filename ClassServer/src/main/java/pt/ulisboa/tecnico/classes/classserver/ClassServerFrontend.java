package pt.ulisboa.tecnico.classes.classserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.Timestamp;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.Command;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest;

public class ClassServerFrontend {
    private final ManagedChannel channelN;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stubN;
    private final String service = "Turmas";
    private Class class_;
    private final String qualifier;
    private final boolean debug;

    public ClassServerFrontend(String host, int port, Class class_, String qualifier, boolean debug){

        channelN = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
	    stubN = NamingServerServiceGrpc.newBlockingStub(channelN);
        this.class_ = class_;
        this.debug = debug;
        if(qualifier.equals("P")) 
            this.qualifier = "S";
        else
            this.qualifier = "P";
    }

    public ManagedChannel SearchServer(String service, String qualifier){
        // Se servidor de nomes nao estiver ativo ignora exeçao e devolve que server nao esta ativo
        if(debug) System.err.println("INF: Searching server");
        try{
            LookupResponse response =  stubN.lookup(LookupRequest.newBuilder().setName(service).setQualifiers(qualifier).build());
            if(response.getServersCount() > 0){
            return ManagedChannelBuilder.forAddress(response.getServers(0).getHost(), response.getServers(0).getPort()).usePlaintext().build();  
            }
            return null;
        }catch(Exception e){return null;}
    }

    public ResponseCode propagateState(){
        if(debug) System.err.println("INF: Going to propagate state");
        ManagedChannel channelS = SearchServer(service, qualifier);
        if(channelS == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return code;
        }
        ClassServerServiceGrpc.ClassServerServiceBlockingStub stubS= ClassServerServiceGrpc.newBlockingStub(channelS);
        
        HashMap<Timestamp, String> log = class_.getLog();
        List<Command> commands= new ArrayList<>();
        for(Map.Entry<Timestamp, String> entry : log.entrySet()){
            Command command = Command.newBuilder().addAllTimestamp(entry.getKey().getTimestamp()).setCommand(entry.getValue()).build();
            commands.add(command);
        }
        class_.clearLog();
        PropagateStateResponse response = stubS.propagateState(PropagateStateRequest.newBuilder().addAllCommand(commands).build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        channelS.shutdown();
        class_.setFirstAfterGossip();
        return code;
    }

    public boolean register(String service, String host, int port, String qualifier){
        if(debug) System.err.println("INF: Going to register in naming server");
        try{
            RegisterResponse response = stubN.register(RegisterRequest.newBuilder().setName(service).setHost(host).setPort(port).setQualifiers(qualifier).build());
            int position = response.getPosition();
            class_.setPosition(position);
            return true;
        }
        catch(Exception e){
            System.err.println("ERROR: No naming server active");
        }
        return false;
    }

    public void remove(String service, String host, int port){
        stubN.remove(RemoveRequest.newBuilder().setName(service).setHost(host).setPort(port).build());
    }
}
