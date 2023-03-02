package pt.ulisboa.tecnico.classes.classserver;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.classes.Stringify;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ClassState;
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
import pt.ulisboa.tecnico.classes.contract.admin.AdminServiceGrpc;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {
    private Class class_;
	private ClassServerFrontend cEnd;

    public AdminServiceImpl(Class class_, ClassServerFrontend cEnd){
        this.class_ = class_;
		this.cEnd = cEnd;
    }

    @Override
	public void dump(DumpRequest request, StreamObserver<DumpResponse> responseObserver) {
		ClassState response = class_.lists(false);
		ResponseCode code = class_.isActive();
		responseObserver.onNext(DumpResponse.newBuilder().setCode(code).setClassState(response).build());
		responseObserver.onCompleted();
	}

	@Override
	public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
		ResponseCode code = class_.setActive();
		responseObserver.onNext(ActivateResponse.newBuilder().setCode(code).build());
		responseObserver.onCompleted();
	}

	@Override
	public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
		ResponseCode code = class_.setDeactive();
		responseObserver.onNext(DeactivateResponse.newBuilder().setCode(code).build());
		responseObserver.onCompleted();
	}

	@Override
	public void activateGossip(ActivateGossipRequest request, StreamObserver<ActivateGossipResponse> responseObserver) {
		ResponseCode code = class_.activateGossip();
		responseObserver.onNext(ActivateGossipResponse.newBuilder().setCode(code).build());
		responseObserver.onCompleted();
	}

	@Override
	public void deactivateGossip(DeactivateGossipRequest request, StreamObserver<DeactivateGossipResponse> responseObserver) {
		ResponseCode code = class_.deactivateGossip();
		responseObserver.onNext(DeactivateGossipResponse.newBuilder().setCode(code).build());
		responseObserver.onCompleted();
	}

	@Override
	public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
		ResponseCode code = cEnd.propagateState();
		System.out.println(Stringify.format(code));
		responseObserver.onNext(GossipResponse.newBuilder().setCode(code).build());
		responseObserver.onCompleted();
	}
}
