package ro.cosu.vampires.server.workload;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import java.time.LocalDateTime;

import ro.cosu.vampires.server.util.gson.AutoValueAdapterFactory;
import ro.cosu.vampires.server.writers.json.LocalDateTimeDeserializer;
import ro.cosu.vampires.server.writers.json.LocalDateTimeSerializer;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;


public class WorkloadTest {
    @Test
    public void fromConfig() throws Exception {
        Config load = ConfigFactory.load("application-dev.conf");
        Workload workload = Workload.fromConfig(load.getConfig("vampires.workload"));
        assertThat(workload.id(), not(isEmptyOrNullString()));
    }

    @Test
    public void updateSingleField() throws Exception {
        Workload workload = Workload.builder().build().update().sequenceStart(10).build();

        assertThat(workload.sequenceStart(), is(10));

    }

    @Test
    public void update() throws Exception {

        Workload workload = Workload.builder().build().update().sequenceStart(10).build();

        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        String j = "{\n" +
                "  \"id\": \"" + workload.id() + "\",\n" +
                "  \"sequence_start\": 15,\n" +
                "  \"sequence_stop\": 0\n" +
                "}";

        Workload fromJson = gson.fromJson(j, Workload.class);

        assertThat(fromJson.id(), is(workload.id()));
        Workload workload1 = workload.withUpdate(fromJson);

        assertThat(workload1.sequenceStart(), is(15));

    }

    @Test
    public void updateFields() throws Exception {
        Config load = ConfigFactory.load("application-dev.conf");


        Workload workload = Workload.fromConfig(load.getConfig("vampires.workload"));

        Workload update = Workload.builder().sequenceStart(32).sequenceStop(12).build();

        Workload updatedWorkload = workload.withUpdate(update);

        assertThat(updatedWorkload.sequenceStart(), is(32));
        assertThat(updatedWorkload.sequenceStop(), is(12));

    }
}