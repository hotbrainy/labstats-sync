package edu.sydneyuni.myuni;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.USydCampuses;
import edu.sydneyuni.myuni.services.LabStatsClient;
import edu.sydneyuni.myuni.services.RoomStationDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LabStatsSync implements RequestStreamHandler {

    private final RoomStationDao dao;
    private final LabStatsClient labStatsClient;
    private static final Logger logger = LogManager.getLogger(LabStatsSync.class);
    private final USydCampuses uSydCampuses;

    @SuppressWarnings("unused") // Lambda needs a public no-arg constructor
    public LabStatsSync() {
        this(new RoomStationDao(AmazonDynamoDBClientBuilder.defaultClient(),
                System.getenv("TABLE_NAME")),
                new LabStatsClient(System.getenv("LABSTATS_API_KEY")));
    }

    LabStatsSync(RoomStationDao dao, LabStatsClient labStatsClient) {
        this.dao = dao;
        this.labStatsClient = labStatsClient;
        try {
            uSydCampuses = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("labstats.json"), USydCampuses.class);
        } catch (Exception e) {
            logger.error("Error opening labstats.json", e);
            throw new IllegalStateException("Error opening labstats.json", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        try {
            RoomStation[] roomStations =  getLabStatsClient().getLabStatsRoomStations(getUSydCampuses());
            getDao().insert(roomStations);
        } catch (IOException e) {
            logger.error("Error syncing LabStats to DynamoDB", e);
        }
    }

    private RoomStationDao getDao() {
        return dao;
    }

    private LabStatsClient getLabStatsClient() {
        return labStatsClient;
    }

    USydCampuses getUSydCampuses() {
        return uSydCampuses;
    }
}
