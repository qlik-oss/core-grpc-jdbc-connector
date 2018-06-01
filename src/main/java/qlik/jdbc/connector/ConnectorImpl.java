package qlik.jdbc.connector;

import com.google.protobuf.Descriptors;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import qlik.connect.ConnectorGrpc;
import qlik.connect.GrpcServer;
import qlik.connect.GrpcServer.*;



import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;

public class ConnectorImpl
        extends ConnectorGrpc.ConnectorImplBase {

    public ThreadLocal<GetDataResponse> initialMetadata = new ThreadLocal<GetDataResponse>();

    private GetDataResponse getDataResponseHeader(ResultSetMetaData rsmd) throws SQLException {

        GetDataResponse.Builder builder = GetDataResponse.newBuilder();

        for(int j = 1 ; j <= rsmd.getColumnCount(); j++){
            builder.addFieldInfo(FieldInfo.newBuilder()
                    .setName(rsmd.getColumnName(j))
                    .setSemanticTypeValue(TypeConversionUtil.getSymanticType(rsmd.getColumnType(j)))
                    .setFieldAttributes(
                            FieldAttributes.newBuilder().
                                    setTypeValue(TypeConversionUtil.getFieldAttrType(rsmd.getColumnType(j)))
                                    .build()
                    )
            ).build();
        }

        return builder.build();
    }

    public void getData(DataRequest request, StreamObserver<DataChunk> responseObserver) {
        String str = request.getConnection().getConnectionString();
        String sql = request.getParameters().getStatement();

        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;

        try{
            //TODO: Make driver configurable from the outside


            System.out.println("Connecting to database...");
            //TODO: The connection info should be exstracted from the connection string
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:54321/postgres", "postgres", "postgres");

            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            GetDataResponse dataResponse = getDataResponseHeader(rsmd);
            initialMetadata.set(dataResponse);

            int columnCount = rsmd.getColumnCount();

            DataChunk.Builder dataChunkBuilder;

            dataChunkBuilder = DataChunk.newBuilder();

            int NR_OF_ROWS = 200;
            int rowCount = 0;

            while(rs.next()){
                rowCount++;

                for(int i = 0; i < columnCount; i++){

                    try {
                        //TODO: Cleanup this and make an extract logic to its own thread
                        //need to looka at the orignal type
//
//                        if(rsmd.getColumnType(i+1) == Types.CHAR){
//                            rs.getDouble(i+1 );
//                            rs.getFloat( i+1 );
//                        }

                        //System.out.println(rsmd.getColumnType(i+1) + " - " + dataResponse.getFieldInfo(i).getFieldAttributesOrBuilder().getTypeValue());

//                        rsmd.getColumnType(i)
                        if (dataResponse.getFieldInfo(i).getFieldAttributesOrBuilder().getTypeValue() == FieldAttrType.TEXT_VALUE) {
                            TypeConversionUtil.addString(dataChunkBuilder, rs.getString(i+1));
                        }

                        else if (rsmd.getColumnType(i+1) == Types.TINYINT ){
                            TypeConversionUtil.addInteger(dataChunkBuilder, rs.getInt(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.INTEGER ){
                            TypeConversionUtil.addInteger(dataChunkBuilder, rs.getInt(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.BIGINT ){
                            TypeConversionUtil.addInteger(dataChunkBuilder, rs.getLong(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.BOOLEAN){
                            TypeConversionUtil.addInteger(dataChunkBuilder, rs.getBoolean(i+1) ? 1 : 0);
                        }
                        else if (rsmd.getColumnType(i+1) == Types.BIT){
                            TypeConversionUtil.addInteger(dataChunkBuilder, rs.getBoolean(i+1) ? 1 : 0);
                        }
                        else if (rsmd.getColumnType(i+1) == Types.DECIMAL ){
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getDouble(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.NUMERIC ){
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getDouble(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.DOUBLE ){
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getDouble(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.FLOAT ){
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getFloat(i+1));
                        }
                        else if (rsmd.getColumnType(i+1) == Types.REAL ){
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getFloat(i+1));
                        } else if (dataResponse.getFieldInfo(i).getFieldAttributesOrBuilder().getTypeValue() == FieldAttrType.DATE_VALUE) {
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getDate(i+1).getTime() / 1000);
                        } else if (dataResponse.getFieldInfo(i).getFieldAttributesOrBuilder().getTypeValue() == FieldAttrType.TIME_VALUE) {
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getTime(i+1).getTime() / 1000);
                        } else if (dataResponse.getFieldInfo(i).getFieldAttributesOrBuilder().getTypeValue() == FieldAttrType.TIMESTAMP_VALUE) {
                            TypeConversionUtil.addNumber(dataChunkBuilder, rs.getTimestamp(i+1).getTime() / 1000);
                        } else {
                            TypeConversionUtil.addString(dataChunkBuilder, rs.getString(i+1));
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        throw e;
                    }
                }

                if(rowCount % NR_OF_ROWS == 0){
                    responseObserver.onNext(dataChunkBuilder.build());
                    dataChunkBuilder = DataChunk.newBuilder();
                }
            }

            //Send the remainder of rows
            if(rowCount % NR_OF_ROWS != 0){
                responseObserver.onNext(dataChunkBuilder.build());
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

        System.out.println("Fetching header");
        return initialMetadata.get();
    }
}
