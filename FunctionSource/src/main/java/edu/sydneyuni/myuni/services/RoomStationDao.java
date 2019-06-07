package edu.sydneyuni.myuni.services;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.sydneyuni.myuni.models.RoomStation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RoomStationDao implements Dao<RoomStation[]> {

    private final AmazonDynamoDB dynamoDB;
    private final String tableName;
    private static final String HASH = "Key";
    private static final String RANGE = "Range";
    private static final String VALUE = "Value";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/HH-mm");
    private final ObjectWriter writer = new ObjectMapper().writerFor(RoomStation[].class);
    private final ObjectReader reader = new ObjectMapper().readerFor(RoomStation[].class);

    public RoomStationDao(AmazonDynamoDB dynamoDB, String tableName) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
        if (tableName == null || tableName.length() == 0) {
            throw new IllegalArgumentException("No DynamoDB Table Name specified");
        }
    }

    @Override
    public void insert(RoomStation[] item) throws IOException {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(HASH, new AttributeValue().withN("0"));
        values.put(RANGE, new AttributeValue().withS(getDateFormat().format(new Date())));
        values.put(VALUE, new AttributeValue().withS(getWriter().writeValueAsString(item)));
        PutItemResult result = dynamoDB.putItem(tableName, values);
        // https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_PutItem.html#API_PutItem_ResponseElements
        if (result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
            throw new IOException(String.format("%s: Error inserting RoomStations into DynamoDB", result.getSdkResponseMetadata().getRequestId()));
        }
    }

    public RoomStation[] getLatestRoomStations() throws IOException {
        QueryResult result = dynamoDB.query(new QueryRequest().withTableName(tableName)
                .withLimit(1)
                .withScanIndexForward(false)
                .withProjectionExpression(VALUE)
                .withKeyConditionExpression(String.format("%s = :v_k AND %s <= :v_r", HASH, RANGE))
                .addExpressionAttributeValuesEntry(":v_k", new AttributeValue().withN("0"))
                .addExpressionAttributeValuesEntry(":v_r", new AttributeValue()
                        .withS(getDateFormat()
                                .format(new Date()))));
        // https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Query.html#API_Query_ResponseElements
        if (result.getSdkHttpMetadata().getHttpStatusCode() != 200 || result.getCount() <= 0) {
            throw new IOException(String.format("%s: Error querying RoomStations into DynamoDB", result.getSdkResponseMetadata().getRequestId()));
        }
        return getReader().readValue(result.getItems().get(0).get(VALUE).getS());
    }

    public AmazonDynamoDB getDynamoDB() {
        return dynamoDB;
    }

    private SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    String getDateKey() {
        return getDateKey(new Date());
    }

    private String getDateKey(Date date) {
        return getDateFormat().format(date);
    }

    public ObjectWriter getWriter() {
        return writer;
    }

    private ObjectReader getReader() {
        return reader;
    }
}
