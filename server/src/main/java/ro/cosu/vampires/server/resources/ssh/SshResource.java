/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.resources.ssh;

import com.jcraft.jsch.JSchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import ro.cosu.vampires.server.resources.AbstractResource;
import ro.cosu.vampires.server.util.SshClient;


public class SshResource extends AbstractResource {
    private static final Logger LOG = LoggerFactory.getLogger(SshResource.class);

    private final SshResourceParameters parameters;
    private final SshClient sshClient;
    private String commandOutput;


    public SshResource(SshResourceParameters parameters, SshClient sshClient) {
        super(parameters);
        this.parameters = parameters;
        this.sshClient = sshClient;
    }

    @Override
    public void onStart() throws IOException, JSchException {
        String command = "nohup " + parameters.command() + " " + parameters().id() + " > /dev/null 2>&1 &  echo $! ";
        this.commandOutput = exec(command);

    }

    @Override
    public void onStop() throws IOException, JSchException {
        String command = "kill " + Integer.parseInt(commandOutput);
        this.commandOutput = exec(command);
    }

    @Override
    public void onFail() throws IOException, JSchException {
        String command = "kill -9" + Integer.parseInt(commandOutput);
        this.commandOutput = exec(command);
    }


    private String exec(String command) throws IOException, JSchException {
        LOG.debug("ssh command: " + command);
        return sshClient.runCommand(parameters.user(), parameters.privateKey(), parameters.address(), command, parameters
                .port());
    }


    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
