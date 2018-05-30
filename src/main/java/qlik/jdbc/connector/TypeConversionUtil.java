package qlik.jdbc.connector;

import qlik.connect.GrpcServer;

public class TypeConversionUtil {
    static public int getFieldAttrType(String type){
        switch (type){
            case "varchar":
                return GrpcServer.FieldAttrType.TEXT_VALUE;
            case "text":
                return GrpcServer.FieldAttrType.TEXT_VALUE;
            case "int2":
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
            case "int4":
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
            case "int8":
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
            case "char":
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
            case "oid":
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
            case "float4":
                return GrpcServer.FieldAttrType.REAL_VALUE;
            case "float8":
                return GrpcServer.FieldAttrType.REAL_VALUE;
            case "timestamp":
                return GrpcServer.FieldAttrType.TIMESTAMP_VALUE;
            case "timestamptz":
                return GrpcServer.FieldAttrType.TIMESTAMP_VALUE;
            case "date":
                return GrpcServer.FieldAttrType.DATE_VALUE;
            case "numeric":
                return GrpcServer.FieldAttrType.REAL_VALUE;
            case "decimal":
                return GrpcServer.FieldAttrType.REAL_VALUE;
            case "bool":
                return GrpcServer.FieldAttrType.INTEGER_VALUE;
        }
        return 0;
    }

    static public int getSymanticType(String type){
        switch (type){
            case "timestamp":
                return GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE;
            case "timestamptz":
                return GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE;
            case "date":
                return GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE;
        }

        return GrpcServer.SemanticType.DEFAULT_VALUE;
    }
}
