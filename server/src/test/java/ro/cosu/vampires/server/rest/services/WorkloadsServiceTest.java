/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.rest.services;


import com.google.inject.Guice;
import com.google.inject.Injector;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import ro.cosu.vampires.server.actors.AbstractActorTest;
import ro.cosu.vampires.server.workload.Workload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class WorkloadsServiceTest extends AbstractActorTest {


    private static Injector injector;
    private WorkloadsService workloadsService;

    @BeforeClass
    public static void setUpClass() {
        ServicesModule controllersModule = new ServicesModule(getActorSystem());
        injector = Guice.createInjector(controllersModule);
    }
    @AfterClass
    public static void tearDown() {
        injector = null;
    }


    @Before
    public void setUp() {
        workloadsService = injector.getInstance(WorkloadsService.class);
    }

    @Test
    public void getWorkloads() throws Exception {
        assertThat(workloadsService.getWorkloads().size(), not(0));
    }

    @Test
    public void deleteWorkload() throws Exception {
        assertThat(workloadsService.getWorkloads().size() , not(0));

        Workload workload = workloadsService.getWorkloads().iterator().next();
        String id = workload.id();

        Optional<Workload> deleted = workloadsService.delete(id);
        assertThat(deleted.isPresent(), is(true));
        assertThat(workloadsService.getWorkloads().size(), is(0));
    }

    @Test
    public void getWorkload() throws Exception {
        Workload workload1 = workloadsService.getWorkloads().iterator().next();
        String id = workload1.id();
        Optional<Workload> workload = workloadsService.getWorkload(id);
        assertThat(workload.isPresent(), is(true));
    }

    @Test
    public void getNonWorkload() throws Exception {
        Optional<Workload> workload = workloadsService.getWorkload("unknown");
        assertThat(workload.isPresent(), is(false));
    }

    @Test
    public void updateWorkload() throws Exception {
        Workload original = workloadsService.getWorkloads().iterator().next();
        Workload update = original.update().sequenceStart(50).build();
        Optional<Workload> saved = workloadsService.updateWorkload(update);

        assertThat(update.sequenceStart(), is(saved.get().sequenceStart()));
    }

    @Test
    public void createWorkload() throws Exception {
        String workloadConfig = "workload {\n" +
                "    format = %08d.tif\n" +
                "    sequenceStart = 0\n" +
                "    sequenceStop = 10\n" +
                "    task = \"echo\"\n" +
                "    url = \"\"\n" +
                "  }";
        Workload workload = Workload.fromConfig(ConfigFactory.parseString(workloadConfig));
        assertThat(workload.task(), is("echo"));

    }


}