package ro.cosu.vampires.server.resources.ssh;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.util.Ssh;

import java.io.IOException;


public class SshResource extends AbstractResource {
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);

    private final SshResourceParameters parameters;
    private String commandOutput;


    public SshResource(SshResourceParameters parameters) {
        super(parameters);
        this.parameters = parameters;
    }

    @Override
    public void onStart() throws IOException, JSchException {
        LOG.debug("ssh starting");
        String command = "nohup " + parameters.command() + " > /dev/null 2>&1 &  echo $! ";

        this.commandOutput = exec(command);

    }

    @Override
    public void onStop() throws IOException, JSchException {
        LOG.debug("ssh stopping");
        String command = "kill " + Integer.parseInt(commandOutput);
        this.commandOutput = exec(command);
    }

    @Override
    public void onFail() throws IOException, JSchException {
        LOG.debug("ssh failed");
        String command = "kill -9" + Integer.parseInt(commandOutput);
        this.commandOutput = exec(command);
    }


    private String exec(String command) throws IOException, JSchException {
        return Ssh.runCommand(parameters.user(), parameters.privateKey(), parameters.address(), command, parameters.port());
    }





}
