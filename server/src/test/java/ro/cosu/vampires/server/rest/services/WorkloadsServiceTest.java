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
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.actors.WorkloadsActor;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;

public class WorkloadsServiceTest extends AbstractServiceTest<Workload, WorkloadPayload> {


    private static ActorRef getActor() {
        return actorSystem.actorOf(WorkloadsActor.props());
    }

    @Override
    protected AbstractModule getModule() {
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(new TypeLiteral<WorkloadsService>() {
                });
            }

            @Provides
            ActorRef actor() {
                return getActor();
            }
        };
        return module;
    }

    @Override
    protected TypeLiteral<Service<Workload, WorkloadPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        };
    }

    @Override
    protected WorkloadPayload getPayload() {

        String workloadConfig = "{\n" +
                "    format = %08d.tif\n" +
                "    sequenceStart = 0\n" +
                "    sequenceStop = 10\n" +
                "    task = \"echo\"\n" +
                "    url = \"\"\n" +
                "  }";

        return WorkloadPayload.fromConfig(ConfigFactory.parseString(workloadConfig));
    }

    @Override
    public void update() throws Exception {
        //
    }
}