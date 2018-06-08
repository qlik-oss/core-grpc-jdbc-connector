package qlik.jdbc.connector;

import io.grpc.stub.StreamObserver;
import qlik.connect.ConnectorGrpc;
import qlik.connect.GrpcServer.*;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

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

        Map<String, String> map = new LinkedHashMap<String, String>();

        for(String keyValue: str.split(" *; *")){
            String[] pairs = keyValue.split(" *= *", 2);
            map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }

        String connectionString = "jdbc:" + map.get("driver") + "://" + map.get("host") + ":" + map.get("port") + "/" + map.get("database");

        Connection conn = null;
        Statement stmt = null;

        try{
            //TODO: Make driver configurable from the outside
            System.out.println("Connecting to database...");
            //TODO: The connection info should be exstracted from the connection string
            conn = DriverManager.getConnection(connectionString, request.getConnection().getUser(), request.getConnection().getPassword());

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
                        switch (rsmd.getColumnType(i+1)) {
                            case Types.CHAR:
                            case Types.VARCHAR:
                            case Types.LONGNVARCHAR:
                            case Types.LONGVARCHAR:
                            case Types.NCHAR:
                            case Types.NVARCHAR:
                                TypeConversionUtil.addString(dataChunkBuilder, rs.getString(i + 1));
                                break;
                            case Types.TINYINT:
                            case Types.INTEGER:
                                TypeConversionUtil.addInteger(dataChunkBuilder, rs.getInt(i + 1));
                                break;
                            case Types.BIGINT:
                                TypeConversionUtil.addInteger(dataChunkBuilder, rs.getLong(i + 1));
                                break;
                            case Types.BOOLEAN:
                            case Types.BIT:
                                TypeConversionUtil.addInteger(dataChunkBuilder, rs.getBoolean(i + 1) ? 1 : 0);
                                break;
                            case Types.DECIMAL:
                            case Types.NUMERIC:
                            case Types.DOUBLE:
                                TypeConversionUtil.addNumber(dataChunkBuilder, rs.getDouble(i + 1));
                                break;
                            case Types.FLOAT:
                            case Types.REAL:
                                TypeConversionUtil.addNumber(dataChunkBuilder, rs.getFloat(i + 1));
                                break;
                            case Types.DATE:
                                TypeConversionUtil.addNumber(dataChunkBuilder, rs.getDate(i + 1).getTime() / 1000);
                                break;
                            case Types.TIME:
                                TypeConversionUtil.addNumber(dataChunkBuilder, rs.getTime(i + 1).getTime() / 1000);
                                break;
                            case Types.TIMESTAMP:
                                TypeConversionUtil.addNumber(dataChunkBuilder, rs.getTimestamp(i + 1).getTime() / 1000);
                                break;
                            default:
                                TypeConversionUtil.addString(dataChunkBuilder, rs.getString(i + 1));
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
    }

    public GetDataResponse getDataResponseMetadata(){
        return initialMetadata.get();
    }
}
