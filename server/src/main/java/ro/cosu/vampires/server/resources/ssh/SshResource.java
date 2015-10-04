package ro.cosu.vampires.server.resources.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;


public class SshResource extends AbstractResource {
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);
    private final String user;
    private final String privateKey;
    private final String address;
    private final String command;
    private String commandOutput;

    public SshResource(String user, String privateKey, String address, String command) {
        super(Resource.Type.SSH);
        this.user = user;
        this.privateKey = privateKey;
        this.address = address;
        this.command = command;
    }

    @Override
    public void onStart() throws IOException, JSchException {
        LOG.debug("ssh starting");
        String command = "nohup " + this.command + " > /dev/null 2>&1 &  echo $! ";

        runSSHComand(user, privateKey, address, command);


    }

    @Override
    public void onStop() throws IOException, JSchException {
        LOG.debug("ssh stopping");
        String command = "kill " + Integer.parseInt(commandOutput);
        runSSHComand(user, privateKey, address, command);
    }

    @Override
    public void onFail() throws IOException, JSchException {
        LOG.debug("ssh failed");
        String command = "kill " + Integer.parseInt(commandOutput);
        runSSHComand(user, privateKey, address, command);
    }


    private void runSSHComand(String user, String privateKey, String address, String command) throws JSchException,
            IOException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(user, address, 22);
        jsch.addIdentity(privateKey);

        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        LOG.debug("SSH Node:" + address + ", command:" + command);
        channel.setCommand(command);
        channel.connect();
        String msg;

        StringBuilder sb = new StringBuilder();
        while ((msg = in.readLine()) != null) {
            sb.append(msg);
        }

        this.commandOutput = sb.toString();
        channel.disconnect();
        session.disconnect();

    }


}
