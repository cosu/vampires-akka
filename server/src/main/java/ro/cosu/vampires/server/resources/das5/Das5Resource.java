package ro.cosu.vampires.server.resources.das5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.ssh.SshResource;
import ro.cosu.vampires.server.util.Ssh;

public class Das5Resource extends AbstractResource {
    static final Logger LOG = LoggerFactory.getLogger(SshResource.class);

    private final String user;
    private final String privateKey;
    private final String address;
    private final String command;
    private final int port;
    private String commandOutput;

    public Das5Resource(String user, String privateKey, String address, String command, int port) {
        super(Type.DAS5);
        this.user = user;
        this.privateKey = privateKey;
        this.address = address;
        this.command = command;
        this.port = port;
    }


    @Override
    public void onStart() throws Exception {
        LOG.debug("das5 starting");

        String command = "sbatch -N 1 " + this.command;

        this.commandOutput = Ssh.runCommand(user, privateKey, address, command, port);


    }

    @Override
    public void onStop() throws Exception {
        LOG.debug("das5 stopping");
        String command = "scancel " + commandOutput.split(" ")[3];
        this.commandOutput = Ssh.runCommand(user, privateKey, address, command, port);

    }

    @Override
    public void onFail() throws Exception {
        LOG.debug("das5 fail");
        String command = "scancel " + commandOutput.split(" ")[3];
        this.commandOutput = Ssh.runCommand(user, privateKey, address, command, port);

    }

}
