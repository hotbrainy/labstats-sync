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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LabStatsSync implements RequestStreamHandler {

    private final ObjectReader reader;
    private final ObjectWriter writer;
    private final AmazonS3 s3;
    private final String bucketName;
    private final String customerId;
    private CloseableHttpClient client;
    private final List<String> publicDataSets;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/hh-mm");
    private static final Logger logger = LogManager.getLogger(LabStatsSync.class);

    @SuppressWarnings("unused")
    public LabStatsSync() {
        this(AmazonS3ClientBuilder.defaultClient(), System.getenv("S3_BUCKET"), System.getenv("CUSTOMER_ID"));
        if (bucketName == null || bucketName.length() == 0) {
            throw new IllegalArgumentException("No AWS S3_BUCKET specified");
        }
        // https://support.labstats.com/article/public-api/
        if (customerId == null || customerId.length() == 0) {
            throw new IllegalArgumentException("No LabStats CUSTOMER_ID specified");
        }
    }

    LabStatsSync(AmazonS3 s3, String bucketName, String customerId) {
        reader = new ObjectMapper().readerFor(RoomStation.class);
        writer = new ObjectMapper().writerFor(RoomStation[].class);
        this.s3 = s3;
        this.bucketName = bucketName;
        this.customerId = customerId;
        client = HttpClientBuilder.create()
                .setDefaultHeaders(Collections.singletonList(
                        new BasicHeader("Authorization", customerId))).build();
        publicDataSets = Arrays.asList("H70", "F03", "A18", "F07", "A35",
                "F10", "C12", "C15", "A16", "C24", "C41", "C43", "G02",
                "D05", "N01", "J02", "M02", "z_SIT_2019");
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        try {
            RoomStation[] roomStations = getRoomStationsLabStats(getPublicDataSets());
            syncRoomStationsS3(getBucketKey(), StorageClass.Glacier, roomStations);
            syncRoomStationsS3("current", StorageClass.Standard, roomStations);
        } catch (IOException e) {
            logger.error("Error syncing LabStats to S3", e);
        }
    }

    RoomStation[] getRoomStationsLabStats(List<String> publicDataSets) throws IOException {
        RoomStation[] roomStationArray = new RoomStation[publicDataSets.size()];
        for (int i = 0; i < publicDataSets.size(); i++) {
            roomStationArray[i] = getRoomStationsLabStats(publicDataSets.get(i));
        }
        return roomStationArray;
    }

    RoomStation getRoomStationsLabStats(String publicDataSet) throws IOException {
        String uri = "https://portal.labstats.com/api/public/GetPublicApiData/" + publicDataSet;
        try (CloseableHttpResponse response = getClient().execute(new HttpGet(uri))) {
            // TODO: Check content
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

    String getBucketKey() {
        return getDateFormat().format(new Date());
    }

    public String getCustomerId() {
        return customerId;
    }

    private ObjectReader getReader() {
        return reader;
    }

    private ObjectWriter getWriter() {
        return writer;
    }

    private AmazonS3 getS3() {
        return s3;
    }

    private CloseableHttpClient getClient() {
        return client;
    }

    List<String> getPublicDataSets() {
        return publicDataSets;
    }

    private SimpleDateFormat getDateFormat() {
        return dateFormat;
    }
}
