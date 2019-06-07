package edu.sydneyuni.myuni;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.services.RoomStationDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RoomStationsQuery implements RequestHandler<Object, String> {

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
        RoomStation[] roomStations = getDao().getLatestRoomStations();
        getDao().getWriter().writeValue(outputStream, roomStations);
    }

    private RoomStationDao getDao() {
        return dao;
    }

    @Override
    public String handleRequest(Object o, Context context) {
        try {
            RoomStation[] roomStations = getDao().getLatestRoomStations();
            return getDao().getWriter().writeValueAsString(roomStations);
        } catch (Exception e) {
            logger.error("Error getting RoomStations from DynamoDB", e);
        }
        return "[]";
    }
}
