package qlik.jdbc.connector;

import io.grpc.ClientInterceptor;
import io.grpc.stub.StreamObserver;
import qlik.connect.ConnectorGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import qlik.connect.GrpcServer;
import io.grpc.Metadata;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;

import java.util.concurrent.TimeUnit;


public class GrpcClient {

    public static class HeaderClientInterceptor implements ClientInterceptor {
        private Metadata.Key<byte[]> customHeadKey =
                Metadata.Key.of("x-qlik-getdata-bin", Metadata.BINARY_BYTE_MARSHALLER);

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                                   CallOptions callOptions, Channel next) {
            return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
                        @Override
                        public void onHeaders(Metadata headers) {
                            /**
                             * if you don't need receive header from server,
                             * you can use {@link io.grpc.stub.MetadataUtils attachHeaders}
                             * directly to send header
                             */
                            System.out.println(headers.toString());
                            super.onHeaders(headers);
                        }
                    }, headers);
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("Client Starting!");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().intercept(new HeaderClientInterceptor()).build();

        ConnectorGrpc.ConnectorStub stub = ConnectorGrpc.newStub(channel);

        System.out.println(".");

        try {
            GrpcServer.DataRequest d = GrpcServer.DataRequest.newBuilder().setConnection(
                    GrpcServer.ConnectionInfo.newBuilder().setConnectionString("postgres").build())
                    .setParameters(GrpcServer.DataInfo.newBuilder().setStatement("SELECT rowID,Airport,City,Country,IATACode,ICAOCode,Latitude,Longitude,Altitude,TimeZone,DST,TZ, clock_timestamp() FROM airports ORDER BY Airport").build())
                    .build();

            StreamObserver<GrpcServer.DataChunk> stream = new StreamObserver<GrpcServer.DataChunk>(){
                @Override
                public void onNext(GrpcServer.DataChunk dataChunk) {
                    System.out.println(dataChunk.getAllFields().toString());
///                    System.out.println("dataChunk: " + dataChunk.getStringBucket(0));
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Error: " + t.toString());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Stream completed");
                }
            };

            stub.getData(d, stream);

            System.out.println("Client sent request!");

        } catch (Error e){
            System.out.println("Error: " + e.toString());
        } finally {
            //
        }

        Thread.sleep(2000);

        channel.shutdown().awaitTermination(3, TimeUnit.SECONDS);
    }
}
