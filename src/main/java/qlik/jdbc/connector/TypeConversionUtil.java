package qlik.jdbc.connector;

import qlik.connect.GrpcServer;

import java.sql.Types;

public class TypeConversionUtil {
    static public int getFieldAttrType(int type){
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
                return GrpcServer.FieldAttrType.TEXT_VALUE;
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.BIT:
            case Types.BOOLEAN:
            case Types.SMALLINT:
            case Types.TINYINT:
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
                return GrpcServer.FieldAttrType.REAL_VALUE;
            case Types.DATE:
                return GrpcServer.FieldAttrType.DATE_VALUE;
            case Types.TIME:
                return GrpcServer.FieldAttrType.TIME_VALUE;
            case Types.TIMESTAMP:
                return GrpcServer.FieldAttrType.TIMESTAMP_VALUE;
            case Types.ARRAY:
            case Types.BINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
            case Types.NCLOB:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.ROWID:
            case Types.SQLXML:
            case Types.STRUCT:
            case Types.VARBINARY:
                return GrpcServer.FieldAttrType.UNKNOWN_VALUE;

        }

        return GrpcServer.FieldAttrType.UNKNOWN_VALUE;
    }

    static public int getSymanticType(int type){
        switch (type){
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE;
        }

        return GrpcServer.SemanticType.DEFAULT_VALUE;
    }

    static public void addNumber(GrpcServer.DataChunk.Builder dataChunkBuilder, double numberValue){
        dataChunkBuilder.addNumberCodes(dataChunkBuilder.getDoubleBucketCount());
        dataChunkBuilder.addDoubleBucket(numberValue);
        dataChunkBuilder.addStringCodes(-1);
    }

    static public void addInteger(GrpcServer.DataChunk.Builder dataChunkBuilder, long intValue){
        dataChunkBuilder.addNumberCodes(-2);
        dataChunkBuilder.addNumberCodes(intValue);
        dataChunkBuilder.addStringCodes(-1);
    }

    static public void addString(GrpcServer.DataChunk.Builder dataChunkBuilder, String stringValue){
        if(stringValue == null){
            addNull(dataChunkBuilder);
        }
        else if(stringValue == ""){
            dataChunkBuilder.addStringCodes(-2);
            dataChunkBuilder.addNumberCodes(-1);
        }
        else {
            int nextIndexInStringBucket = dataChunkBuilder.getStringBucketCount();
            dataChunkBuilder.addStringCodes(nextIndexInStringBucket);
            dataChunkBuilder.addStringBucket(stringValue);
            dataChunkBuilder.addNumberCodes(-1);
        }
    }

    static public void addNull(GrpcServer.DataChunk.Builder dataChunkBuilder){
        dataChunkBuilder.addStringCodes(-1);
        dataChunkBuilder.addNumberCodes(-1);
    }
}
