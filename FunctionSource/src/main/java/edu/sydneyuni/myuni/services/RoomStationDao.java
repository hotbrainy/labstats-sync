package edu.sydneyuni.myuni.services;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
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
    static final String HASH = "K";
    static final String RANGE = "R";
    private static final String VALUE = "V";
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
                .withKeyConditionExpression("K = :v_k AND R <= :v_r")
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

    SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    String getDateKey() {
        return getDateKey(new Date());
    }

    String getDateKey(Date date) {
        return getDateFormat().format(date);
    }

    public ObjectWriter getWriter() {
        return writer;
    }

    private ObjectReader getReader() {
        return reader;
    }
}
