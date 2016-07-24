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

package ro.cosu.vampires.server.values.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationIterator implements Iterator<Configuration> {

    private final Map<ResourceDescription, Integer> maxResources;
    private Map<ResourceDescription, Integer> lastSchedule;

    public ConfigurationIterator(Map<ResourceDescription, Integer> maxResources) {
        this.maxResources = maxResources;
        lastSchedule = Maps.newHashMap(maxResources);
        lastSchedule.keySet().forEach(k -> lastSchedule.put(k, 0));
    }

    @Override
    public Configuration next() {
        Map<ResourceDescription, Integer> schedule = Maps.newHashMap(lastSchedule);

        for (ResourceDescription k : schedule.keySet()) {
            if (schedule.get(k) < maxResources.get(k)) {
                schedule.put(k, schedule.get(k) + 1);
                break;
            }
        }

        lastSchedule = schedule;

        List<ResourceDemand> collect = lastSchedule.keySet().stream().map(k -> ResourceDemand.builder().count(lastSchedule.get(k))
                .resourceDescription(k).build()
        ).collect(Collectors.toList());

        return Configuration.builder().resources(ImmutableList.copyOf(collect)).build();
    }

    public boolean hasNext() {
        Integer current = lastSchedule.values().stream().reduce(0, (a, b) -> a + b);
        Integer max = maxResources.values().stream().reduce(0, (a, b) -> a + b);
        return current < max;
    }



}
