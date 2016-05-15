package ro.cosu.vampires.server.workload;


import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class AutoValueUtil<P, B> {
    private final TypeToken<P> typeTokenPayload = new TypeToken<P>(getClass()) {
    };
    private final TypeToken<B> typeTokenBuilder = new TypeToken<B>(getClass()) {
    };


    B builderFromPayload(P payload, B builder) {

        Set<String> forbiddenMethods = Sets.newHashSet("equals", "toString", "hashCode", "$jacocoInit");
        Arrays.stream(typeTokenPayload.getRawType().getDeclaredMethods())
                // we filter out the methods that seem to be builders
                .filter(method -> !method.getReturnType().getName().toLowerCase().contains("builder"))
                // also filter any constructor helpers - they return the same type as the object
                .filter(method -> !method.getReturnType().equals(typeTokenPayload.getRawType()))
                .filter(method -> !forbiddenMethods.contains(method.getName()))
                .forEach(payloadMethod -> {
                    try {
                        Method builderMethod = typeTokenBuilder.getRawType()
                                .getMethod(payloadMethod.getName(), payloadMethod.getReturnType());

                        Object invokeResult = payloadMethod.invoke(payload);

                        if (invokeResult != null) {
                            builderMethod.invoke(builder, invokeResult);
                        }

                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new IllegalArgumentException(e);
                    }
                });
        return builder;
    }
}
