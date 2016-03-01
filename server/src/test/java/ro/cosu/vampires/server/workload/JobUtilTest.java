package ro.cosu.vampires.server.workload;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class JobUtilTest {

    @Test
    public void testBag() throws Exception {

        Config config = ConfigFactory.load().getConfig("vampires.workload");

        List<Job> jobs = JobUtil.fromConfig(config);

        assertThat(jobs.size(), not(0));

    }

    @Test
    public void testBagFromFile() throws Exception {
        File tempFile = File.createTempFile("foo", "bar");
        tempFile.deleteOnExit();

        Files.write("foo\nbar\nbaz", tempFile, Charsets.UTF_8);

        List<Job> jobs = JobUtil.bagFromFile("foo", tempFile);

        assertThat(jobs.size(), is(3));

    }
}