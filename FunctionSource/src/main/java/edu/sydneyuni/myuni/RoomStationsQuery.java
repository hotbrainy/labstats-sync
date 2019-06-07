package edu.sydneyuni.myuni;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.services.RoomStationDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RoomStationsQuery implements RequestStreamHandler {

    private final RoomStationDao dao;

    @SuppressWarnings("unused") // Lambda needs a public no-arg constructor
    public RoomStationsQuery() {
        this(new RoomStationDao(AmazonDynamoDBClientBuilder.defaultClient(), System.getenv("TABLE_NAME")));
    }

    RoomStationsQuery(RoomStationDao dao) {
        this.dao = dao;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        RoomStation[] roomStations = getDao().getLatestRoomStations();
        getDao().getWriter().writeValue(outputStream, roomStations);
    }

    private RoomStationDao getDao() {
        return dao;
    }
}
