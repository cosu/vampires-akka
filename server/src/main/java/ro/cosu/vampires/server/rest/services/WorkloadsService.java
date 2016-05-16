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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.TypeLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import ro.cosu.vampires.server.workload.Workload;
import ro.cosu.vampires.server.workload.WorkloadPayload;


public class WorkloadsService implements Service<Workload, WorkloadPayload> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkloadsService.class);

    private Map<String, Workload> workloads = Collections.synchronizedSortedMap(Maps.newTreeMap());

    WorkloadsService() {
        LOG.debug("init");
    }
    public static TypeLiteral<Service<Workload, WorkloadPayload>> getTypeTokenService() {
        return new TypeLiteral<Service<Workload, WorkloadPayload>>() {
        };
    }

    @Override
    public Collection<Workload> list() {
        return workloads.values();
    }

    @Override
    public Workload create(WorkloadPayload payload) {

        Workload created = Workload.fromPayload(payload);
        workloads.put(created.id(), created);
        LOG.debug("Created  {} : {}", created.id(), created);
        return created;
    }

    @Override
    public Optional<Workload> delete(String id) {
        return Optional.ofNullable(workloads.remove(id));
    }

    @Override
    public Optional<Workload> update(WorkloadPayload updated) {
        Preconditions.checkNotNull(updated.id(), "id must not be empty");

        if (workloads.containsKey(updated.id())) {
            Workload workload = workloads.get(updated.id());
            workload = workload.updateWithPayload(updated);

            workloads.put(updated.id(), workload);
            return Optional.of(workload);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Workload> get(String id) {
        return Optional.ofNullable(workloads.get(id));
    }
}
