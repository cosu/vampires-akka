package ro.cosu.vampires.server.util;


import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created on 7-2-16.
 */
public class Ip {
    private static final Logger LOG = LoggerFactory.getLogger(Ip.class);
    private static String HOST = "http://ip.cosu.ro/?ip";

    public static String getPublicIp() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(HOST);
        HttpEntity entity = client.execute(request).getEntity();
        String ip = EntityUtils.toString(entity);
        LOG.info("got ip {}", ip);
        return ip;

    }
}
