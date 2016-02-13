package ro.cosu.vampires.server.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;


public class SshTest {

    @Test
    public void testRunCommand() throws Exception {
        Ssh ssh = new Ssh();
        JSch jschMock = Mockito.mock(JSch.class);
        ssh.setJsch(jschMock);

        Session session = Mockito.mock(Session.class);
        ChannelExec channelExec = Mockito.mock(ChannelExec.class);

        when(jschMock.getSession("user", "address", 1)).thenReturn(session);
        when(session.openChannel("exec")).thenReturn(channelExec);
        when(channelExec.getInputStream()).thenReturn(new ByteArrayInputStream( "result".getBytes() ));

        String result = ssh.runCommand("user", "privateKey", "address", "cmd", 1);

        assertThat(result, is("result"));

    }
}