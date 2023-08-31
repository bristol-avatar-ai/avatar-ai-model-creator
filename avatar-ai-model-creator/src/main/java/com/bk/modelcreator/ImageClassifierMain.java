package com.bk.modelcreator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class ImageClassifierMain extends Application {
    private EventBus eventBus;
    private PropertiesManager propertiesManager;
    private ApplicationState applicationState;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ImageClassifierMain.class.getResource("MainWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        MainWindowController controller = fxmlLoader.getController();
        initialiseClasses();

        controller.setEventBus(eventBus);
        controller.setPropertiesManager(propertiesManager);
        controller.setApplicationState(applicationState);

        Logger.getInstance().addObserver(controller);

        stage.setTitle("Model Creator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void initialiseClasses()
    {
        eventBus = new EventBus();
        propertiesManager = new PropertiesManager();
        applicationState = new ApplicationState();

        CloudUploadHandler cloudUploadHandler  = new CloudUploadHandler(eventBus, propertiesManager, applicationState);
        DBDownloadHandler dbDownloadHandler = new DBDownloadHandler(eventBus, propertiesManager, applicationState);
        FolderValidationHandler folderValidationHandler = new FolderValidationHandler(eventBus, propertiesManager, applicationState);
        SSHConnectHandler sshConnectHandler = new SSHConnectHandler(eventBus, propertiesManager, applicationState);
        VMStartHandler vmStartHandler = new VMStartHandler(eventBus, propertiesManager, applicationState);
        ZipDirectoryHandler zipDirectoryHandler = new ZipDirectoryHandler(eventBus, applicationState);

    }
}