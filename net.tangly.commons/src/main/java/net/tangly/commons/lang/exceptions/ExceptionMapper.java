/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.lang.exceptions;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * The exception mapper processes an exception and executes associated instructions with the helper. For example a HTTP error code and answer can be generated
 * based on the kind of the exception.
 *
 * @param <H> handler used to process the exception
 */
public class ExceptionMapper<H> {
    private final Map<Class<? extends Exception>, BiConsumer<H, Exception>> functors;

    public ExceptionMapper() {
        functors = new HashMap<>();
        functors.put(Exception.class, (h, e) -> {
        });
    }

    /**
     * Register the class exception with the associated consumer.
     *
     * @param clazz   class of the exception top bind
     * @param functor consumer used to process the exception
     */
    public void register(@NotNull Class<? extends Exception> clazz, @NotNull BiConsumer<H, Exception> functor) {
        functors.put(clazz, functor);
    }

    /**
     * The class exception cannot be unregister to insure we are always handling any exception. If specific behavior is requested you can overwrite it using
     * bind method.
     *
     * @param clazz class instance to unregister
     */
    public void unregister(Class<? extends Exception> clazz) {
        if (clazz != Exception.class) {
            functors.remove(clazz);
        }
    }

    /**
     * Returns true if the exception class is registered.
     *
     * @param clazz class which registration shall be checked
     * @return true if the class is registered otherwise false
     */
    public boolean isRegistered(Class<? extends Exception> clazz) {
        return functors.containsKey(clazz);
    }

    /**
     * Processes the exception by executing the associated functor. The functor is associated with the class of the exception. Generalization of the exception
     * class for handling is similar to the rules of the try catch instruction.
     *
     * @param handler   handler used to process the exception
     * @param exception exception to be processed
     */
    public void process(@NotNull H handler, @NotNull Exception exception) {
        Class<?> clazz = exception.getClass();
        while (!functors.containsKey(clazz) && (clazz != Exception.class)) {
            clazz = clazz.getSuperclass();
        }
        functors.get(clazz).accept(handler, exception);
    }
}
