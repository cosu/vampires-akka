package ro.cosu.vampires.server.resources.local;

import autovalue.shaded.com.google.common.common.base.Joiner;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.resources.Resource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class LocalResource extends AbstractResource{
    static final Logger LOG = LoggerFactory.getLogger(LocalResource.class);
    private final String command;
    private CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();


    public LocalResource(String command) {
        super(Resource.Type.LOCAL);
        this.command = command;
    }

    @Override
    public void onStart() throws IOException {

        CommandLine cmd = new CommandLine("/bin/sh");
        cmd.addArgument("-c");
        cmd.addArgument("nohup " + command + " > /dev/null 2>&1 &  echo $! ", false);

        LOG.debug("execute {}", cmd.toString());
        execute(cmd);

        LOG.debug("local starting");
    }

    private void execute(CommandLine cmd) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());
        executor.setStreamHandler(new PumpStreamHandler(collectingLogOutputStream));
        executor.setWatchdog(new ExecuteWatchdog(1500));

        int exitCode = 0;
        try {
            exitCode =executor.execute(cmd);
        } catch (IOException e) {
            LOG.debug("{} has failed with error {}: {}", this, exitCode, e);
            LOG.debug("{}", Joiner.on("\n").join(collectingLogOutputStream.getLines()));
            throw e;
        }
    }

    @Override
    public void onStop() throws IOException {
        LOG.debug("local stopping");
        Integer pid = collectingLogOutputStream.getLines().stream().map(Integer::parseInt).findFirst().get();
        CommandLine cmd = new CommandLine("/bin/sh");
        cmd.addArgument("-c");
        cmd.addArgument("kill " + pid, false);
        execute(cmd);

    }

    @Override
    public void onFail() throws IOException {
        LOG.debug("local fail");
    }

    private static class CollectingLogOutputStream extends LogOutputStream {
        private final List<String> lines = new LinkedList<>();
        @Override protected void processLine(String line, int level) {
            lines.add(line);
        }
        public List<String> getLines() {
            return lines;
        }
    }
}
