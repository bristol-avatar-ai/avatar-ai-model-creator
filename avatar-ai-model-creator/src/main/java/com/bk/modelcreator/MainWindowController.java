package com.bk.modelcreator;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class MainWindowController {
    private String workingDirectory;
    private FolderValidator folderValidator;
    private Properties properties;
    private String folderName = null;
    private String parentPath = null;
    private String folderText = "Click to select a folder";
    @FXML
    private Button buttonProcess;
    @FXML
    private TextField textFieldDirectory;
    @FXML
    private TextArea textFieldUpdates;
    @FXML
    private MenuItem loadPropertiesMenuItem;
    private ModifiedTextArea textArea;
    private DBDownloader dbDownloader;
    public MainWindowController()
    {
        GlobalEventBus.getInstance().register(this);
    }

    @FXML
    public void initialize() {
        textArea = new ModifiedTextArea(textFieldUpdates);

        loadPropertiesMenuItem.setOnAction(actionEvent -> onLoadPropertiesClick());

        folderValidator = new FolderValidator(textArea);

        textFieldDirectory.setPromptText(folderText);
        textFieldDirectory.setOnMouseClicked(event -> {
            onTextFieldDirectoryClick();
        });

        textFieldDirectory.setDisable(true);

        buttonProcess.setOnMouseClicked(event -> {
            onButtonProcessClicked();
        });

    }

    /**
     * Starts load properties window
     */
    protected void onLoadPropertiesClick()
    {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PropertiesWindow.fxml"));
            Parent secondView = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Load properties");
            stage.setScene(new Scene(secondView));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     void onTextFieldDirectoryClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);

        new Thread(() -> {
            buttonProcess.setDisable(true);

            if (selectedDirectory != null) {
                String absolutePath = selectedDirectory.getAbsolutePath();
                String databasePath = workingDirectory + File.separator + "data.db";
                boolean hasValidStructure = folderValidator.hasValidStructure(absolutePath);
                boolean hasValidFolderNames = folderValidator.hasValidFolderNames(absolutePath,
                        databasePath);

                Platform.runLater(() -> {
                    textFieldDirectory.setText(absolutePath);
                    folderName = selectedDirectory.getName();
                    parentPath = Paths.get(absolutePath).getParent().toString();

                    if (hasValidStructure && hasValidFolderNames) {
                        buttonProcess.setDisable(false);
                    }

                    if (!hasValidFolderNames)
                    {
                        textArea.appendText("Allowed subfolder names are: ");
                        List<String> exhibitNames = folderValidator.getExhibitionNamesFromDatabase(databasePath);
                        for (String exhibitName: exhibitNames)
                        {
                            textArea.appendText("-" + exhibitName);
                        }
                    }

                });
            } else {
                Platform.runLater(() -> {
                    folderName = null;
                    parentPath = null;
                    textFieldDirectory.setPromptText(folderText);
                });
            }
        }).start();
    }

    @FXML
     void onButtonProcessClicked() {
        // Create a new thread to handle tasks
        new Thread(() -> {
            try {
                //Zip and upload images to cloud storage
                ZipDirectory.zipDirectory(textFieldDirectory.getText(), parentPath + File.separator + folderName + ".zip");
                textArea.appendText("Zipped!");
                CloudUploader uploader = new CloudUploader(properties);
                uploader.upload(textArea, "images.zip", parentPath + File.separator + folderName + ".zip");

                //Start up VM
                VMStarter vm = new VMStarter(properties);
                String externalIP = vm.startAndGetIP(textArea);

                if (externalIP == null) {
                    textArea.appendText("Could not get IP for VM; cannot proceed");
                    return;
                }

                //Attempt to SSH in
                SSHConnector connector = new SSHConnector(properties, externalIP);
                connector.runCommands(textArea);

                textArea.appendText("Image model is now being trained. Please feel free to close the application.");

            } catch (Exception e) {
                textArea.appendText("Error encountered");
            }
        }).start(); // Start the thread.
    }

    private boolean loadProperties()
    {
        InputStream input = null;

        try {
            input = ImageClassifierMain.class.getClassLoader().getResourceAsStream("config.properties");
            // load a properties file
            properties.load(input);
            return true;

        }
        catch (Exception exception) {
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {

                }
            }
        }

    }
    @Subscribe
    public void handlePropertiesLoaded(PropertiesFileLoadedEvent event) {
        if (event.isLoaded()) {
            textFieldDirectory.setDisable(false);
            buttonProcess.setDisable(true);
            this.properties =  event.getProperties();
            downloadDB();
        } else {
            textFieldDirectory.setDisable(true);
            buttonProcess.setDisable(true);
        }

        textArea.appendText(event.getMessage());
    }

    private void downloadDB()
    {
        new Thread(() -> {
            workingDirectory = System.getProperty("user.dir");
            dbDownloader = new DBDownloader(textArea, properties);

            //Disable updating if database cannot be downloaded
            if (!dbDownloader.downloadDB(workingDirectory))
            {
                textFieldDirectory.setDisable(true);
            }

        }).start();
    }


}