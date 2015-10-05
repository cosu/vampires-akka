package ro.cosu.vampires.server.resources.das5;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.ssh.SshResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class Das5Resource extends AbstractResource {
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);

    private final String user;
    private final String privateKey;
    private final String address;
    private final String command;
    private String commandOutput;

    public Das5Resource(String user, String privateKey, String address, String command) {
        super(Type.DAS5);
        this.user = user;
        this.privateKey = privateKey;
        this.address = address;
        this.command = command;
    }


    @Override
    public void onStart() throws Exception {
        LOG.debug("das5 starting");

        String command = "sbatch -N 1 vampires.sh";

        runSSHComand(user, privateKey, address, command);


    }

    @Override
    public void onStop() throws Exception {
        LOG.debug("das5 stopping");
        String command = "scancel " + commandOutput.split(" ")[3];
        runSSHComand(user, privateKey, address, command);

    }

    @Override
    public void onFail() throws Exception {

    }

    private void runSSHComand(String user, String privateKey, String address, String command) throws JSchException,
            IOException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(user, address, 2222);
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
        //validate ssh result
        LOG.info("command output :" + commandOutput);
        channel.disconnect();
        session.disconnect();

    }
}
