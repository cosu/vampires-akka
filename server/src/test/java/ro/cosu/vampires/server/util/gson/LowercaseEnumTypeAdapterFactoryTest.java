package ro.cosu.vampires.server.util.gson;

import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import ro.cosu.vampires.server.resources.Resource.Status;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LowercaseEnumTypeAdapterFactoryTest {
    @Test
    public void create() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Map<String, Status> map = ImmutableMap.of("foo", Status.STARTING);

        String json = gson.toJson(map);

        assertThat(json, containsString("starting"));

        Type type = new TypeToken<Map<String, Status>>() {
        }.getType();
        Map<String, Status> status = gson.fromJson(json, type);
        assertThat(status.get("foo"), is(Status.STARTING));


    }

}