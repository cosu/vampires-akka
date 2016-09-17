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

import com.google.common.collect.ImmutableMap;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import akka.actor.Extension;
import ro.cosu.vampires.server.resources.Resource;
import ro.cosu.vampires.server.values.User;
import ro.cosu.vampires.server.values.jobs.Job;
import ro.cosu.vampires.server.values.jobs.JobUtil;
import ro.cosu.vampires.server.values.resources.ProviderDescription;
import ro.cosu.vampires.server.values.resources.ResourceDescription;
import ro.cosu.vampires.server.writers.ResultsWriter;
import ro.cosu.vampires.server.writers.json.JsonResultsWriter;
import ro.cosu.vampires.server.writers.mongo.MongoWriter;

public class SettingsImpl implements Extension {

    public static final String SAMPLING_MODE = "sampling";
    public static final String MODE = "mode";
    private static final String SAMPLE_SIZE = "sample-size";
    private static final String EXECUTORS = "executors";
    private static final String CPU_SET_SIZE = "cpu-set-size";
    private static final String JOB_DEADLINE_SECONDS = "job-deadline-seconds";
    private static final String ENABLED_WRITERS = "enabled-writers";
    private static final String BACKOFF_INTERVAL_SECONDS = "backoff-interval-seconds";
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
        List<String> enabledWriters = vampires.getStringList(ENABLED_WRITERS);
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

    public int getNumberOfJobsToSample() {
        if (vampires.hasPath(SAMPLE_SIZE)) {
            return vampires.getInt(SAMPLE_SIZE);
        } else {
            LOG.warn("Missing sampleSize config value. using default {}", JOBS_TO_SAMPLE);
            return JOBS_TO_SAMPLE;
        }
    }

    public List<String> getExecutors() {
        if (vampires.hasPath(EXECUTORS)) {
            return vampires.getStringList(EXECUTORS).stream().map(String::toUpperCase).collect(Collectors.toList());
        } else {
            LOG.warn("Missing executors config value. using default {}", DEFAULT_EXECUTOR);
            return Collections.singletonList(DEFAULT_EXECUTOR);
        }

    }

    public int getBackoffInterval() {
        if (vampires.hasPath(BACKOFF_INTERVAL_SECONDS)) {
            return vampires.getInt(BACKOFF_INTERVAL_SECONDS);
        } else {
            LOG.warn("missing " + BACKOFF_INTERVAL_SECONDS + " . Using default value: {}", DEFAULT_BACK_OFF_INTERVAL);
        }
        return DEFAULT_BACK_OFF_INTERVAL;

    }

    public int getCpuSetSize() {
        if (vampires.hasPath(CPU_SET_SIZE)) {
            return vampires.getInt(CPU_SET_SIZE);
        } else {
            LOG.warn("missing executor " + CPU_SET_SIZE + " . Using default value: {}", DEFAULT_CPU_SET_SIZE);
        }
        return DEFAULT_CPU_SET_SIZE;
    }

    public int getJobDeadline() {

        int maxJobSeconds = DEFAULT_MAX_JOB_DEADLINE;
        if (vampires.hasPath(JOB_DEADLINE_SECONDS)) {
            maxJobSeconds = vampires.getInt(JOB_DEADLINE_SECONDS);
        } else {
            LOG.warn("maxJobSeconds not provided. Using default value of {}", DEFAULT_MAX_JOB_DEADLINE);
        }
        return maxJobSeconds;
    }

    public Map<Resource.ProviderType, ProviderDescription> getProviders() {
        Config resourcesConfig = vampires.getConfig("resources");
        return vampires.getStringList("enabled-resources").stream().map(
                enabledProvider -> {
                    Config providerConfig = resourcesConfig.getConfig(enabledProvider);
                    String name = providerConfig.hasPath("name") ? providerConfig.getString("name") : enabledProvider;
                    // iterate over the keys of resourcesConfig
                    Map<String, ResourceDescription> resourceDescriptions = resourcesConfig.getObject(enabledProvider).keySet().stream()
                            // only objects are associated with providers.
                            // single values are overrides
                            .filter(key -> providerConfig.getValue(key).valueType().equals(ConfigValueType.OBJECT))
                            .map(key -> {
                                Config resourceConfig = providerConfig.getConfig(key);
                                double cost = resourceConfig.hasPath("cost") ? resourceConfig.getDouble("cost") : 0.;
                                Resource.ProviderType pt = Resource.ProviderType.valueOf(enabledProvider.toUpperCase());
                                return
                                        ResourceDescription.builder().provider(pt).type(key).cost(cost).build();
                            })
                            .collect(Collectors.toMap(ResourceDescription::type, Function.identity()));
                    return ProviderDescription.builder()
                            .description(name)
                            .provider(Resource.ProviderType.valueOf(enabledProvider.toUpperCase()))
                            .resourceDescriptions(ImmutableMap.copyOf(resourceDescriptions))
                            .build();

                }
        ).collect(Collectors.toMap(ProviderDescription::provider, Function.identity()));
    }


    public User getDefaultUser() {
        return User.admin();
    }
}

