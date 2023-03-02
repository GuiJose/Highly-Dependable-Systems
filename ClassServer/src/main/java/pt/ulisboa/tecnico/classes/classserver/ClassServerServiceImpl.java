package pt.ulisboa.tecnico.classes.classserver;


import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Timestamp;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.Command;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerServiceGrpc;

public class ClassServerServiceImpl extends ClassServerServiceGrpc.ClassServerServiceImplBase{
    private Class class_;
    private final boolean debug;
    private static final String ABRIR_INSCRICOES_CMD = "openEnrollments";
	private static final String FECHAR_INSCRICOES_CMD = "closeEnrollments";
	private static final String CANCELAR_INSCRICAO_CMD = "cancelEnrollment";
    private static final String INSCREVER_CMD = "enroll";

    public ClassServerServiceImpl(Class class_, boolean debug){
        this.debug = debug;
        this.class_ = class_;
    }

    public void execCommands(List<Command> commands) {
        if(debug) System.err.println("INF: Resolving commands from gossip");
        for(Command c: commands) {
            Timestamp prev = new Timestamp(c.getTimestampList());
            class_.updateTimestampByPrev(prev.getTimestamp()); 
            List<Integer> replica = class_.getTimestamp();

            if(!prev.lessOrEqual(replica)) {
                continue;
            }
            
            String[] lineSplited = c.getCommand().split(" ", 0);
			if (ABRIR_INSCRICOES_CMD.equals(lineSplited[0])) {
                class_.openEnrollments(Integer.parseInt(lineSplited[1]), true);
			} else if (FECHAR_INSCRICOES_CMD.equals(lineSplited[0])) {
				class_.closeEnrollments(true);
			} else if (CANCELAR_INSCRICAO_CMD.equals(lineSplited[0])) {
				class_.cancelEnroll(lineSplited[1], true);
      		} else if (INSCREVER_CMD.equals(lineSplited[0])) {
                class_.enroll(lineSplited[1], lineSplited[2], true);
			}
            
            class_.updateTimestamp();
        }
    }

    @Override
	public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
		List<Command> commands = request.getCommandList();
        execCommands(commands);
        class_.resolvePendingCommands();
        class_.setFirstAfterGossip();

        ResponseCode code = ResponseCode.OK;

		responseObserver.onNext(PropagateStateResponse.newBuilder().setCode(code).build());
		responseObserver.onCompleted();
	}
}