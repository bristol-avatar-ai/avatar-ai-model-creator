package com.bk.modelcreator;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.v1.*;

import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

public class VMStartHandler implements EventHandler<UploadFinishedEvent>{

    private String jsonPath;
    private String project;
    private String zone;
    private String instanceName;
    private EventBus eventBus;
    private PropertiesManager propertiesManager;
    private ApplicationState applicationState;

    public VMStartHandler(EventBus eventBus, PropertiesManager propertiesManager, ApplicationState applicationState)
    {
        this.eventBus = eventBus;
        this.propertiesManager = propertiesManager;
        this.applicationState = applicationState;

        eventBus.subscribe(UploadFinishedEvent.class, this);
    }

    private String startAndGetIP()
    {
        try
        {
            this.jsonPath = propertiesManager.getProperty("vmStarterJSON");
            this.project = propertiesManager.getProperty("projectID");
            this.zone = propertiesManager.getProperty("vmZone");
            this.instanceName = propertiesManager.getProperty("vmInstanceName");

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

            eventBus.publish(new GenericMessageEvent("Attempting to start VM - please wait..."));

            // Wait for the operation to complete.
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Operation.Status.DONE) {

                Instance instance = instancesClient.get(project, zone, instanceName);

                NetworkInterface networkInterface = instance.getNetworkInterfacesList().get(0);
                AccessConfig accessConfig = networkInterface.getAccessConfigsList().get(0);

                return accessConfig.getNatIP();
            }

        }

        catch (Exception exception)
        {
            eventBus.publish(new ErrorEvent("Could not start VM"));
        }

        return null;
    }
    @Override
    public void handle(UploadFinishedEvent event)
    {
        String ipAddress = startAndGetIP();

        if (ipAddress != null)
        {
            eventBus.publish(new VMStartedEvent(ipAddress));
        }

        else
        {
            eventBus.publish(new ErrorEvent("VM failed to start"));
        }
    }
}
