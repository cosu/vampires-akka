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


import com.google.common.collect.Maps;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorSystem;
import ro.cosu.vampires.server.settings.Settings;
import ro.cosu.vampires.server.settings.SettingsImpl;
import ro.cosu.vampires.server.workload.Workload;

public class WorkloadsService {
    private Map<String, Workload> workloads = Maps.newConcurrentMap();
    private ActorSystem actorSystem;

    @Inject
    WorkloadsService(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        Workload workload = Workload.fromConfig(getSettings().vampires.getConfig("workload"));
        workloads.put(workload.id(), workload);
    }

    private SettingsImpl getSettings() {
        SettingsImpl settings = Settings.SettingsProvider.get(actorSystem);
        return settings;
    }

    public Collection<Workload> getWorkloads() {
        return workloads.values();
    }

    public Workload createWorkload(Workload workload) {

        workloads.put(workload.id(), workload);

        return workload;
    }


    public Optional<Workload> getWorkload(String id) {
        return Optional.ofNullable(workloads.get(id));
    }


    public Optional<Workload> delete(String id) {
        return Optional.ofNullable(workloads.remove(id));
    }

    public Optional<Workload> updateWorkload(Workload workload) {
        if (workloads.containsKey(workload.id())) {
            Workload updated = workloads.get(workload.id()).withUpdate(workload);
            workloads.put(updated.id(), updated);
            return Optional.of(updated);
        } else {
            return Optional.empty();
        }
    }
}
