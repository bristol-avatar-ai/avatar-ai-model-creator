package com.bk.modelcreator;


import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.Credentials;
import com.google.api.gax.core.FixedCredentialsProvider;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class VMStarter {
    private String jsonPath;
    private String project;
    private String zone;
    private String instanceName;
    public VMStarter(Properties properties)
    {
        this.jsonPath = properties.getProperty("vmStarterJSON");
        this.project = properties.getProperty("projectID");
        this.zone = properties.getProperty("vmZone");
        this.instanceName = properties.getProperty("vmInstanceName");
    }

    public String startAndGetIP(ModifiedTextArea textArea)
    {
        try
        {
            // Load the credentials from the JSON key file
            FileInputStream serviceAccountStream = new FileInputStream(jsonPath);
            Credentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

            // Use the credentials when creating the InstancesClient
            InstancesSettings instancesSettings = InstancesSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            InstancesClient instancesClient = InstancesClient.create(instancesSettings);

            // Create the request.
            StartInstanceRequest startInstanceRequest = StartInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.startAsync(
                    startInstanceRequest);

            textArea.appendText("Attempting to start VM - please wait...");

            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Operation.Status.DONE) {
                textArea.appendText("Instance started successfully!");

                Instance instance = instancesClient.get(project, zone, instanceName);

                NetworkInterface networkInterface = instance.getNetworkInterfacesList().get(0);
                AccessConfig accessConfig = networkInterface.getAccessConfigsList().get(0);

                return accessConfig.getNatIP();
            }

        }

        catch (Exception exception)
        {
            textArea.appendText(exception.getMessage() + System.lineSeparator());
            textArea.appendText("Encountered above bug; please contact developers");
        }

        return null;
    }

}

