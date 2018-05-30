package qlik.jdbc.connector;

import com.google.protobuf.Descriptors;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import qlik.connect.ConnectorGrpc;
import qlik.connect.GrpcServer.*;



import java.lang.reflect.Field;
import java.sql.*;

public class ConnectorImpl
        extends ConnectorGrpc.ConnectorImplBase {

    public ThreadLocal<GetDataResponse> initialMetadata = new ThreadLocal<GetDataResponse>();

    public void getData(DataRequest request, StreamObserver<DataChunk> responseObserver) {
        String str = request.getConnection().getConnectionString();
        String sql = request.getParameters().getStatement();

        Connection conn = null;
        Statement stmt = null;

        try{

            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to database...");
            //TODO: The connection info should be exstracted from the connection string
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:54321/postgres", "postgres", "postgres");

            stmt = conn.createStatement();


            DataChunk.Builder d;

            ResultSet rs = stmt.executeQuery(sql);

            ResultSetMetaData rsmd = rs.getMetaData();

            GetDataResponse.Builder builder = GetDataResponse.newBuilder();

            for(int j = 1 ; j <= rsmd.getColumnCount(); j++){

                builder.addFieldInfo(FieldInfo.newBuilder()
                        .setName(rsmd.getColumnName(j))
                        .setSemanticTypeValue(TypeConversionUtil.getSymanticType(rsmd.getColumnTypeName(j)))
                        .setFieldAttributes(
                                FieldAttributes.newBuilder().
                                        setTypeValue(TypeConversionUtil.getFieldAttrType(rsmd.getColumnTypeName(j)))
                                        .build()
                        )
                ).build();

                System.out.print("Name: "+ rsmd.getColumnName(j));
                System.out.println(" - type: "+ rsmd.getColumnTypeName(j));
            }

            initialMetadata.set(builder.build());




            int columnCount = rsmd.getColumnCount();

            while(rs.next()){
                d = DataChunk.newBuilder();

                for(int i = 1; i <= columnCount; i++){

                    try {
                        //TODO: Cleanup this and make an extract logic to its own thread
                        if (rsmd.getColumnTypeName(i) == "varchar") {
                            d.addStringBucket(rs.getString(i));
                            d.addStringCodes(i);
                        } else if (rsmd.getColumnTypeName(i) == "int4") {
                            d.addDoubleBucket(rs.getInt(i));
                            d.addNumberCodes(i);
                        } else {
                            d.addStringBucket(rs.getString(i));
                            d.addStringCodes(i);
                        }
                    }
                    catch(Exception e){
                        d.addStringBucket("-");
                        d.addStringCodes(i);
                    }
                }

                responseObserver.onNext(d.build());
            }

            rs.close();
            stmt.close();
        }catch(SQLException se){
            se.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){

            }
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        responseObserver.onCompleted();

        System.out.println("Data sent!");
    }

    public GetDataResponse getDataResponseMetadata(){
        return initialMetadata.get();
    }
}
