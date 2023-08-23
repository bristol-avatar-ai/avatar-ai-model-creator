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
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DBDownloader {
    private ModifiedTextArea textArea;
    private String serviceInstanceID;
    private String apiKeyPath;
    private String apiKey;
    private String endpointURL;
    private String location;
    private String bucketName;
    private String objectName;
    public DBDownloader(ModifiedTextArea textArea, Properties properties)
    {
        this.textArea = textArea;
        apiKeyPath = properties.getProperty("dbDownloadJSON");
        serviceInstanceID = properties.getProperty("dbServiceInstanceID");
        endpointURL = properties.getProperty("dbEndpointURL");
        location = properties.getProperty("dbLocation");
        bucketName = properties.getProperty("dbBucketName");
        objectName = "data.db";

    }
    public boolean downloadDB(String downloadPath) {
        if (!getAPIKey())
        {
            return false;
        }

        downloadPath = downloadPath + File.separator + objectName;

        AWSCredentials credentials = new BasicIBMOAuthCredentials(apiKey, serviceInstanceID);
        ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);

        AmazonS3 cos = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointURL, location))
                .withClientConfiguration(clientConfig).build();

        try {
            textArea.appendText("Downloading DB");
            cos.getObject(new GetObjectRequest(bucketName, objectName), new File(downloadPath));
            textArea.appendText("Downloaded DB " + objectName + " to " + downloadPath);
        } catch (Exception e) {
            textArea.appendText("Unable to download " + objectName);
            return false;
        }

        return true;
    }

    private boolean getAPIKey()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<?, ?> map = null;
        try {
            map = objectMapper.readValue(new File(apiKeyPath), Map.class);
        } catch (IOException e) {
            textArea.appendText("Could not open API key file");
            return false;
        }
        apiKey = (String) map.get("apikey");
        return true;
    }

}
