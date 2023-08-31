package com.bk.modelcreator;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class CloudUploadHandler implements EventHandler<UploadRequestEvent>{
    private EventBus eventBus;
    private PropertiesManager propertiesManager;
    private ApplicationState applicationState;
    public CloudUploadHandler(EventBus eventBus, PropertiesManager propertiesManager, ApplicationState applicationState)
    {
        this.eventBus = eventBus;
        this.propertiesManager = propertiesManager;
        this.applicationState = applicationState;
        eventBus.subscribe(UploadRequestEvent.class, this);
    }
    @Override
    public void handle(UploadRequestEvent event) {
        try
        {
            // Authenticates using the provided JSON key
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(propertiesManager.getProperty("projectID"))
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(propertiesManager.getProperty("cloudUploaderJSON"))))
                    .build()
                    .getService();


            // Upload the file in chunks
            BlobId blobId = BlobId.of(propertiesManager.getProperty("cloudBucketName"), "images.zip");
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/zip").build();

            int bufferSize = 1024 * 1024; // 1 MB buffer.
            byte[] buffer = new byte[bufferSize];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

            File file = new File(event.getFilePath());
            long fileSize = file.length();
            long totalBytesUploaded = 0;

            try (FileInputStream fileInputStream = new FileInputStream(file);
                 ReadableByteChannel readableByteChannel = Channels.newChannel(fileInputStream);
                 WriteChannel writer = storage.writer(blobInfo)) {

                int bytesRead;
                while ((bytesRead = readableByteChannel.read(byteBuffer)) > 0) {
                    byteBuffer.flip();
                    writer.write(byteBuffer);
                    byteBuffer.clear();

                    totalBytesUploaded += bytesRead;
                    double uploadPercentage = (100.0 * totalBytesUploaded) / fileSize;

                    eventBus.publish(new GenericMessageEvent("Upload percentage: " + String.format("%.2f", uploadPercentage)));
                }
            }
            catch (Exception exception) {
                eventBus.publish(new ErrorEvent("Upload failed"));
                return;

            }
        }

        catch (Exception exception)
        {
            eventBus.publish(new ErrorEvent("Upload failed"));
            System.out.println(exception.getMessage());
            System.out.println(exception.getStackTrace());

            return;
        }

        eventBus.publish(new UploadFinishedEvent(propertiesManager.getProperty("cloudBucketName")));

    }

    }
