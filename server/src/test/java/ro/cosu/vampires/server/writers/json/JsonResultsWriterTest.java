package ro.cosu.vampires.server.writers.json;

import com.google.common.io.Files;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import ro.cosu.vampires.server.workload.Job;

import java.io.File;

public class JsonResultsWriterTest {
    @Test
    public void testWriteJson() throws Exception {
        final File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        System.out.println(tempDir.getAbsolutePath());

        final JsonResultsWriter jsonResultsWriter = new JsonResultsWriter(ConfigFactory.parseString("writers.json" +
                ".dir=" + tempDir.getAbsolutePath()));
        jsonResultsWriter.writeResult(Job.empty());
        jsonResultsWriter.close();

    }
}
