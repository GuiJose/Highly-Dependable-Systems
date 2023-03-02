package pt.ulisboa.tecnico.classes.classserver;


import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Timestamp;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CancelEnrollmentRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CancelEnrollmentResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CloseEnrollmentsRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.CloseEnrollmentsResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.ListClassRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.ListClassResponse;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.OpenEnrollmentsRequest;
import pt.ulisboa.tecnico.classes.contract.professor.ProfessorClassServer.OpenEnrollmentsResponse;

public class ProfessorServiceImpl extends ProfessorServiceGrpc.ProfessorServiceImplBase {
	private Class class_;

    public ProfessorServiceImpl(Class class_){
        this.class_ = class_;
    }

	@Override
	public void openEnrollments(OpenEnrollmentsRequest request, StreamObserver<OpenEnrollmentsResponse> responseObserver) {
		String command = "openEnrollments " + request.getCapacity();
		Timestamp prev = new Timestamp(request.getTimestampList());
		if(!prev.lessOrEqual(class_.getTimestamp())) {
			class_.addPendingCommand(prev, command);
			ResponseCode code = ResponseCode.SERVER_NOT_RECENT_VERSION;
			responseObserver.onNext(OpenEnrollmentsResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
			responseObserver.onCompleted();
			return;
		}
		
		ResponseCode code = class_.openEnrollments(request.getCapacity(), false);
		if(code == ResponseCode.OK) {
			prev = class_.updatePrevTimestamp(new ArrayList<>(request.getTimestampList()));	
			class_.updateTimestamp();
			class_.addLog(prev, command);
		}

		responseObserver.onNext(OpenEnrollmentsResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
		responseObserver.onCompleted();
	}

    @Override
	public void closeEnrollments(CloseEnrollmentsRequest request, StreamObserver<CloseEnrollmentsResponse> responseObserver) {
		String command = "closeEnrollments";
		Timestamp prev = new Timestamp(request.getTimestampList());
		if(!prev.lessOrEqual(class_.getTimestamp())) {
			class_.addPendingCommand(prev, command);
			ResponseCode code = ResponseCode.SERVER_NOT_RECENT_VERSION;
			responseObserver.onNext(CloseEnrollmentsResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
			responseObserver.onCompleted();
			return;
		}
		
		ResponseCode code = class_.closeEnrollments(false);
		if(code == ResponseCode.OK) {	
			prev = class_.updatePrevTimestamp(new ArrayList<>(request.getTimestampList()));	
			class_.updateTimestamp();
			class_.addLog(prev, command);
		}

		responseObserver.onNext(CloseEnrollmentsResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
		responseObserver.onCompleted();
	}

	@Override
	public void listClass(ListClassRequest request, StreamObserver<ListClassResponse> responseObserver) {
		Timestamp prev = new Timestamp(request.getTimestampList());
		
		if(!prev.lessOrEqual(class_.getTimestamp())) {
			ResponseCode code = ResponseCode.SERVER_NOT_RECENT_VERSION_LIST;
			responseObserver.onNext(ListClassResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
			responseObserver.onCompleted();
			return;
		}
		ClassState response = class_.lists(false);
		ResponseCode code = class_.isActive();
		responseObserver.onNext(ListClassResponse.newBuilder().setCode(code).setClassState(response).addAllTimestamp(class_.getTimestamp()).build());
		responseObserver.onCompleted();
	}

	@Override
	public void cancelEnrollment(CancelEnrollmentRequest request, StreamObserver<CancelEnrollmentResponse> responseObserver) {
		String command = "cancelEnrollment " + request.getStudentId();
		Timestamp prev = new Timestamp(request.getTimestampList());
		
		if(!prev.lessOrEqual(class_.getTimestamp())) {
			class_.addPendingCommand(prev, command);
			ResponseCode code = ResponseCode.SERVER_NOT_RECENT_VERSION;
			responseObserver.onNext(CancelEnrollmentResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
			responseObserver.onCompleted();
			return;
		}
		ResponseCode code = class_.cancelEnroll(request.getStudentId(), false);
		if(code == ResponseCode.OK) {	
			prev = class_.updatePrevTimestamp(new ArrayList<>(request.getTimestampList()));
			class_.updateTimestamp();	
			class_.addLog(prev, command);
		}

		responseObserver.onNext(CancelEnrollmentResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
		responseObserver.onCompleted();
	}
}