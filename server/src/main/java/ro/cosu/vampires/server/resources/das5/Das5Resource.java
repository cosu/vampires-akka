package ro.cosu.vampires.server.resources.das5;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.ssh.SshResource;
import ro.cosu.vampires.server.util.Ssh;

import java.io.IOException;

public class Das5Resource extends AbstractResource {
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);

    private final Das5ResourceParameters parameters;
    private String commandOutput;



    public Das5Resource(Das5ResourceParameters parameters) {
        super(parameters);
        this.parameters = parameters;
    }


    @Override
    public void onStart() throws Exception {
        LOG.debug("das5 starting");

        String command = "sbatch -N 1 " + parameters.command();
        this.commandOutput = exec(command);

    }

    @Override
    public void onStop() throws Exception {
        LOG.debug("das5 stopping");
        String command = "scancel " + commandOutput.split(" ")[3];
        this.commandOutput = exec(command);

    }

    @Override
    public void onFail() throws Exception {
        LOG.debug("das5 fail");
        String command = "scancel " + commandOutput.split(" ")[3];
        this.commandOutput = exec(command);

    }

    private String exec(String command) throws IOException, JSchException {
        return Ssh.runCommand(parameters.user(), parameters.privateKey(), parameters.address(), command, parameters.port());
    }

}
