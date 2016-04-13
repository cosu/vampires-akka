/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.resources.local;

import com.google.common.base.Joiner;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


public class LocalResource extends AbstractResource {
    private static final Logger LOG = LoggerFactory.getLogger(LocalResource.class);
    private final LocalResourceParameters parameters;
    private CollectingLogOutputStream collectingLogOutputStream = new CollectingLogOutputStream();

    private Executor executor;

    public LocalResource(LocalResourceParameters parameters, Executor executor) {
        super(parameters);
        this.executor = executor;
        this.parameters = parameters;
    }

    @Override
    public void onStart() throws IOException {
        // TODO check somehow that the file exists and then exit
        CommandLine cmd = new CommandLine("/bin/sh");
        cmd.addArgument("-c");
        cmd.addArgument("nohup " + parameters.command() + " " + description().id() + " 2>&1 0</dev/null & echo $! ", false);

        LOG.debug("local starting {}", cmd);
        execute(cmd);
    }

    private void execute(CommandLine cmd) throws IOException {
        executor.setWorkingDirectory(Paths.get("").toAbsolutePath().toFile());
        executor.setStreamHandler(new PumpStreamHandler(collectingLogOutputStream));
        executor.setWatchdog(new ExecuteWatchdog(10000));

        int exitCode = 0;
        try {
            LOG.debug("execute {}", cmd.toString());
            exitCode = executor.execute(cmd);
            LOG.debug("Output {}", collectingLogOutputStream.getLines());
            if (exitCode != 0) throw new IOException("Non zero exit code");
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
            // the first line has the pid.
            Optional<Integer> firstInt = collectingLogOutputStream.getLines().stream().map(Integer::parseInt).findFirst();
            if (firstInt.isPresent()) {
                int pid = firstInt.get();
                CommandLine cmd = new CommandLine("/bin/sh");
                cmd.addArgument("-c");
                cmd.addArgument("kill " + pid, false);
                execute(cmd);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException | NoSuchElementException ex) {
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

        @Override
        protected void processLine(String line, int level) {
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }
    }
}
