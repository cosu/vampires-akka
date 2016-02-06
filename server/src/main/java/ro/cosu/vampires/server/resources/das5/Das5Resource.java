package ro.cosu.vampires.server.resources.das5;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.util.Ssh;

import java.io.IOException;

public class Das5Resource extends AbstractResource {
    private  static final Logger LOG = LoggerFactory.getLogger(Das5Resource.class);

    private final Das5ResourceParameters parameters;
    private String commandOutput;

    private final Ssh ssh;


    public Das5Resource(Das5ResourceParameters parameters, Ssh ssh) {
        super(parameters);
        this.parameters = parameters;
        this.ssh = ssh;
    }


    @Override
    public void onStart() throws Exception {
        String command = "sbatch -N 1 " + parameters.command() + " " + description().id();
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
        return ssh.runCommand(parameters.user(), parameters.privateKey(), parameters.address(), command, parameters.port());
    }

}
