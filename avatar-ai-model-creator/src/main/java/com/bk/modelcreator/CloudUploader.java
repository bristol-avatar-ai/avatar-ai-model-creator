package com.bk.modelcreator;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * A class to handle uploading files to a cloud storage bucket.
 */
public class CloudUploader {

    /**
     * The project ID for the cloud storage.
     */
    private static String PROJECT_ID;

    /**
     * The name of the cloud storage bucket.
     */
    private static String BUCKET_NAME;

    /**
     * The path to the JSON key file for authentication.
     */
    private static String PATH_TO_JSON_KEY;

    /**
     * Constructs a new CloudUploader with properties for configuration.
     *
     * @param properties The properties object containing configuration details.
     */
    public CloudUploader(Properties properties)
    {
        PROJECT_ID = properties.getProperty("projectID");
        BUCKET_NAME = properties.getProperty("cloudBucketName");
        PATH_TO_JSON_KEY = properties.getProperty("cloudUploaderJSON");
    }

    /**
     * Uploads a file to the specified cloud storage bucket.
     *
     * @param textArea The text area where upload progress and messages will be displayed.
     * @param fileName The name that the uploaded file will be saved as in the bucket.
     * @param filePath The local file system path to the file to be uploaded.
     */
    public void upload(ModifiedTextArea textArea, String fileName, String filePath)
    {
        try
        {
            // Authenticates using the provided JSON key
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(PROJECT_ID)
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(PATH_TO_JSON_KEY)))
                    .build()
                    .getService();

            // Upload the file in chunks
            BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/zip").build();

            int bufferSize = 1024 * 1024; // 1 MB buffer.
            byte[] buffer = new byte[bufferSize];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

            File file = new File(filePath);
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

                    textArea.appendText(String.format("Upload progress: %.2f percent", uploadPercentage));
                }
            }
            catch (Exception exception) {
                textArea.appendText(exception.getMessage());
            }
        }

        catch (Exception exception)
        {
            textArea.appendText(exception.getMessage());
        }

        textArea.appendText("File " + filePath + " uploaded to bucket " + BUCKET_NAME + " as " + fileName);

        }
    }

