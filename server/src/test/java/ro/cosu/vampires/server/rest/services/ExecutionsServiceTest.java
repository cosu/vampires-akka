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

package ro.cosu.vampires.server.rest.services;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ExecutionInfo;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.ExecutionPayload;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class ExecutionsServiceTest extends AbstractServiceTest<Execution, ExecutionPayload> {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("test", ConfigFactory.load("application-dev.conf"));
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Override
    protected AbstractModule getModule() {
        return new ServicesTestModule(system);
    }

    @Override
    protected TypeLiteral<Service<Execution, ExecutionPayload>> getTypeTokenService() {
        return ExecutionsService.getTypeTokenService();
    }

    @Override
    protected ExecutionPayload getPayload() {
        return ExecutionPayload.builder().configuration("foo").workload("bar").type(ExecutionMode.FULL).build();
    }

    @Override
    @Test
    public void delete() throws Exception {
        assertThat(instance.list().size(), is(1));
        String id = instance.list().iterator().next().id();
        Optional<Execution> delete = instance.delete(id);
        assertThat(delete.isPresent(), is(true));
        assertThat(delete.get().info().status(), is(ExecutionInfo.Status.CANCELED));
    }

    @Override
    public void update() throws Exception {
        //
    }

}