package com.bk.modelcreator;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class SSHConnector {
    private String username;
    private String host;
    private String privateKey;
    private final int PORT = 22;
    private String bucketName;

    public SSHConnector(Properties properties, String externalIP)
    {
        this.username = properties.getProperty("vmUsername");
        this.host = externalIP;
        this.privateKey = properties.getProperty("vmPrivateKey");
        this.bucketName = properties.getProperty("cloudBucketName");
    }
    public void runCommands (ModifiedTextArea textArea)
    {
        try {
            JSch jsch = new JSch();

            // Add the private key
            jsch.addIdentity(privateKey);

            // Create a JSch session to connect to the server
            Session session = jsch.getSession(username, host, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            textArea.appendText("Establishing SSH connection to VM...");
            session.connect();
            textArea.appendText("SSH connection established.");

            String command = getCommand();

            // Execute command
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // Get the input stream and check if command has gone through
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream input = channel.getInputStream();
            channel.connect();

            textArea.appendText("Command: " + command);

            // Disconnect the channel and session
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | IOException e) {
            textArea.appendText(e.getMessage());
            textArea.appendText("Something has gone horribly wrong!");
        }
    }

    private String getCommand()
    {
        return "nohup ./startupScript.sh " + bucketName + " " +  username +  " " + "& > output.log";
    }
}

