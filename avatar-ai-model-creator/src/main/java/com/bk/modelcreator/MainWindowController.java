package com.bk.modelcreator;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MainWindowController implements EventHandler<Event>, LogObserver{
    private String dbPath;
    private PropertiesManager propertiesManager;
    private String folderText = "Click to select a folder";
    @FXML
    private Button buttonProcess;
    @FXML
    private TextField textFieldDirectory;
    @FXML
    private TextArea textFieldUpdates;
    @FXML
    private MenuItem loadPropertiesMenuItem;
    private EventBus eventBus;
    private ApplicationState applicationState;
    @FXML
    public void initialize() {

        loadPropertiesMenuItem.setOnAction(actionEvent -> onLoadPropertiesClick());

        textFieldDirectory.setOnMouseClicked(event -> {
            onTextFieldDirectoryClick();
        });

        buttonProcess.setOnMouseClicked(event -> {
            onButtonProcessClicked();
        });
    }

    private void updateUIBasedOnState(ApplicationState.State newValue)
    {
        switch (newValue)
        {
            case INIT, PROCESSING -> {
                textFieldDirectory.setDisable(true);
                buttonProcess.setDisable(true);
            }

            case PROPERTIES_SELECTED -> {
                textFieldDirectory.setDisable(false);
            }

        }
    }

    /**
     * Starts load properties window
     */
    protected void onLoadPropertiesClick()
    {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PropertiesWindow.fxml"));
            Parent secondView = fxmlLoader.load();

            PropertiesWindowController controller = fxmlLoader.getController();

            controller.setEventBus(eventBus);
            controller.setPropertiesManager(propertiesManager);
            controller.setApplicationState(applicationState);

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
        Optional<String> folderPath = Optional.ofNullable(selectedDirectory).map(File::getPath);
        textFieldDirectory.setText(folderPath.orElse(""));
        eventBus.publish(new FolderSelectedEvent(folderPath.orElse(null), dbPath));}

    @FXML
     void onButtonProcessClicked() {
        // Create a new thread to handle tasks
        new Thread(() -> {
            applicationState.setCurrentState(ApplicationState.State.PROCESSING);
            eventBus.publish(new ProcessRequestEvent(textFieldDirectory.getText(),  textFieldDirectory.getText() + ".zip"));
        }).start(); // Start the thread.
    }


    public void setEventBus(EventBus eventBus)
    {
        this.eventBus = eventBus;
        this.eventBus.subscribe(DatabaseDownloadedEvent.class, this);
        this.eventBus.subscribe(ValidFolderSelectedEvent.class, this);
        this.eventBus.subscribe(InvalidFolderSelectedEvent.class, this);
    }

    public void setPropertiesManager(PropertiesManager propertiesManager)
    {
        this.propertiesManager = propertiesManager;
    }
    @Override
    public void handle(Event event) {

        if (event instanceof DatabaseDownloadedEvent)
        {
            dbPath = ((DatabaseDownloadedEvent) event).getDbPath();
            applicationState.setCurrentState(ApplicationState.State.PROPERTIES_SELECTED);
        }

        else if (event instanceof InvalidFolderSelectedEvent)
        {
            buttonProcess.setDisable(true);
        }

        else if (event instanceof ValidFolderSelectedEvent)
        {
            buttonProcess.setDisable(false);
        }

        else if (event instanceof CommandSentEvent)
        {
            applicationState.setCurrentState(ApplicationState.State.PROPERTIES_SELECTED);
        }

    }

    @Override
    public void update(String message) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> textFieldUpdates.appendText(message + System.lineSeparator()));
        Platform.runLater(pause::play);
    }
    public void setApplicationState(ApplicationState applicationState)
    {
        this.applicationState = applicationState;

        // Setup the initial state
        updateUIBasedOnState(applicationState.getCurrentState());

        // Listen to future changes
        applicationState.addStateChangeListener((observable, oldValue, newValue) -> updateUIBasedOnState(newValue));

    }
}
