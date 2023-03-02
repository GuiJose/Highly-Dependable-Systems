package pt.ulisboa.tecnico.classes.professor;

import java.util.Random;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CancelEnrollmentRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CancelEnrollmentResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CloseEnrollmentsRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CloseEnrollmentsResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.ListClassRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.ListClassResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.OpenEnrollmentsRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.OpenEnrollmentsResponse;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.Timestamp;

public class ProfessorFrontend {

    final ManagedChannel channelN;
    final NamingServerServiceGrpc.NamingServerServiceBlockingStub stubN;
    final String service = "Turmas";
    private Timestamp timestamp  = new Timestamp();
    private final boolean debug;
    private String qualifier;

    public ProfessorFrontend(String host, int port, boolean debug){
        this.debug = debug;
        channelN = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
	    stubN = NamingServerServiceGrpc.newBlockingStub(channelN);
    }

    public ManagedChannel SearchServer(String service, String qualifier){
       // Se servidor de nomes nao estiver ativo ignora exeçao e devolve que server nao esta ativo
       if(debug) System.err.println("INF: Searching server");
       try{
        LookupResponse response =  stubN.lookup(LookupRequest.newBuilder().setName(service).setQualifiers(qualifier).build());
        if(response.getServersCount() > 0){
            Random rand = new Random();
            int serverNumber = rand.nextInt(response.getServersCount());
            this.qualifier = response.getServers(serverNumber).getQualifier();
            ManagedChannel channel = ManagedChannelBuilder.forAddress(response.getServers(serverNumber).getHost(), response.getServers(serverNumber).getPort()).usePlaintext().build();
            return channel;
        }
        return null;
        }catch(Exception e){return null;}
    }

    public String openEnrollments(int capacity){
        ManagedChannel channel = SearchServer(service, "P");
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = ProfessorServiceGrpc.newBlockingStub(channel);
        OpenEnrollmentsResponse response = stub.openEnrollments(OpenEnrollmentsRequest.newBuilder().setCapacity(capacity).addAllTimestamp(timestamp.getTimestamp()).build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        timestamp.updateTimestamp(response.getTimestampList());
        channel.shutdown();
        return Stringify.format(code);
    }

    public String closeEnrollments(){
        ManagedChannel channel = SearchServer(service, "P");
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = ProfessorServiceGrpc.newBlockingStub(channel);
        CloseEnrollmentsResponse response = stub.closeEnrollments(CloseEnrollmentsRequest.newBuilder().addAllTimestamp(timestamp.getTimestamp()).build());
        ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        timestamp.updateTimestamp(response.getTimestampList());
        channel.shutdown();
        return Stringify.format(code);
    }

    public String listing(){
        ManagedChannel channel = SearchServer(service, "");
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        } 
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = ProfessorServiceGrpc.newBlockingStub(channel);
        ListClassResponse response = stub.listClass(ListClassRequest.newBuilder().addAllTimestamp(timestamp.getTimestamp()).build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        timestamp.updateTimestamp(response.getTimestampList());
        channel.shutdown();
        return code == ResponseCode.OK? Stringify.format(response.getClassState()) : Stringify.format(code);
    }

    public String cancelEnroll(String StudentId){
        ManagedChannel channel = SearchServer(service, "");
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        ProfessorServiceGrpc.ProfessorServiceBlockingStub stub = ProfessorServiceGrpc.newBlockingStub(channel);
        CancelEnrollmentResponse response = stub.cancelEnrollment(CancelEnrollmentRequest.newBuilder().setStudentId(StudentId).addAllTimestamp(timestamp.getTimestamp()).build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        timestamp.updateTimestamp(response.getTimestampList());
        channel.shutdown();
        return Stringify.format(code);
    }

}