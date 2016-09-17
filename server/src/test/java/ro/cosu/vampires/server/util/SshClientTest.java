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

package ro.cosu.vampires.server.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;


public class SshClientTest {

    @Test
    public void testRunCommand() throws Exception {
        SshClient sshClient = getSshClient(0);

        String result = sshClient.runCommand("user", "privateKey", "address", "cmd", 1);
        assertThat(result, is("resulterror"));
    }

    @Test(expected = RuntimeException.class)
    public void testRunCommandFail() throws Exception {
        SshClient sshClient = getSshClient(1);
        String result = sshClient.runCommand("user", "privateKey", "address", "cmd", 1);

        assertThat(result, is("resulterror"));
    }

    private SshClient getSshClient(int value) throws JSchException, IOException {
        SshClient sshClient = new SshClient();
        JSch jschMock = Mockito.mock(JSch.class);
        sshClient.setJsch(jschMock);

        Session session = Mockito.mock(Session.class);
        ChannelExec channelExec = Mockito.mock(ChannelExec.class);

        when(jschMock.getSession("user", "address", 1)).thenReturn(session);
        when(session.openChannel("exec")).thenReturn(channelExec);
        when(channelExec.getInputStream()).thenReturn(new ByteArrayInputStream("result".getBytes(StandardCharsets.UTF_8)));
        when(channelExec.getErrStream()).thenReturn(new ByteArrayInputStream("error".getBytes(StandardCharsets.UTF_8)));
        when(channelExec.getExitStatus()).thenReturn(value);
        when(channelExec.isClosed()).thenReturn(true);
        return sshClient;
    }
}