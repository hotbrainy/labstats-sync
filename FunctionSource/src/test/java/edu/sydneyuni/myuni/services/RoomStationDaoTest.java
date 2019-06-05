package edu.sydneyuni.myuni.services;

import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import edu.sydneyuni.myuni.models.RoomStation;
import edu.sydneyuni.myuni.models.RoomStationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {"dynamodb"})
class RoomStationDaoTest {

    private AmazonDynamoDB dynamoDBMock;
    private AmazonDynamoDB dynamoDB;
    private final String tableName = "abcd1992";

    @BeforeEach
    void setUp() {
        dynamoDBMock = Mockito.mock(AmazonDynamoDB.class);
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        builder.setEndpointConfiguration(new AwsClientBuilder
                .EndpointConfiguration("http://localhost:4569", "us-east-1"));
        builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("access", "secret")));
        dynamoDB = builder.build();
    }

    @Test
    void testGetBucketKey() throws ParseException {
        RoomStationDao dao = new RoomStationDao(dynamoDBMock, tableName);

        assertNotNull(dao.getDateKey());
        assertNotNull(dao.getDateFormat().parse(dao.getDateKey()));
        assertEquals("1970-01-02/10-00", dao.getDateKey(new Date(TimeUnit.DAYS.toMillis(1))));
        assertEquals("1970-01-01/13-00", dao.getDateKey(new Date(TimeUnit.HOURS.toMillis(3))));
        assertEquals("1970-01-01/10-02", dao.getDateKey(new Date(TimeUnit.MINUTES.toMillis(2))));
        assertEquals("1971-05-16/10-00", dao.getDateKey(new Date(TimeUnit.DAYS.toMillis(500))));
    }

    @Test
    void testInsertItem() throws IOException {
        RoomStationDao dao = new RoomStationDao(dynamoDB, tableName);
        dynamoDB.createTable(new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(new KeySchemaElement(RoomStationDao.HASH, KeyType.HASH), new KeySchemaElement(RoomStationDao.RANGE, KeyType.RANGE))
                .withAttributeDefinitions(new AttributeDefinition(RoomStationDao.HASH, ScalarAttributeType.N), new AttributeDefinition(RoomStationDao.RANGE, ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L)));

        RoomStation[] arr = RoomStationTest.generateArray();
        dao.insert(arr);
        assertArrayEquals(arr, dao.getLatestRoomStations());
    }
}
