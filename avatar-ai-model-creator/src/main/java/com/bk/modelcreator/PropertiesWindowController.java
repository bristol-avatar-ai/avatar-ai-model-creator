package com.bk.modelcreator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesWindowController {
    private final StringProperty selectedFilePath = new SimpleStringProperty(null);
    @FXML
    private TextField selectFileTextField;
    @FXML
    private Button loadFileButton;
    private static final String PROMPT = "Click and select a .properties file";
    @FXML
    public void initialize() {
        loadFileButton.setDisable(true);

        // Add a listener that updates the UI when the selectedFilePath changes
        selectedFilePath.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectFileTextField.setText(newValue);
                loadFileButton.setDisable(false);
            } else {
                selectFileTextField.setText(null);
                loadFileButton.setDisable(true);
                selectFileTextField.setPromptText(PROMPT);
            }
        });

        selectFileTextField.setOnMouseClicked(event -> {
            onSelectFileTextFieldClick();
        });


        loadFileButton.setOnMouseClicked(event -> {
            onLoadFileButtonClick();
        });

    }

    @FXML
    void onSelectFileTextFieldClick()
    {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Open .properties file");

        // Add Extension Filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PROPERTIES files (*.properties)", "*.properties");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedFilePath.set(file.getAbsolutePath());
        }

        else
        {
            selectedFilePath.set(null);
        }
    }

    @FXML
    void onLoadFileButtonClick()
    {
        loadProperties();
    }

    private void loadProperties()
    {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(selectedFilePath.get())) {
            // Load the .properties file
            properties.load(fileInputStream);

            if (!checkProperties(properties))
            {
                GlobalEventBus.getInstance().post(new PropertiesFileLoadedEvent(false, "Invalid properties", null));

            }

            else
            {
                GlobalEventBus.getInstance().post(new PropertiesFileLoadedEvent(true, "Loaded properties", properties));

            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            GlobalEventBus.getInstance().post(new PropertiesFileLoadedEvent(false, "IO error", null));
        }

    }

    private boolean checkProperties(Properties properties)
    {
        // List of expected keys
        String[] keysToCheck = {"cloudUploaderJSON",
                "vmStarterJSON",
                "vmPrivateKey",
                "vmUsername",
                "cloudBucketName",
                "projectID",
                "vmZone",
                "vmInstanceName",
                "dbDownloadJSON",
                "dbServiceInstanceID",
                "dbEndpointURL",
                "dbLocation",
                "dbBucketName"};

        for (String key : keysToCheck) {
            if (!properties.containsKey(key)) {
                return false;
            } else {
                String value = properties.getProperty(key);
                if (value == null || value.trim().isEmpty()) {
                    System.out.println("Empty value for key: " + key);
                    return false;
                }
            }
        }

        return true;
    }


}
