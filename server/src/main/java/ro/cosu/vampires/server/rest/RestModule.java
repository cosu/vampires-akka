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

package ro.cosu.vampires.server.rest;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import com.typesafe.config.Config;

import java.util.Map;

import akka.actor.ActorRef;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.rest.controllers.ControllersModule;
import ro.cosu.vampires.server.rest.services.ServicesModule;
import ro.cosu.vampires.server.values.resources.ProviderDescription;


public class RestModule extends AbstractModule {
    private final ActorRef actorRef;


    private final Config config;
    private Map<Resource.ProviderType, ProviderDescription> providers;

    public RestModule(ActorRef actorRef, Map<Resource.ProviderType, ProviderDescription> providers, Config config) {
        this.actorRef = actorRef;
        this.providers = providers;
        this.config = config;
    }

    @Override
    protected void configure() {
        install(new ServicesModule());
        install(new ControllersModule());
    }

    @Provides
    public Map<Resource.ProviderType, ProviderDescription> getProviders() {
        return providers;
    }

    @Provides
    public ActorRef getActorRef() {
        return actorRef;
    }

    @Provides
    public Config getConfig() {
        return config;
    }

    private boolean withAuth() {
        return config.hasPath("auth") && config.getBoolean("auth");
    }
}
