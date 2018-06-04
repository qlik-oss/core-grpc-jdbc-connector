package qlik.jdbc.connector;
import java.sql.Types;

import qlik.connect.GrpcServer;
import qlik.connect.GrpcServer.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeConversionUtilTest {

    @Test
    void getFieldAttrTypeString() {
        assertEquals(GrpcServer.FieldAttrType.TEXT_VALUE, TypeConversionUtil.getFieldAttrType(Types.CHAR));
        assertEquals(GrpcServer.FieldAttrType.TEXT_VALUE, TypeConversionUtil.getFieldAttrType(Types.VARCHAR));
        assertEquals(GrpcServer.FieldAttrType.TEXT_VALUE, TypeConversionUtil.getFieldAttrType(Types.LONGNVARCHAR));
        assertEquals(GrpcServer.FieldAttrType.TEXT_VALUE, TypeConversionUtil.getFieldAttrType(Types.LONGVARCHAR));
        assertEquals(GrpcServer.FieldAttrType.TEXT_VALUE, TypeConversionUtil.getFieldAttrType(Types.NCHAR));
        assertEquals(GrpcServer.FieldAttrType.TEXT_VALUE, TypeConversionUtil.getFieldAttrType(Types.NVARCHAR));
    }

    @Test
    void getFieldAttrTypeWithIntegers(){
        assertEquals(GrpcServer.FieldAttrType.INTEGER_VALUE, TypeConversionUtil.getFieldAttrType(Types.INTEGER));
        assertEquals(GrpcServer.FieldAttrType.INTEGER_VALUE, TypeConversionUtil.getFieldAttrType(Types.BIGINT));
        assertEquals(GrpcServer.FieldAttrType.INTEGER_VALUE, TypeConversionUtil.getFieldAttrType(Types.BIT));
        assertEquals(GrpcServer.FieldAttrType.INTEGER_VALUE, TypeConversionUtil.getFieldAttrType(Types.BOOLEAN));
        assertEquals(GrpcServer.FieldAttrType.INTEGER_VALUE, TypeConversionUtil.getFieldAttrType(Types.SMALLINT));
        assertEquals(GrpcServer.FieldAttrType.INTEGER_VALUE, TypeConversionUtil.getFieldAttrType(Types.TINYINT));
    }

    @Test
    void getFieldAttrTypeNumbers() {
        assertEquals(GrpcServer.FieldAttrType.REAL_VALUE, TypeConversionUtil.getFieldAttrType(Types.NUMERIC));
        assertEquals(GrpcServer.FieldAttrType.REAL_VALUE, TypeConversionUtil.getFieldAttrType(Types.DECIMAL));
        assertEquals(GrpcServer.FieldAttrType.REAL_VALUE, TypeConversionUtil.getFieldAttrType(Types.DOUBLE));
        assertEquals(GrpcServer.FieldAttrType.REAL_VALUE, TypeConversionUtil.getFieldAttrType(Types.FLOAT));
        assertEquals(GrpcServer.FieldAttrType.REAL_VALUE, TypeConversionUtil.getFieldAttrType(Types.REAL));
    }

    @Test
    void getFieldAttrTypeDates() {
        assertEquals(GrpcServer.FieldAttrType.DATE_VALUE, TypeConversionUtil.getFieldAttrType(Types.DATE));
        assertEquals(GrpcServer.FieldAttrType.TIME_VALUE, TypeConversionUtil.getFieldAttrType(Types.TIME));
        assertEquals(GrpcServer.FieldAttrType.TIMESTAMP_VALUE, TypeConversionUtil.getFieldAttrType(Types.TIMESTAMP));
    }

    @Test
    void getFieldAttrTypeUnknown() {
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.ARRAY));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.BINARY));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.BLOB));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.CLOB));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.DATALINK));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.DISTINCT));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.JAVA_OBJECT));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.LONGVARBINARY));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.NCLOB));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.NULL));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.OTHER));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.REF));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.ROWID));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.SQLXML));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.STRUCT));
        assertEquals(GrpcServer.FieldAttrType.UNKNOWN_VALUE, TypeConversionUtil.getFieldAttrType(Types.VARBINARY));
    }

    @Test
    void getSymanticType() {
        assertEquals(GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE, TypeConversionUtil.getSymanticType(Types.DATE));
        assertEquals(GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE, TypeConversionUtil.getSymanticType(Types.TIME));
        assertEquals(GrpcServer.SemanticType.UNIX_SECONDS_SINCE_1970_UTC_VALUE, TypeConversionUtil.getSymanticType(Types.TIMESTAMP));

        assertEquals(GrpcServer.SemanticType.DEFAULT_VALUE, TypeConversionUtil.getSymanticType(Types.INTEGER));
    }

    @Test
    void addNumber() {
        DataChunk.Builder dataChunkBuilder = DataChunk.newBuilder();

        TypeConversionUtil.addNumber(dataChunkBuilder, 12.345);
        DataChunk dataChunk = dataChunkBuilder.build();

        assertEquals(-1, dataChunk.getStringCodes(0));
        assertEquals(0, dataChunk.getNumberCodes(0));
        assertEquals(12.345, dataChunk.getDoubleBucket(0));
    }

    @Test
    void addInteger() {
        DataChunk.Builder dataChunkBuilder = DataChunk.newBuilder();

        TypeConversionUtil.addInteger(dataChunkBuilder, 12345);
        DataChunk dataChunk = dataChunkBuilder.build();

        assertEquals(-1, dataChunk.getStringCodes(0));
        assertEquals(-2, dataChunk.getNumberCodes(0));
        assertEquals(12345, dataChunk.getNumberCodes(1));
    }

    @Test
    void addStringEmpty() {
        DataChunk.Builder dataChunkBuilder = DataChunk.newBuilder();

        TypeConversionUtil.addString(dataChunkBuilder, "");
        DataChunk dataChunk = dataChunkBuilder.build();

        assertEquals(-2, dataChunk.getStringCodes(0));
        assertEquals(-1, dataChunk.getNumberCodes(0));
    }

    @Test
    void addStringNull() {
        DataChunk.Builder dataChunkBuilder = DataChunk.newBuilder();

        TypeConversionUtil.addString(dataChunkBuilder, null);
        DataChunk dataChunk = dataChunkBuilder.build();

        assertEquals(-1, dataChunk.getStringCodes(0));
        assertEquals(-1, dataChunk.getNumberCodes(0));
    }

    @Test
    void addString() {
        DataChunk.Builder dataChunkBuilder = DataChunk.newBuilder();

        TypeConversionUtil.addString(dataChunkBuilder, "hello");
        DataChunk dataChunk = dataChunkBuilder.build();

        assertEquals(0, dataChunk.getStringCodes(0));
        assertEquals(-1, dataChunk.getNumberCodes(0));
        assertEquals("hello", dataChunk.getStringBucket(0));
    }

    @Test
    void addNull() {
        DataChunk.Builder dataChunkBuilder = DataChunk.newBuilder();

        TypeConversionUtil.addNull(dataChunkBuilder);
        DataChunk dataChunk = dataChunkBuilder.build();

        assertEquals(-1, dataChunk.getStringCodes(0));
        assertEquals(-1, dataChunk.getNumberCodes(0));
    }
}