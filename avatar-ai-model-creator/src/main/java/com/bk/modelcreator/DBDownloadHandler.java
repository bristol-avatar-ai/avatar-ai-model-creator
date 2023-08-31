package com.bk.modelcreator;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cloud.objectstorage.services.s3.model.GetObjectRequest;

import java.io.File;

public class DBDownloadHandler implements EventHandler<PropertiesLoadedEvent>{
    private EventBus eventBus;
    private PropertiesManager propertiesManager;
    private ApplicationState applicationState;
    public DBDownloadHandler(EventBus eventBus, PropertiesManager propertiesManager, ApplicationState applicationState)
    {
        this.eventBus = eventBus;
        this.propertiesManager = propertiesManager;
        this.applicationState = applicationState;
        eventBus.subscribe(PropertiesLoadedEvent.class, this);
    }
    @Override
    public void handle(PropertiesLoadedEvent event) {
        downloadDB();
    }

    private void downloadDB() {
        String apiKey = propertiesManager.getProperty("dbAPIKey");
        String serviceInstanceID = propertiesManager.getProperty("dbServiceInstanceID");
        String endpointURL = propertiesManager.getProperty("dbEndpointURL");
        String location = propertiesManager.getProperty("dbLocation");
        String bucketName = propertiesManager.getProperty("dbBucketName");
        String objectName = "data.db";
        String downloadPath = System.getProperty("user.dir");

        downloadPath = downloadPath + File.separator + objectName;

        AWSCredentials credentials = new BasicIBMOAuthCredentials(apiKey, serviceInstanceID);
        ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);

        AmazonS3 cos = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointURL, location))
                .withClientConfiguration(clientConfig).build();

        try {
            eventBus.publish(new GenericMessageEvent("Database downloading"));
            cos.getObject(new GetObjectRequest(bucketName, objectName), new File(downloadPath));
            eventBus.publish(new DatabaseDownloadedEvent(downloadPath));
        } catch (Exception e) {
            eventBus.publish(new ErrorEvent("Database failed to download"));
        }

    }
}
