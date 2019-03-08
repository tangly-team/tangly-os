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

import net.tangly.commons.di.Injector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Injector main class.
 *
 * @implNote For each class with a supplier responsible to create instances of the class. A supplier can provides singleton, JSR330 Provider, or a
 * constructor.
 */
public class InjectorImp implements Injector {
    private Map<Class<?>, Supplier<?>> creators;

    public InjectorImp() {
        creators = new HashMap<>();
    }

    @Override
    public <R, T extends R> void bind(Class<R> interfaces, Class<T> clazz) {
        creators.put(interfaces,
                wrapSingleton(interfaces, new SupplierBinding<R, T>(interfaces, clazz, wrapSingleton(clazz, createConstructor(clazz)))));
    }

    @Override
    public <T> void bindProvider(Class<T> clazz, Provider<T> provider) {
        creators.put(clazz, wrapSingleton(clazz, new SupplierProvider<T>(clazz, provider)));
    }

    @Override
    public <T> void bindSingleton(Class<T> clazz, T instance) {
        creators.put(clazz, new SupplierSingleton<T>(clazz, instance));
    }

    @Override
    public <T> void bindSingleton(Class<T> clazz) {
        creators.put(clazz, new SupplierSingleton<T>(clazz, createConstructor(clazz)));
    }

    @Override
    public <T> T instance(Class<T> clazz) {
        if (!creators.containsKey(clazz)) {
            bind(clazz, clazz);
        }
        return (T) creators.get(clazz).get();
    }

    private <T> Supplier<T> createConstructor(Class<T> clazz) {
        if (creators.containsKey(clazz)) {
            return (Supplier<T>) creators.get(clazz);
        }
        if (isAbstract(clazz)) {
            throw new IllegalArgumentException("The target class shall be a concrete subtype of the source class");
        }
        Constructor<T> constructor = findConstructor(clazz);
        Parameter[] parameters = constructor.getParameters();
        List<Supplier<?>> suppliers = Arrays.stream(parameters).map(Parameter::getType).map(this::createConstructor).collect(Collectors.toList());
        return new SupplierConstructor<>(clazz, constructor, suppliers);
    }

    <T> Supplier<T> createSingleton(Class<T> clazz) {
        Supplier<T> supplier = new SupplierSingleton<>(clazz, createConstructor(clazz));
        if (isSingleton(clazz)) {
            supplier = new SupplierSingleton<>(clazz, supplier);
        }
        return supplier;
    }

    <T> Supplier<T> wrapSingleton(Class<T> clazz, Supplier<T> supplier) {
        if (isSingleton(clazz)) {
            return new SupplierSingleton<>(clazz, supplier);
        } else {
            return supplier;
        }
    }

    /**
     * Find out the constructor that will be used for instantiation. If there is only one public constructor, it will be used. If there are more then
     * one public constructors, the one with an {@link javax.inject.Inject} annotation is used.
     *
     * @param type the class of which the constructor is searched for.
     * @param <T>  the generic type of the class.
     * @return the constructor to use
     * @throws java.lang.IllegalStateException when no constructor can be found.
     */
    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findConstructor(Class<T> type) {
        final Constructor<?>[] constructors = type.getConstructors();
        if (constructors.length == 0) {
            throw new IllegalArgumentException(
                    "Injector can't create an instance of the class [" + type + "]. " + "The class has no public constructor.");
        } else if (constructors.length > 1) {
            List<Constructor<?>> constructorsWithInject =
                    Arrays.stream(constructors).filter(c -> c.isAnnotationPresent(Inject.class)).collect(Collectors.toList());
            if (constructorsWithInject.isEmpty()) {
                throw new IllegalArgumentException("Injector can't create an instance of the class [" + type + "]. " +
                        "There is more than one public constructor defined so I don't know which one to use. " +
                        "Fix this by either make only one constructor public " +
                        "or annotate exactly one constructor with the javax.inject.Inject annotation.");
            }
            if (constructorsWithInject.size() != 1) {
                throw new IllegalArgumentException("Injector can't create an instance of the class [" + type + "]. " +
                        "There is more than one public constructor marked with @Inject so I don't know which one to use. " +
                        "Fix this by either make only one constructor public " +
                        "or annotate exactly one constructor with the javax.inject.Inject annotation.");
            }
            return (Constructor<T>) constructorsWithInject.get(0);
        } else {
            return (Constructor<T>) constructors[0];
        }
    }

    /**
     * Check if the given class type is marked as singleton.
     */
    private boolean isSingleton(Class<?> clazz) {
        return clazz.isAnnotationPresent(Singleton.class);
    }

    private boolean isAbstract(Class<?> clazz) {
        return clazz.isInterface() || (Modifier.isAbstract(clazz.getModifiers()));
    }

}