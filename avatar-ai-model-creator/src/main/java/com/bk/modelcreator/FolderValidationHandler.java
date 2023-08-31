package com.bk.modelcreator;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FolderValidationHandler implements EventHandler<FolderSelectedEvent>{
    private static final Set<String> VALID_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png"
    );

    private EventBus eventBus;

    private PropertiesManager propertiesManager;

    private ApplicationState applicationState;

    /**
     * At least 2 labels are required for the image classification model to be troined
     */
    private static final int MIN_DIRECTORIES = 2;

    public FolderValidationHandler(EventBus eventBus, PropertiesManager propertiesManager, ApplicationState applicationState)
    {
        this.eventBus = eventBus;
        this.propertiesManager = propertiesManager;
        this.applicationState = applicationState;
        eventBus.subscribe(FolderSelectedEvent.class, this);
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
    @Override
    public void handle(FolderSelectedEvent event) {

        if (event.getDirectoryPath() == null)
        {
            return;
        }

        if (hasValidFolderNames(event.getDirectoryPath(), event.getDatabasePath()) && hasValidStructure(event.getDirectoryPath()))
        {
            eventBus.publish(new ValidFolderSelectedEvent());
        }

        else{
            eventBus.publish(new InvalidFolderSelectedEvent());
        }
    }
}
