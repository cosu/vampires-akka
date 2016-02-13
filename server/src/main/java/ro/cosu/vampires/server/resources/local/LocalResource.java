package ro.cosu.vampires.server.resources.local;

import autovalue.shaded.com.google.common.common.base.Joiner;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class LocalResource extends AbstractResource{
    private static final Logger LOG = LoggerFactory.getLogger(LocalResource.class);
    private final LocalResourceParameters parameters;
    private CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();


    public LocalResource(LocalResourceParameters parameters) {
        super(parameters);
        this.parameters= parameters;
    }

    @Override
    public void onStart() throws IOException {

        // TODO check somehow that the file exists and then exit
        CommandLine cmd = new CommandLine("/bin/sh");
        cmd.addArgument("-c");
        cmd.addArgument("nohup " + parameters.command() + " "+ description().id()  + " 2>&1 0</dev/null  & echo $! ", false);

        LOG.debug("local starting {}", cmd);
        execute(cmd);


    }

    private void execute(CommandLine cmd) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());
        executor.setStreamHandler(new PumpStreamHandler(collectingLogOutputStream));
        executor.setWatchdog(new ExecuteWatchdog(10000));

        int exitCode = 0;
        try {
            LOG.debug("execute {}", cmd.toString());
            exitCode = executor.execute(cmd);
            LOG.debug("Output {}", collectingLogOutputStream.getLines());
        } catch (IOException e) {
            LOG.debug("{} has failed with error {}: {}", this, exitCode, e);
            LOG.debug("{}", Joiner.on("\n").join(collectingLogOutputStream.getLines()));
            throw e;
        }
    }

    @Override
    public void onStop() throws IOException {
        LOG.debug("local stopping");
        try {
            Integer pid = collectingLogOutputStream.getLines().stream().map(Integer::parseInt).findFirst().get();
            CommandLine cmd = new CommandLine("/bin/sh");
            cmd.addArgument("-c");
            cmd.addArgument("kill " + pid, false);
            execute(cmd);
        }
        catch (NumberFormatException ex) {
            LOG.warn("Failed to get pid value. nohup process exited prematurely");
        }

    }

    @Override
    public void onFail() throws IOException {
        LOG.debug("local fail");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
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
