package ro.cosu.vampires.server.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created on 7-2-16.
 */
public class IpTest {

    @Test
    public void testGetPublicIp() throws Exception {
        assertThat(Ip.getPublicIp(), not(""));
    }
}