/*
 * Copyright 2006-2019 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.di.imp;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class SupplierConstructor<T> implements Supplier<T> {
    private final Class<T> clazz;
    private final Constructor<T> constructor;
    private final List<Supplier<?>> parameters;

    public SupplierConstructor(Class<T> clazz, Constructor<T> constructor, List<Supplier<?>> parameters) {
        this.clazz = clazz;
        this.constructor = constructor;
        this.parameters = new ArrayList<>(parameters);
    }

    @Override
    public T get() {
        final List<Object> arguments = parameters.stream().map(Supplier::get).collect(Collectors.toList());
        try {
            return constructor.newInstance(arguments.toArray());
        } catch (Exception e) {
            throw new CompletionException(
                    "Injector can't create an instance of the class [" + clazz + "]. " + "An Exception was thrown during the " + "instantiation.", e);
        }
    }

    /**
     * Returns the class of the instantiated objects.
     *
     * @return class of the instantiated objects
     */
    public Class<T> clazz() {
        return clazz;
    }
}
