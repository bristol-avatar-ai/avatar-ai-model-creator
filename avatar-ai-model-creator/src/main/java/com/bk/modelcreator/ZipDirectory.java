package com.bk.modelcreator;

import java.io.*;
import java.util.zip.*;

public class ZipDirectory {
    public static void zipDirectory(String srcDirectory, String outputZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        File srcFile = new File(srcDirectory);
        addFolderToZip(srcFile, srcFile.getName(), zos);

        zos.close();
    }
    private static void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
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


}
