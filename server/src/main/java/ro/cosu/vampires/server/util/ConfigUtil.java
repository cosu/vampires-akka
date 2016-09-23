package ro.cosu.vampires.server.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import com.typesafe.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConfigUtil {

    public static Config getConfigForKey(Config config, String... keys) {
        // generate all the keys from least specific to most specific
        // foo, bar baz -> foo, foo.bar, foo.bar.baz
        Config mergedConfig = config;

        List<String> strings = Arrays.asList(keys);
        List<String> resolvedKeys = IntStream.rangeClosed(1, keys.length).boxed().map(
                i -> Joiner.on(".").join(strings.subList(0, i)))
                .collect(Collectors.toList());

        resolvedKeys = Lists.reverse(resolvedKeys);

        for (String key : resolvedKeys) {
            Config fromKey = config.getConfig(key);
            mergedConfig = mergedConfig.withFallback(fromKey);
        }

        return mergedConfig;
    }
}
