package pt.ulisboa.tecnico.classes.contract.classserver;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.44.1)",
    comments = "Source: ClassServer_ClassServer.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ClassServerServiceGrpc {

  private ClassServerServiceGrpc() {}

  public static final String SERVICE_NAME = "pt.ulisboa.tecnico.classes.contract.classserver.ClassServerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest,
      pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse> getPropagateStateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "propagateState",
      requestType = pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest.class,
      responseType = pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest,
      pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse> getPropagateStateMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest, pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse> getPropagateStateMethod;
    if ((getPropagateStateMethod = ClassServerServiceGrpc.getPropagateStateMethod) == null) {
      synchronized (ClassServerServiceGrpc.class) {
        if ((getPropagateStateMethod = ClassServerServiceGrpc.getPropagateStateMethod) == null) {
          ClassServerServiceGrpc.getPropagateStateMethod = getPropagateStateMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest, pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "propagateState"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ClassServerServiceMethodDescriptorSupplier("propagateState"))
              .build();
        }
      }
    }
    return getPropagateStateMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ClassServerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClassServerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClassServerServiceStub>() {
        @java.lang.Override
        public ClassServerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClassServerServiceStub(channel, callOptions);
        }
      };
    return ClassServerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ClassServerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClassServerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClassServerServiceBlockingStub>() {
        @java.lang.Override
        public ClassServerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClassServerServiceBlockingStub(channel, callOptions);
        }
      };
    return ClassServerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ClassServerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ClassServerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ClassServerServiceFutureStub>() {
        @java.lang.Override
        public ClassServerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ClassServerServiceFutureStub(channel, callOptions);
        }
      };
    return ClassServerServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ClassServerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void propagateState(pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPropagateStateMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPropagateStateMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest,
                pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse>(
                  this, METHODID_PROPAGATE_STATE)))
          .build();
    }
  }

  /**
   */
  public static final class ClassServerServiceStub extends io.grpc.stub.AbstractAsyncStub<ClassServerServiceStub> {
    private ClassServerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClassServerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClassServerServiceStub(channel, callOptions);
    }

    /**
     */
    public void propagateState(pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPropagateStateMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ClassServerServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<ClassServerServiceBlockingStub> {
    private ClassServerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClassServerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClassServerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse propagateState(pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPropagateStateMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ClassServerServiceFutureStub extends io.grpc.stub.AbstractFutureStub<ClassServerServiceFutureStub> {
    private ClassServerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ClassServerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ClassServerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse> propagateState(
        pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPropagateStateMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PROPAGATE_STATE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ClassServerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ClassServerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PROPAGATE_STATE:
          serviceImpl.propagateState((pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ClassServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ClassServerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ClassServerService");
    }
  }

  private static final class ClassServerServiceFileDescriptorSupplier
      extends ClassServerServiceBaseDescriptorSupplier {
    ClassServerServiceFileDescriptorSupplier() {}
  }

  private static final class ClassServerServiceMethodDescriptorSupplier
      extends ClassServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ClassServerServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ClassServerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ClassServerServiceFileDescriptorSupplier())
              .addMethod(getPropagateStateMethod())
              .build();
        }
      }
    }
    return result;
  }
}
