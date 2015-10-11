package ro.cosu.vampires.server.util;

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

public class Ssh {
    static final Logger LOG = LoggerFactory.getLogger(Ssh.class);


    public static String runCommand(String user, String privateKey, String address, String command, int port) throws JSchException,
            IOException {
        JSch jsch = new JSch();

        Session session = jsch.getSession(user, address, port);
        jsch.addIdentity(privateKey);

        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        LOG.debug("SSH@{}:{} command: {}",  address , port ,command);
        channel.setCommand(command);
        channel.connect();
        String msg;

        StringBuilder sb = new StringBuilder();
        while ((msg = in.readLine()) != null) {
            sb.append(msg);
        }

        channel.disconnect();
        session.disconnect();
        return sb.toString();

    }
}
