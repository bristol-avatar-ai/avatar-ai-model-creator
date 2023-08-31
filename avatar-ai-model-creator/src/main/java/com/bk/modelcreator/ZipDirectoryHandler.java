package com.bk.modelcreator;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectoryHandler implements EventHandler<ProcessRequestEvent>{
    private EventBus eventBus;
    private ApplicationState applicationState;
    public ZipDirectoryHandler(EventBus eventBus, ApplicationState applicationState)
    {
        this.eventBus  = eventBus;
        this.applicationState = applicationState;
        eventBus.subscribe(ProcessRequestEvent.class, this);
    }
    public void zipDirectory(String srcDirectory, String outputZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        File srcFile = new File(srcDirectory);
        addFolderToZip(srcFile, srcFile.getName(), zos);

        zos.close();
    }
    private void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            // Replace spaces with underscores in the file name for image classification model
            String sanitisedName = file.getName().replace(" ", "_");

            if (file.isDirectory()) {
                addFolderToZip(file, parentFolder + File.separator + sanitisedName, zos);
                continue;
            }

            zos.putNextEntry(new ZipEntry(parentFolder + File.separator + sanitisedName));

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                long bytesRead;
                byte[] bytesIn = new byte[4096];
                while ((bytesRead = bis.read(bytesIn)) != -1) {
                    zos.write(bytesIn, 0, (int) bytesRead);
                }
                zos.closeEntry();
            }
        }
    }

    @Override
    public void handle(ProcessRequestEvent event) {
        try{
            zipDirectory(event.getSrcDirectory(), event.getOutputZipFile());
            eventBus.publish(new UploadRequestEvent(event.getOutputZipFile()));
        } catch (IOException e) {
            eventBus.publish(new ErrorEvent("Could not zip folder!"));
        }
    }
}
