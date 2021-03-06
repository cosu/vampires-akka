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

package ro.cosu.vampires.server.values;


import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class AutoValueUtil<P, B> {
    private final static Set<String> IGNORE_METHODS = Sets.newHashSet("equals", "toString", "hashCode", "$jacocoInit");

    private final TypeToken<P> typeTokenPayload = new TypeToken<P>(getClass()) {
    };

    private final TypeToken<B> typeTokenBuilder = new TypeToken<B>(getClass()) {
    };


    public B builderFromPayload(P payload, B builder) {
        Arrays.stream(typeTokenPayload.getRawType().getDeclaredMethods())
                // we filter out the methods that seem to be builders
                .filter(method -> !method.getReturnType().getName().toLowerCase().contains("builder"))
                // also filter any constructor helpers - they return the same type as the object
                .filter(method -> !method.getReturnType().equals(typeTokenPayload.getRawType()))
                .filter(method -> !IGNORE_METHODS.contains(method.getName()))
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
