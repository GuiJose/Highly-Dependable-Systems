package pt.ulisboa.tecnico.classes.student;

import java.util.Random;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Student;
import pt.ulisboa.tecnico.classes.contract.naming.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest;
import pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.EnrollRequest;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.EnrollResponse;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.ListClassResponse;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.ListClassRequest;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.Timestamp;

public class StudentFrontend {

    private final ManagedChannel channelN;
    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stubN;
    private final String service = "Turmas";
    private Timestamp timestamp  = new Timestamp();
    private final boolean debug;
    private String qualifier;

    public StudentFrontend(String host, int port, boolean debug){
        this.debug = debug;
        channelN = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
	    stubN = NamingServerServiceGrpc.newBlockingStub(channelN);
    }

    public  ManagedChannel SearchServer(String service, String qualifier){
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

    public String enrollment(String id, String name){
        ManagedChannel channel = SearchServer(service, "");
        if(channel == null){
            ResponseCode code = ResponseCode.INACTIVE_SERVER;
            return Stringify.format(code);
        }
        if(debug) System.err.println("INF: Sending request to server " + qualifier);
        StudentServiceGrpc.StudentServiceBlockingStub stub = StudentServiceGrpc.newBlockingStub(channel);
        EnrollResponse response = stub.enroll(EnrollRequest.newBuilder().setStudent(Student.newBuilder().setStudentId(id).setStudentName(name).build()).addAllTimestamp(timestamp.getTimestamp()).build());
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
        StudentServiceGrpc.StudentServiceBlockingStub stub = StudentServiceGrpc.newBlockingStub(channel);
        ListClassResponse response = stub.listClass(ListClassRequest.newBuilder().addAllTimestamp(timestamp.getTimestamp()).build());
		ResponseCode code = ResponseCode.forNumber(response.getCodeValue());
        timestamp.updateTimestamp(response.getTimestampList());
        channel.shutdown();
        return code == ResponseCode.OK? Stringify.format(response.getClassState()) : Stringify.format(code);
    }
}