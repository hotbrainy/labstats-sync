package edu.sydneyuni.myuni;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.sydneyuni.myuni.models.RoomStation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RoomStationsQuery implements RequestStreamHandler {

    private final ObjectReader reader;
    private final ObjectWriter writer;
    private final AmazonS3 s3;
    private final String bucketName;

    @SuppressWarnings("unused")
    RoomStationsQuery() {
        this(AmazonS3ClientBuilder.defaultClient(), System.getenv("S3_BUCKET"));
        if (bucketName == null || bucketName.length() == 0) {
            throw new IllegalArgumentException("No AWS S3_BUCKET specified");
        }
    }

    RoomStationsQuery(AmazonS3 s3, String bucketName) {
        reader = new ObjectMapper().readerFor(RoomStation[].class);
        writer = new ObjectMapper().writerFor(RoomStation[].class);
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        RoomStation[] roomStations = getRoomStationsS3();
        getWriter().writeValue(outputStream, roomStations);
    }

    RoomStation[] getRoomStationsS3() throws IOException {
        try (S3Object object = getS3().getObject(getBucketName(), "current")) {
            return getReader().readValue(object.getObjectContent());
        }
    }

    ObjectReader getReader() {
        return reader;
    }

    ObjectWriter getWriter() {
        return writer;
    }

    private AmazonS3 getS3() {
        return s3;
    }

    private String getBucketName() {
        return bucketName;
    }
}
