package ro.cosu.vampires.server.writers.json;

import com.google.common.io.Files;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.workload.Job;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class JsonResultsWriterTest {
    @Test
    public void testWriteJson() throws Exception {
        final File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();

        final JsonResultsWriter jsonResultsWriter = new JsonResultsWriter(ConfigFactory.parseString("writers.json" +
                ".dir=" + tempDir.getAbsolutePath()));
        jsonResultsWriter.addResult(Job.empty());
        jsonResultsWriter.close();
        assertThat(tempDir.list().length, not(0));
    }
}
