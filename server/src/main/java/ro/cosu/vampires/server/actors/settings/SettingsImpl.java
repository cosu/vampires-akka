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

package ro.cosu.vampires.server.actors.settings;

import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import akka.actor.Extension;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.workload.ExecutionMode;
import ro.cosu.vampires.server.workload.Job;
import ro.cosu.vampires.server.workload.JobUtil;
import ro.cosu.vampires.server.workload.ProviderDescription;
import ro.cosu.vampires.server.workload.ResourceDescription;
import ro.cosu.vampires.server.workload.schedulers.SamplingScheduler;
import ro.cosu.vampires.server.workload.schedulers.Scheduler;
import ro.cosu.vampires.server.workload.schedulers.SimpleScheduler;
import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.writers.json.JsonResultsWriter;
import ro.cosu.vampires.server.writers.mongo.MongoWriter;

public class SettingsImpl implements Extension {

    public static final String SAMPLING_MODE = "sampling";
    private static final int DEFAULT_CPU_SET_SIZE = 1;
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);
    private final static int DEFAULT_MAX_JOB_DEADLINE = 60;
    private final static int DEFAULT_BACK_OFF_INTERVAL = 20;
    private final static String DEFAULT_EXECUTOR = "FORK";
    private static final int JOBS_TO_SAMPLE = 30;
    public final Config vampires;


    public SettingsImpl(Config config) {
        vampires = config.getConfig("vampires");
    }

    public List<ResultsWriter> getWriters() {
        List<ResultsWriter> writers = new LinkedList<>();
        List<String> enabledWriters = vampires.getStringList("enabled-writers");
        if (enabledWriters.contains("json")) {
            writers.add(new JsonResultsWriter(vampires));
        }

        if (enabledWriters.contains("mongo")) {
            writers.add(new MongoWriter());
        }

        if (writers.isEmpty()) {
            LOG.warn("no writers configured!");
        }

        return writers;

    }

    public List<Job> getWorkload() {
        return JobUtil.fromConfig(vampires.getConfig("workload"));
    }

    public Scheduler getScheduler() {
        List<Job> workload = getWorkload();
        if (getMode().equals(ExecutionMode.SAMPLE)) {
            LOG.info("running in sampling mode : sampling from {} jobs", workload.size());
            return new SamplingScheduler(workload, getJobDeadline(), getBackoffInterval(), getNumberOfJobsToSample());
        } else
            return new SimpleScheduler(workload, getJobDeadline(), getBackoffInterval());
    }

    public int getNumberOfJobsToSample() {
        if (vampires.hasPath("sampleSize")) {
            return vampires.getInt("sampleSize");
        } else {
            LOG.warn("Missing sampleSize config value. using default {}", JOBS_TO_SAMPLE);
            return JOBS_TO_SAMPLE;
        }
    }

    public List<String> getExecutors() {
        if (vampires.hasPath("executors")) {
            return vampires.getStringList("executors").stream().map(String::toUpperCase).collect(Collectors.toList());
        } else {
            LOG.warn("Missing executors config value. using default {}", DEFAULT_EXECUTOR);
            return Collections.singletonList(DEFAULT_EXECUTOR);
        }

    }

    public int getBackoffInterval() {
        if (vampires.hasPath("backoffInterval")) {
            return vampires.getInt("backoffInterval");
        } else {
            LOG.warn("missing backoffInterval. Using default value: {}", DEFAULT_BACK_OFF_INTERVAL);
        }
        return DEFAULT_BACK_OFF_INTERVAL;

    }

    public int getCpuSetSize() {
        if (vampires.hasPath("cpuSetSize")) {
            return vampires.getInt("cpuSetSize");
        } else {
            LOG.warn("missing executor cpuSetSize. Using default value: {}", DEFAULT_CPU_SET_SIZE);
        }
        return DEFAULT_CPU_SET_SIZE;
    }

    public int getJobDeadline() {

        int maxJobSeconds = DEFAULT_MAX_JOB_DEADLINE;
        if (vampires.hasPath("jobDeadlineSeconds")) {
            maxJobSeconds = vampires.getInt("jobDeadlineSeconds");
        } else {
            LOG.warn("maxJobSeconds not provided. Using default value of {}", DEFAULT_MAX_JOB_DEADLINE);
        }
        return maxJobSeconds;
    }

    public List<ProviderDescription> getProviders() {
        Config config = vampires.getConfig("resources");

        List<ProviderDescription> list = vampires.getStringList("enabled-resources").stream().map(
                providerType -> {
                    ConfigObject configObject = config.getObject(providerType);

                    Config providerConfig = config.getConfig(providerType);

                    List<ResourceDescription> collect = configObject.keySet().stream()
                            .filter(key ->
                                    // only objects are asociated with providers.
                                    // single values are overrides
                                    providerConfig.getValue(key).valueType().equals(ConfigValueType.OBJECT)
                            )
                            .map(key -> {
                                Config resourceConfig = providerConfig.getConfig(key);
                                int cost = 0;
                                if (resourceConfig.hasPath("cost")) {
                                    cost = resourceConfig.getInt("cost");
                                }
                                return ResourceDescription.create(key, cost);
                            })
                            .collect(Collectors.toList());

                    String name = providerConfig.hasPath("description") ? providerConfig.getString("description") : providerType;

                    return ProviderDescription.builder()
                            .description(name)
                            .provider(Resource.ProviderType.valueOf(providerType.toUpperCase()))
                            .resources(ImmutableList.copyOf(collect))
                            .build();

                }
        ).collect(Collectors.toList());
        return list;
    }

    public ExecutionMode getMode() {
        ExecutionMode executionMode = ExecutionMode.FULL;
        if (vampires.hasPath("mode")) {
            executionMode = Enums.getIfPresent(ExecutionMode.class, vampires.getString("mode").
                    toUpperCase()).or(ExecutionMode.FULL);
        }
        return executionMode;
    }
}

