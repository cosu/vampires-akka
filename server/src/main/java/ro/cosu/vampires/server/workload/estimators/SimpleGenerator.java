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

package ro.cosu.vampires.server.workload.estimators;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.Configuration;
import ro.cosu.vampires.server.workload.Execution;
import ro.cosu.vampires.server.workload.ProviderDescription;
import ro.cosu.vampires.server.workload.ResourceDemand;
import ro.cosu.vampires.server.workload.ResourceDescription;
import ro.cosu.vampires.server.workload.User;

public class SimpleGenerator implements Generator {
    private int maxResourcesPerProvider;
//    private final HashMap<String, Worker> workerParams;
//    private final int maxWorkers;
//    private Schedule currentSchedule;
//    private double currentId;
//    private double totalSchedules;
//
//    private HashBasedTable<Resource.ProviderType, ResourceDescription, Long> configurations= HashBasedTable.create();
    private Map<ResourceDescription, Long> configurations = Maps.newHashMap();

    public SimpleGenerator(int maxResourcesPerProvider, List<ResourceDescription> resourceDescriptions) {
        this.maxResourcesPerProvider = maxResourcesPerProvider;
        // init to 0
        resourceDescriptions.stream().forEach(rd -> configurations.put(rd, 0L));

    }

    public void getNextSchedule() {

    }
//    public ScheduleGenerator(HashMap<String, Worker> workerParams, int maxWorkers) {
//
//        this.workerParams = workerParams;
//        this.maxWorkers = maxWorkers;
//
//        HashMap<String, Integer> configuration = new HashMap<String, Integer>();
//
//        totalSchedules = 1.;
//        for (Worker worker : workerParams.values()) {
//            configuration.put(worker.getType(), 0);
//            logger.info("getMaxWorkers" + worker.getMaxWorkers());
//            totalSchedules *= (worker.getMaxWorkers() + 1);
//        }
//        logger.info("total sched" + totalSchedules);
//
//        currentId = 0;
//        currentSchedule = new Schedule(configuration, workerParams, currentId, 0.);
//
//    }

//    public Schedule getNextSchedule() {

//        HashMap<String, Integer> configuration = new HashMap<String, Integer>(currentSchedule.getSchedule());
//
//        int sum = Integer.MAX_VALUE;
//
//        while (sum > maxWorkers && hasNext()) {
//            for (String workerType : configuration.keySet()) {
//
//                int currentValue = configuration.get(workerType);
//
//                if (configuration.get(workerType) < workerParams.get(workerType).getMaxWorkers()) {
//                    currentValue++;
//                    configuration.put(workerType, currentValue);
//
//                    break;
//                } else {
//                    configuration.put(workerType, 0);
//                }
//            }
//
//            sum = 0;
//            for (String workerType : configuration.keySet()) {
//                sum += configuration.get(workerType);
//            }
//            currentId++;
//        }
//
//
//        if (sum <= maxWorkers)
//            currentSchedule = new Schedule(configuration, workerParams, currentId, 0);
//
//
//        return currentSchedule;
//    }
//
//    public boolean hasNext() {
//
//        if (totalSchedules - currentId > 1) {
//            return true;
//        } else {
//            return false;
//        }
//
//    }
}
