/*
 *
 *  * The MIT License (MIT)
 *  * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the “Software”), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *  *
 *
 */

package ro.cosu.vampires.server.values;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import java.time.LocalDateTime;

import ro.cosu.vampires.server.util.gson.AutoValueAdapterFactory;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;
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
        WorkloadPayload workloadPayload = WorkloadPayload.fromConfig(load.getConfig("vampires.workload"));
        Workload workload = Workload.fromPayload(workloadPayload);
        assertThat(workload.id(), not(isEmptyOrNullString()));
    }

    @Test
    public void update() throws Exception {

        Workload workload = Workload.builder()
                .task("foo")
                .build().update().sequenceStart(10).build();

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

        assertThat(fromJson.sequenceStart(), is(15));

    }

    @Test
    public void updateFields() throws Exception {
        Config load = ConfigFactory.load("application-dev.conf");

        WorkloadPayload workloadPayload = WorkloadPayload.
                fromConfig(load.getConfig("vampires.workload"));

        Workload workload = Workload.fromPayload(workloadPayload);

        WorkloadPayload updatedPayload = workloadPayload.toBuilder().sequenceStart(32).sequenceStop(12)
                .id(workload.id())
                .build();

        Workload updatedWorkload = workload.updateFromPayload(updatedPayload);

        assertThat(updatedWorkload.sequenceStart(), is(32));
        assertThat(updatedWorkload.sequenceStop(), is(12));

    }

    @Test
    public void file() throws Exception {

        WorkloadPayload workloadPayload = WorkloadPayload.builder()
                .file("foo\nbar\nbaz").build();

        Workload workload = Workload.fromPayload(workloadPayload);

        assertThat(workload.size(), is(3));
    }
}