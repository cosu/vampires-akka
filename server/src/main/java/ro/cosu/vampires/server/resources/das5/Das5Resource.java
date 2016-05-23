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

package ro.cosu.vampires.server.resources.das5;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.util.SshClient;

import java.io.IOException;

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
        String command = "sbatch -N 1 " + parameters.command() + " " + parameters.serverId()
                + " " + parameters.id();
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
