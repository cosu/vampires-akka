package ro.cosu.vampires.server.resources.das5;

import com.jcraft.jsch.JSchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.util.SshClient;

public class Das5Resource extends AbstractResource {
    private static final Logger LOG = LoggerFactory.getLogger(Das5Resource.class);

    private final Das5ResourceParameters parameters;
    private final SshClient sshClient;
    private String commandOutput;


    public Das5Resource(Das5ResourceParameters parameters, SshClient sshClient) {
        super(parameters);
        this.parameters = parameters;
        this.sshClient = sshClient;
    }


    @Override
    public void onStart() throws Exception {
        String command = "sbatch -N 1 " + parameters.command() + " " + description().id();
        getLogger().debug("command: {}", command);
        this.commandOutput = exec(command);
    }

    @Override
    public void onStop() throws Exception {
        LOG.debug("das " + commandOutput);
        String command = "scancel " + commandOutput.split(" ")[3];
        this.commandOutput = exec(command);

    }

    @Override
    public void onFail() throws Exception {
        String command = "scancel " + commandOutput.split(" ")[3];
        this.commandOutput = exec(command);

    }

    private String exec(String command) throws IOException, JSchException {
        return sshClient.runCommand(parameters.user(), parameters.privateKey(), parameters.address(), command, parameters.port());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
