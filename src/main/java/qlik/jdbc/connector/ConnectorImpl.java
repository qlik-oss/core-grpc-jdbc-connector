package qlik.jdbc.connector;

import io.grpc.stub.StreamObserver;
import qlik.connect.ConnectorGrpc;
import qlik.connect.GrpcServer.*;

import java.sql.*;
import java.util.*;

import static java.util.stream.Collectors.joining;

public class ConnectorImpl
        extends ConnectorGrpc.ConnectorImplBase {

    private int fetchSize;
    private int maxDataChunkSize;
    private static final Set<String> IGNORE_PARAMS = new HashSet<String>(Arrays.asList(new String[]{"host","port","provider","driver"}));

    public ConnectorImpl(Integer fetchSize, Integer maxDataChunkSize){
      super();
      this.fetchSize = fetchSize;
      this.maxDataChunkSize = maxDataChunkSize;
    }

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

    private Map<String, String> parseConnectionString(final String connection) {
      Map<String, String> map = new LinkedHashMap<String, String>();

      for(String keyValue: connection.split(" *; *")){
        String[] pairs = keyValue.split(" *= *", 2);
        map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
    } 

      return map;
    }

    public void getData(DataRequest request, StreamObserver<DataChunk> responseObserver) {
        String sql = request.getParameters().getStatement();
        Map<String, String> map = this.parseConnectionString(request.getConnection().getConnectionString());
        String connectionString = String.format("jdbc:%s://", map.get("driver"));

        if (map.containsKey("host") && map.containsKey("port")) {
          // both host and port defined, assume regular JDBC driver in
          // 'jdbc:driver://host:port/database' format
          connectionString += String.format("%s:%d/%s", map.get("host"), map.get("port"), map.get("database"));
        } else if (map.containsKey("host") || map.containsKey("port")) {
          // either 'host' or 'port' is defined, but not both
          throw new Error("'host' or 'port' is missing, assumed none or both to exist in connection string");
        } else {
          // no 'host' or 'path' defined, assume a driver like Athena with custom properties
          // so just pass along all of them and let the JDBC driver throw exception if needed
          connectionString += map.entrySet().stream()
            .filter(e -> !IGNORE_PARAMS.contains(e.getKey()))
            .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
            .collect(joining(";"));
        }

        Connection conn = null;
        Statement stmt = null;

        try{
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(connectionString, request.getConnection().getUser(), request.getConnection().getPassword());
            stmt = conn.createStatement();


            if(fetchSize > 0) {
              //Enable fetching data in chunks from the database to avoid loading everything into memory
              conn.setAutoCommit(false);
              stmt.setFetchSize(fetchSize);
            }

            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            GetDataResponse dataResponse = getDataResponseHeader(rsmd);
            initialMetadata.set(dataResponse);

            int columnCount = rsmd.getColumnCount();

            DataChunk.Builder dataChunkBuilder;

            dataChunkBuilder = DataChunk.newBuilder();

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

                if(rowCount % maxDataChunkSize == 0){
                    responseObserver.onNext(dataChunkBuilder.build());
                    dataChunkBuilder = DataChunk.newBuilder();
                }
            }

            //Send the remainder of rows
            if(rowCount % maxDataChunkSize != 0){
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
