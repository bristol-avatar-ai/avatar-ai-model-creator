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
import java.util.Properties;

public class PropertiesWindowController{
    private EventBus eventBus;
    private PropertiesManager propertiesManager;
    private ApplicationState applicationState;
    private final StringProperty selectedFilePath = new SimpleStringProperty(null);
    @FXML
    private TextField selectFileTextField;
    @FXML
    private Button loadFileButton;
    private static final String PROMPT = "Click and select a .properties file";
    @FXML
    public void initialize() {
        // A listener that updates the UI when the selectedFilePath changes
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

            if (checkProperties(properties))
            {
                propertiesManager.setProperties(properties);
                propertiesManager.setConfigPath(selectedFilePath.get());
                eventBus.publish(new PropertiesLoadedEvent());
            }

            else
            {
                eventBus.publish(new ErrorEvent("Properties could not be loaded; is the file correct?"));
            }

        } catch (IOException e) {
            eventBus.publish(new ErrorEvent("Properties could not be loaded; IO exception"));
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
                "dbAPIKey",
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
                    return false;
                }
            }
        }

        return true;
    }
    public void setEventBus(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }

    public void setPropertiesManager(PropertiesManager propertiesManager)
    {
        this.propertiesManager = propertiesManager;
    }

    public void setApplicationState(ApplicationState applicationState)
    {
        this.applicationState = applicationState;

        // Setup the initial state
        updateUIBasedOnState(applicationState.getCurrentState());

        // Listen to future changes
        applicationState.addStateChangeListener((observable, oldValue, newValue) -> updateUIBasedOnState(newValue));

    }

    private void updateUIBasedOnState(ApplicationState.State newValue)
    {
        switch (newValue)
        {
            case INIT, PROPERTIES_SELECTED -> {
                selectFileTextField.setDisable(false);
                loadFileButton.setDisable(true);
            }

            case PROCESSING -> {
                selectFileTextField.setDisable(true);
                loadFileButton.setDisable(true);
            }

        }
    }

}
