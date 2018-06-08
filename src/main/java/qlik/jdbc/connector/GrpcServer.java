package qlik.jdbc.connector;

import io.grpc.*;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;

import java.io.IOException;

public class GrpcServer {

    public static class HeaderServerInterceptor implements ServerInterceptor{
        private Metadata.Key<byte[]> CUSTOM_HEADER_KEY =
                Metadata.Key.of("x-qlik-getdata-bin", Metadata.BINARY_BYTE_MARSHALLER);

        private ConnectorImpl _connector;

        public HeaderServerInterceptor(ConnectorImpl connector){
            super();
            _connector = connector;
        }

        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                final Metadata requestHeaders,
                ServerCallHandler<ReqT, RespT> next) {

            return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
                @Override
                public void sendHeaders(Metadata responseHeaders) {
                    responseHeaders.put(CUSTOM_HEADER_KEY, _connector.getDataResponseMetadata().toByteArray());

                    super.sendHeaders(responseHeaders);
                }
            }, requestHeaders);
        }
    }

    static public void main(String [] args) throws IOException, InterruptedException {

        ConnectorImpl connector = new ConnectorImpl();

        //TODO: Make port configurable with args
        io.grpc.Server server = ServerBuilder.forPort(50051)
                .addService(ServerInterceptors.intercept(connector, new HeaderServerInterceptor(connector)))
                .build()
                .start();

        System.out.println("Server started!");

        if (server != null) {
            server.awaitTermination();
        }

        System.out.println("Server Closing!");
    }
}