package pt.ulisboa.tecnico.classes.contract.naming;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.44.1)",
    comments = "Source: ClassServer_NamingServer.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class NamingServerServiceGrpc {

  private NamingServerServiceGrpc() {}

  public static final String SERVICE_NAME = "pt.ulisboa.tecnico.classes.contract.naming.NamingServerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest,
      pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse> getRegisterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "register",
      requestType = pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest.class,
      responseType = pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest,
      pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse> getRegisterMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest, pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse> getRegisterMethod;
    if ((getRegisterMethod = NamingServerServiceGrpc.getRegisterMethod) == null) {
      synchronized (NamingServerServiceGrpc.class) {
        if ((getRegisterMethod = NamingServerServiceGrpc.getRegisterMethod) == null) {
          NamingServerServiceGrpc.getRegisterMethod = getRegisterMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest, pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "register"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NamingServerServiceMethodDescriptorSupplier("register"))
              .build();
        }
      }
    }
    return getRegisterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest,
      pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse> getLookupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Lookup",
      requestType = pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest.class,
      responseType = pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest,
      pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse> getLookupMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest, pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse> getLookupMethod;
    if ((getLookupMethod = NamingServerServiceGrpc.getLookupMethod) == null) {
      synchronized (NamingServerServiceGrpc.class) {
        if ((getLookupMethod = NamingServerServiceGrpc.getLookupMethod) == null) {
          NamingServerServiceGrpc.getLookupMethod = getLookupMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest, pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Lookup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NamingServerServiceMethodDescriptorSupplier("Lookup"))
              .build();
        }
      }
    }
    return getLookupMethod;
  }

  private static volatile io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest,
      pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse> getRemoveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "remove",
      requestType = pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest.class,
      responseType = pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest,
      pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse> getRemoveMethod() {
    io.grpc.MethodDescriptor<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest, pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse> getRemoveMethod;
    if ((getRemoveMethod = NamingServerServiceGrpc.getRemoveMethod) == null) {
      synchronized (NamingServerServiceGrpc.class) {
        if ((getRemoveMethod = NamingServerServiceGrpc.getRemoveMethod) == null) {
          NamingServerServiceGrpc.getRemoveMethod = getRemoveMethod =
              io.grpc.MethodDescriptor.<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest, pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "remove"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new NamingServerServiceMethodDescriptorSupplier("remove"))
              .build();
        }
      }
    }
    return getRemoveMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NamingServerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NamingServerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NamingServerServiceStub>() {
        @java.lang.Override
        public NamingServerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NamingServerServiceStub(channel, callOptions);
        }
      };
    return NamingServerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NamingServerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NamingServerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NamingServerServiceBlockingStub>() {
        @java.lang.Override
        public NamingServerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NamingServerServiceBlockingStub(channel, callOptions);
        }
      };
    return NamingServerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NamingServerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NamingServerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NamingServerServiceFutureStub>() {
        @java.lang.Override
        public NamingServerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NamingServerServiceFutureStub(channel, callOptions);
        }
      };
    return NamingServerServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class NamingServerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void register(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterMethod(), responseObserver);
    }

    /**
     */
    public void lookup(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getLookupMethod(), responseObserver);
    }

    /**
     */
    public void remove(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRegisterMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest,
                pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse>(
                  this, METHODID_REGISTER)))
          .addMethod(
            getLookupMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest,
                pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse>(
                  this, METHODID_LOOKUP)))
          .addMethod(
            getRemoveMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest,
                pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse>(
                  this, METHODID_REMOVE)))
          .build();
    }
  }

  /**
   */
  public static final class NamingServerServiceStub extends io.grpc.stub.AbstractAsyncStub<NamingServerServiceStub> {
    private NamingServerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NamingServerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NamingServerServiceStub(channel, callOptions);
    }

    /**
     */
    public void register(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void lookup(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getLookupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void remove(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest request,
        io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class NamingServerServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<NamingServerServiceBlockingStub> {
    private NamingServerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NamingServerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NamingServerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse register(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse lookup(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getLookupMethod(), getCallOptions(), request);
    }

    /**
     */
    public pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse remove(pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class NamingServerServiceFutureStub extends io.grpc.stub.AbstractFutureStub<NamingServerServiceFutureStub> {
    private NamingServerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NamingServerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NamingServerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse> register(
        pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse> lookup(
        pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getLookupMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse> remove(
        pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER = 0;
  private static final int METHODID_LOOKUP = 1;
  private static final int METHODID_REMOVE = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final NamingServerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(NamingServerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER:
          serviceImpl.register((pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RegisterResponse>) responseObserver);
          break;
        case METHODID_LOOKUP:
          serviceImpl.lookup((pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.LookupResponse>) responseObserver);
          break;
        case METHODID_REMOVE:
          serviceImpl.remove((pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveRequest) request,
              (io.grpc.stub.StreamObserver<pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.RemoveResponse>) responseObserver);
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

  private static abstract class NamingServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NamingServerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return pt.ulisboa.tecnico.classes.contract.naming.ClassServerNamingServer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NamingServerService");
    }
  }

  private static final class NamingServerServiceFileDescriptorSupplier
      extends NamingServerServiceBaseDescriptorSupplier {
    NamingServerServiceFileDescriptorSupplier() {}
  }

  private static final class NamingServerServiceMethodDescriptorSupplier
      extends NamingServerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    NamingServerServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (NamingServerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NamingServerServiceFileDescriptorSupplier())
              .addMethod(getRegisterMethod())
              .addMethod(getLookupMethod())
              .addMethod(getRemoveMethod())
              .build();
        }
      }
    }
    return result;
  }
}
