package edu.sydneyuni.myuni;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.sydneyuni.myuni.models.LabStatsConfig;
import edu.sydneyuni.myuni.models.LabStatsGroupStatusResponse;
import edu.sydneyuni.myuni.models.RoomStation;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class LabStatsSync implements RequestStreamHandler {

    private final ObjectReader reader;
    private final ObjectWriter writer;
    private final AmazonS3 s3;
    private final String bucketName;
    private final String labStatsApiKey;
    private CloseableHttpClient client;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/HH-mm");
    private static final Logger logger = LogManager.getLogger(LabStatsSync.class);
    private final LabStatsConfig config;

    @SuppressWarnings("unused")
    public LabStatsSync() {
        this(AmazonS3ClientBuilder.defaultClient(), System.getenv("S3_BUCKET"), System.getenv("LABSTATS_API_KEY"));
        if (bucketName == null || bucketName.length() == 0) {
            throw new IllegalArgumentException("No AWS S3_BUCKET specified");
        }
        // https://support.labstats.com/article/public-api/
        if (labStatsApiKey == null || labStatsApiKey.length() == 0) {
            throw new IllegalArgumentException("No LabStats LABSTATS_API_KEY specified");
        }
    }

    /*
    public static void main(String[] args) throws IOException {
        LabStatsSync labStatsSync = new LabStatsSync(null, null, "");
        RoomStation[] response = labStatsSync.getLabStatsRoomStations(labStatsSync.getConfig());
    }*/

    LabStatsSync(AmazonS3 s3, String bucketName, String labStatsApiKey) {
        reader = new ObjectMapper().readerFor(LabStatsGroupStatusResponse.class);
        writer = new ObjectMapper().writerFor(RoomStation[].class);
        this.s3 = s3;
        this.bucketName = bucketName;
        this.labStatsApiKey = labStatsApiKey;
        client = HttpClientBuilder.create()
                .setDefaultHeaders(Collections.singletonList(
                        new BasicHeader("Authorization", labStatsApiKey))).build();
        try {
            config = new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("labstats.json"), LabStatsConfig.class);
        } catch (IOException e) {
            logger.error("Error opening labstats.json", e);
            throw new IllegalStateException("Error opening labstats.json", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        try {
            RoomStation[] roomStations = getLabStatsRoomStations(getConfig());
            syncRoomStationsS3(getBucketKey(), StorageClass.Glacier, roomStations);
            syncRoomStationsS3("current", StorageClass.Standard, roomStations);
        } catch (IOException e) {
            logger.error("Error syncing LabStats to S3", e);
        }
    }

    RoomStation[] getLabStatsRoomStations(LabStatsConfig config) throws IOException {
        List<RoomStation> roomStations = new ArrayList<>();
        for (Map.Entry<String, LabStatsConfig.BuildingRooms> building : config.entrySet()) {
            for (Map.Entry<String, LabStatsConfig.BuildingRooms.RoomStationGroups> room : building.getValue().entrySet()) {
                int on = 0;
                int busy = 0;
                int offline = 0;
                for (int pcGroup : room.getValue().getPcGroups()) {
                    LabStatsGroupStatusResponse response = getLabStatsGroupStatus(pcGroup);
                    on += response.getOn();
                    busy += response.getBusy();
                    offline += response.getOffline();
                }
                for (int podGroup : room.getValue().getPodGroups()) {
                    LabStatsGroupStatusResponse response = getLabStatsGroupStatus(podGroup);
                    on += response.getOn();
                    busy += response.getBusy();
                    offline += response.getOffline();
                }
                roomStations.add(new RoomStation(building.getKey(), room.getKey(), on, busy, offline));
            }
        }
        return roomStations.toArray(new RoomStation[0]);
    }

    LabStatsGroupStatusResponse getLabStatsGroupStatus(int groupId) throws IOException {
        String uri = String.format("https://sea-api.labstats.com/groups/%d/status", groupId);
        try (CloseableHttpResponse response = getClient().execute(new HttpGet(uri))) {
            return getReader().readValue(response.getEntity().getContent());
        } catch (Exception e) {
            throw new IOException("Error getting LabStats GET /" + uri, e);
        }
    }

    void syncRoomStationsS3(String key, StorageClass storageClass, RoomStation[] roomStations) throws IOException {
        try {
            byte[] json = getWriter().writeValueAsBytes(roomStations);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("application/json");
            objectMetadata.setContentLength(json.length);
            getS3().putObject(new PutObjectRequest(getBucketName(), key, new ByteArrayInputStream(json), objectMetadata).withStorageClass(storageClass));
        } catch (Exception e) {
            throw new IOException("Error uploading to S3 /" + getS3().getUrl(getBucketName(), key).toExternalForm(), e);
        }
    }

    private String getBucketName() {
        return bucketName;
    }

    String getBucketKey(Date date) {
        return getDateFormat().format(date);
    }

    String getBucketKey() {
        return getBucketKey(new Date());
    }

    public String getLabStatsApiKey() {
        return labStatsApiKey;
    }

    ObjectReader getReader() {
        return reader;
    }

    private ObjectWriter getWriter() {
        return writer;
    }

    private AmazonS3 getS3() {
        return s3;
    }

    CloseableHttpClient getClient() {
        return client;
    }

    SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    LabStatsConfig getConfig() {
        return config;
    }
}
