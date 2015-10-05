package ro.cosu.vampires.server.resources.ssh;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.util.Ssh;

import java.io.IOException;


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

        this.commandOutput = Ssh.runCommand(user, privateKey, address, command);


    }

    @Override
    public void onStop() throws IOException, JSchException {
        LOG.debug("ssh stopping");
        String command = "kill " + Integer.parseInt(commandOutput);
        this.commandOutput = Ssh.runCommand(user, privateKey, address, command);
    }

    @Override
    public void onFail() throws IOException, JSchException {
        LOG.debug("ssh failed");
        String command = "kill " + Integer.parseInt(commandOutput);
        this.commandOutput = Ssh.runCommand(user, privateKey, address, command);
    }





}
