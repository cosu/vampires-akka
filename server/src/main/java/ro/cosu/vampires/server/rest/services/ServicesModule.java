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

import ro.cosu.vampires.server.values.jobs.Execution;
import ro.cosu.vampires.server.values.jobs.ExecutionPayload;
import ro.cosu.vampires.server.values.jobs.Workload;
import ro.cosu.vampires.server.values.jobs.WorkloadPayload;
import ro.cosu.vampires.server.values.resources.Configuration;
import ro.cosu.vampires.server.values.resources.ConfigurationPayload;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConfigurationsService.class).asEagerSingleton();
        bind(ExecutionsService.class).asEagerSingleton();
        bind(WorkloadsService.class).asEagerSingleton();
        bind(ProvidersService.class).asEagerSingleton();

        bind(new TypeLiteral<Service<Configuration, ConfigurationPayload>>() {
        })
                .to(new TypeLiteral<ConfigurationsService>() {
                }).asEagerSingleton();

        bind(new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        })
                .to(new TypeLiteral<WorkloadsService>() {
                }).asEagerSingleton();

        bind(new TypeLiteral<Service<Execution, ExecutionPayload>>() {
        })
                .to(new TypeLiteral<ExecutionsService>() {
                }).asEagerSingleton();
    }
}
