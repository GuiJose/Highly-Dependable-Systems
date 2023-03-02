package pt.ulisboa.tecnico.classes.classserver;


import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Timestamp;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;
import pt.ulisboa.tecnico.classes.contract.student.StudentServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.EnrollRequest;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.EnrollResponse;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.ListClassRequest;
import pt.ulisboa.tecnico.classes.contract.student.StudentClassServer.ListClassResponse;

public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {
	private Class class_;

    public StudentServiceImpl(Class class_){
        this.class_ = class_;
    }

	@Override
	public void enroll(EnrollRequest request, StreamObserver<EnrollResponse> responseObserver) {
		String command = "enroll " + request.getStudent().getStudentId() + " " + request.getStudent().getStudentName();
		Timestamp prev = new Timestamp(request.getTimestampList());
		if(!prev.lessOrEqual(class_.getTimestamp())) {
			class_.addPendingCommand(prev, command);
			ResponseCode code = ResponseCode.SERVER_NOT_RECENT_VERSION;
			responseObserver.onNext(EnrollResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
			responseObserver.onCompleted();
			return;
		}

		ResponseCode code = class_.enroll(request.getStudent().getStudentId(), request.getStudent().getStudentName(), false);
		if(code == ResponseCode.OK) {
			prev = class_.updatePrevTimestamp(new ArrayList<>(request.getTimestampList()));
			class_.updateTimestamp();	
			class_.addLog(prev, command);
		}
		responseObserver.onNext(EnrollResponse.newBuilder().setCode(code).addAllTimestamp(class_.getTimestamp()).build());
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
}