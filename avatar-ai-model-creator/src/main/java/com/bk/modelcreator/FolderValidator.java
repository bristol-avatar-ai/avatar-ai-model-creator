package com.bk.modelcreator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FolderValidator {

    private static final Set<String> VALID_EXTENSIONS = Set.of(
      "jpg", "jpeg", "png"
    );

    /**
     * At least 2 labels are required for the image classification model to be troined
     */
    private static final int MIN_DIRECTORIES = 2;
    private ModifiedTextArea textArea;

    public FolderValidator(ModifiedTextArea textArea)
    {
        this.textArea = textArea;
    }

    /**
     *
     * @param rootDirectoryPath The directory which is to be uploaded
     * @return boolean
     */
    public boolean hasValidStructure(String rootDirectoryPath)
    {
        Path rootPath = Paths.get(rootDirectoryPath);

        AtomicBoolean isValid = new AtomicBoolean(true);
        AtomicBoolean hasSubdirectory = new AtomicBoolean(false);
        AtomicInteger subDirectoryCount = new AtomicInteger(0);

        // Ensure that the root path is a directory
        if (!Files.isDirectory(rootPath)) {
            textArea.appendText("Root path is not a directory");
            return false;
        }

        // Walk through the directory tree starting at the root
        try {
            Files.walk(rootPath)
                    .forEach(path -> {
                        if (Files.isDirectory(path))
                        {
                            hasSubdirectory.set(true);
                            subDirectoryCount.getAndIncrement();
                        }
                        // If the path is a directory and is more than one level deep
                        if (Files.isDirectory(path) && path.getNameCount() > rootPath.getNameCount() + 1) {
                            System.out.println("Is this getting called?");
                            isValid.set(false);
                        }
                        // If the path is a file and its extension is not .png or .jpeg
                        else if (Files.isRegularFile(path)) {
                            String fileName = path.getFileName().toString();
                            String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                            if (!VALID_EXTENSIONS.contains(fileExtension))
                            {
                                isValid.set(false);
                            }
                        }
                    });
        } catch (IOException e) {
            isValid.set(false);
        }

        if (isValid.get() == false)
        {
            textArea.appendText("Please ensure that your subdirectories are not more than " +
                    "one layer deep, have only .png or .jpeg images and are named " +
                    "after the exhibits ");
        }

        System.out.println("Count is:" + subDirectoryCount.get());

        return isValid.get() && hasSubdirectory.get() && (subDirectoryCount.get() >= MIN_DIRECTORIES);
    }

    /**
     * Function which ensures that subfolders uploaded have valid names
     *
     * @param directoryPath Directory to be uploaded
     * @param databasePath Path to the .db file
     * @return boolean
     */

    public boolean hasValidFolderNames(String directoryPath, String databasePath) {

        List<String> subfolderNames = getSubfolderNames(directoryPath);
        List<String> exhibitionNames = getExhibitionNamesFromDatabase(databasePath);

        for (String exhibitionName: exhibitionNames)
        {
            System.out.println("Exhibition name: " + exhibitionName);
        }

        for (String subfolder: subfolderNames)
        {
            System.out.println("Subfolder name: " + subfolder);
        }

        // Compare the two lists
        for (String folder : subfolderNames) {
            if (!exhibitionNames.contains(folder)) {
                System.out.println(folder);
                System.out.println("Returning false?");
                return false;
            }
        }

        return true;
    }

    private List<String> getSubfolderNames(String directoryPath) {
        List<String> subfolderNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    subfolderNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return subfolderNames;
    }

    public List<String> getExhibitionNamesFromDatabase(String databasePath) {
        List<String> exhibitionNames = new ArrayList<>();
        try {
            String url = "jdbc:sqlite:" + databasePath;
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT name FROM feature");
            while (rs.next()) {
                exhibitionNames.add(rs.getString("name").strip());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exhibitionNames;
    }
}

