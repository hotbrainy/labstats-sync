package edu.sydneyuni.myuni;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.ApiGatewayProxyResponse;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.services.RoomStationDao;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class RoomStationsQuery implements RequestStreamHandler {

    private final RoomStationDao dao;
    private static final Logger logger = LogManager.getLogger(RoomStationsQuery.class);

    @SuppressWarnings("unused") // Lambda needs a public no-arg constructor
    public RoomStationsQuery() {
        this(new RoomStationDao(AmazonDynamoDBClientBuilder.defaultClient(), System.getenv("TABLE_NAME")));
    }

    RoomStationsQuery(RoomStationDao dao) {
        this.dao = dao;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        try {
            RoomStation[] roomStations = getDao().getLatestRoomStations();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> responseHeaders = new HashMap<>(1);
            responseHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");
            mapper.writeValue(outputStream, mapper.writeValueAsString(
                    new ApiGatewayProxyResponse.ApiGatewayProxyResponseBuilder()
                            .withStatusCode(200)
                            .withHeaders(responseHeaders)
                            .withBody(mapper.writeValueAsString(roomStations))
                            .build()
            ));
        } catch (Exception e) {
            logger.error("Error getting RoomStations from DynamoDB", e);
        }
    }

    private RoomStationDao getDao() {
        return dao;
    }
}
