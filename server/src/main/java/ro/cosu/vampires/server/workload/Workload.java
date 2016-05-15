package ro.cosu.vampires.server.workload;


import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;

import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import ro.cosu.vampires.server.util.gson.AutoGson;

@AutoValue
@AutoGson

public abstract class Workload implements Id {

    private static final Logger LOG = LoggerFactory.getLogger(Workload.class);

    public static Workload fromConfig(Config config) {
        String format = "";
        if (config.hasPath("format")) {
            format = config.getString("format");
        }

        String url = "";
        if (config.hasPath("url")) {
            url = config.getString("url");
        }

        int sequenceStart = config.getInt("sequenceStart");
        int sequenceStop = config.getInt("sequenceStop");
        String task = config.getString("task");

        return builder().format(format)
                .task(task)
                .url(url)
                .sequenceStart(sequenceStart)
                .sequenceStop(sequenceStop)
                .build();
    }

    public static Workload.Builder builder() {
        return new AutoValue_Workload.Builder().id(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .format("")
                .sequenceStart(0)
                .sequenceStop(0);
    }


    public static Workload fromPayload(WorkloadPayload payload) {
        return new AutoValueUtil<WorkloadPayload, Workload.Builder>() {
        }
                .builderFromPayload(payload, builder()).build();
    }

    public abstract String id();

    @Nullable
    public abstract LocalDateTime createdAt();

    @Nullable
    public abstract LocalDateTime updatedAt();

    public abstract int sequenceStart();

    public abstract int sequenceStop();

    @Nullable
    public abstract String task();

    @Nullable
    public abstract String format();

    @Nullable
    public abstract String url();

    public abstract Builder toBuilder();

    public Builder update() {
        return toBuilder()
                .updatedAt(LocalDateTime.now());
    }

    public Workload touch() {
        return toBuilder().updatedAt(LocalDateTime.now()).build();
    }


    public List<Job> getJobs() {
        final String finalUrl = url();
        final String finalFormat = format();
        return IntStream.rangeClosed(sequenceStart(), sequenceStop()).mapToObj(i -> String.format(finalFormat, i))
                .map(f -> String.format("%s %s%s", task(), finalUrl, f).trim())
                .map(command -> Job.empty().withCommand(command))
                .collect(Collectors.toList());

    }

    public Workload withUpdate(Workload workloadUpdate) {
        //TODO this should live in a separate class so that all autovalues can share it
        Builder builder = toBuilder().updatedAt(LocalDateTime.now());

        Set<String> forbiddenMethods = Sets.newHashSet("toBuilder", "equals", "id", "toString",
                "hashCode", "$jacocoInit");

        Arrays.stream(getClass().getDeclaredMethods())
                .filter(method -> !forbiddenMethods.contains(method.getName()))
                .forEach(method -> {
                    try {
                        Object invoke = method.invoke(workloadUpdate);

                        Method builderMethod = builder.getClass()
                                .getMethod(method.getName(), method.getReturnType());

                        if (invoke != null) {
                            builderMethod.invoke(builder, invoke);
                        }

                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        LOG.error("Error calling on " + method.getName() + " updating " + this, e);
                    }
                });

        return builder.build();

    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder id(String id);

        public abstract Builder sequenceStart(int sequenceStrat);

        public abstract Builder createdAt(LocalDateTime createdAt);

        public abstract Builder updatedAt(LocalDateTime createdAt);

        public abstract Builder sequenceStop(int sequenceStop);

        public abstract Builder task(String task);

        public abstract Builder format(String format);

        public abstract Builder url(String format);

        public abstract Workload build();
    }
}
