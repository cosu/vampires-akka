package ro.cosu.vampires.server.util;

import com.google.common.annotations.VisibleForTesting;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class SshClient {

    private static final Logger LOG = LoggerFactory.getLogger(SshClient.class);
    private JSch jsch = new JSch();

    @VisibleForTesting
    protected void setJsch(JSch jsch){
        this.jsch = jsch;
    }

    public String runCommand(String user, String privateKey, String address, String command, int port) throws JSchException,
            IOException {
        LOG.debug("SSH: {}@{}:{}({}) command: {}",  user, address , port , privateKey ,command);

        Session session = getSession(user, privateKey, address, port);

        ChannelExec channel = getChannelExec(session, command);
        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));

        String msg;
        StringBuilder sb = new StringBuilder();
        while ((msg = in.readLine()) != null) {
            sb.append(msg);
        }

        channel.disconnect();
        session.disconnect();
        return sb.toString();

    }

    private ChannelExec getChannelExec(Session session, String command) throws JSchException, IOException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();
        return channel;
    }

    private Session getSession(String user, String privateKey, String address, int port) throws JSchException {
        jsch.setLogger( new JSCHLogger());
        Session session = jsch.getSession(user, address, port);
        jsch.addIdentity(privateKey);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }

    private static class JSCHLogger implements com.jcraft.jsch.Logger
    {

        public static java.util.Hashtable<Integer,String> name = new java.util.Hashtable<>();

        static
        {
            name.put(DEBUG, "DEBUG: ");
            name.put(INFO, "INFO: ");
            name.put(WARN, "WARN: ");
            name.put(ERROR, "ERROR: ");
            name.put(FATAL, "FATAL: ");
        }

        public boolean isEnabled(int level)
        {
            return true;
        }

        public void log(int level, String message)
        {
            if(level == DEBUG)
            {
                LOG.debug(message);
            }
            else if(level == INFO)
            {
                LOG.info(message);
            }
            else if(level == WARN)
            {
                LOG.warn(message);
            }
            else if(level == ERROR)
            {
                LOG.error(message);
            }
            else if(level == FATAL)
            {
                LOG.error(message);
            }
        }
    } }