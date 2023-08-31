package com.bk.modelcreator;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;

public class SSHConnectHandler implements EventHandler<VMStartedEvent>{
    private String username;
    private String host;
    private String privateKey;
    private final int PORT = 22;
    private String bucketName;
    private EventBus eventBus;
    private PropertiesManager propertiesManager;
    private ApplicationState applicationState;

    private Long WAIT_TIME = 5000L;

    public SSHConnectHandler(EventBus eventBus, PropertiesManager propertiesManager, ApplicationState applicationState)
    {
        this.eventBus = eventBus;
        this.propertiesManager = propertiesManager;
        this.applicationState = applicationState;

        eventBus.subscribe(VMStartedEvent.class, this);
    }
    private void runCommands(String externalIP)
    {
        //Wait 5 seconds before trying to run command
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {

            this.username = propertiesManager.getProperty("vmUsername");
            this.host = externalIP;
            this.privateKey = propertiesManager.getProperty("vmPrivateKey");
            this.bucketName = propertiesManager.getProperty("cloudBucketName");

            JSch jsch = new JSch();

            // Add the private key
            jsch.addIdentity(privateKey);

            // Create a JSch session to connect to the server
            Session session = jsch.getSession(username, host, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            eventBus.publish(new GenericMessageEvent("Establishing SSH connection to VM..."));
            session.connect();
            eventBus.publish(new GenericMessageEvent("Connection established"));

            //Upload .config file

            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.put(propertiesManager.getConfigPath(), "/home/" + username + "/");
            sftpChannel.disconnect();

            String command = getCommand();

            // Execute command
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // Get the input stream and check if command has gone through
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream input = channel.getInputStream();
            channel.connect();

            eventBus.publish(new GenericMessageEvent("Command: " + command));
            eventBus.publish(new CommandSentEvent());
            // Disconnect the channel and session
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | IOException e) {
            eventBus.publish(new ErrorEvent("Something has gone wrong; could not connect"));
        } catch (SftpException e) {
            eventBus.publish(new ErrorEvent("Could not transfer .config file"));
        }
    }

    private String getCommand()
    {
        return "nohup ./startupScript.sh " + username + " &> output.log &";
    }
    @Override
    public void handle(VMStartedEvent event) {
        runCommands(event.getAddress());
    }

}
