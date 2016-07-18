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

package ro.cosu.vampires.server.rest.controllers;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.actors.ConfigurationsActor;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.rest.services.ConfigurationsService;
import ro.cosu.vampires.server.rest.services.Service;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.ConfigurationPayload;
import ro.cosu.vampires.server.workload.ResourceDemand;
import ro.cosu.vampires.server.workload.ResourceDescription;


public class ConfigurationsControllerTest extends AbstractControllerTest<Configuration, ConfigurationPayload> {

    private static ActorRef getActor() {
        return actorSystem.actorOf(ConfigurationsActor.props());
    }

    protected AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(getTypeTokenService()).to(ConfigurationsService.class).asEagerSingleton();
                bind(ConfigurationsController.class).asEagerSingleton();
            }

            @Provides
            ActorRef actor() {
                return getActor();
            }
        };
    }

    @Override
    protected TypeLiteral<Service<Configuration, ConfigurationPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        };
    }

    @Override
    protected ConfigurationPayload getPayload() {
        ImmutableList<ResourceDemand> resourceDemands = ImmutableList.of(
                ResourceDemand.builder()
                        .count(1)
                        .resourceDescription(ResourceDescription.create("bar", Resource.ProviderType.MOCK, 0L))
                        .build());
        return ConfigurationPayload.create("foo", resourceDemands);
    }

    @Override
    protected String getPath() {
        return "/configurations";
    }

}