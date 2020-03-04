/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.commons.di;

import net.tangly.commons.di.imp.InjectorImp;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.util.Arrays;

/**
 * Defines the interface for the dependency injector. The injector supports solely constructors based dependency injection. Annotations based on JSR
 * 330 provide declarative definition of constructor injection and singleton scoping. A provider can also be registered instead of using a dedicated
 * constructor.
 */
public interface Injector {
    /**
     * Creates an injector instances and configure it with the module instructions.
     *
     * @param modules describe the configuration instructions. Often a module is defined for each subsystem of the application
     * @return injector instance
     */
    static Injector create(Module... modules) {
        Injector injector = new InjectorImp();
        Arrays.stream(modules).forEachOrdered(o -> o.configure(injector));
        return injector;
    }

    /**
     * Defines what implementing class should be used for a given interface or abstract class. This way you can use interface types as dependencies in
     * your classes and doesn't have to depend on specific implementations. But Injector needs to know what implementing class should be used when an
     * interface type is defined as dependency. Alternatively to this method you can use the {@link #bindSingleton(Class, Object)} method to define an
     * instance of the interface that is used - use the {@link #bindProvider(Class, Provider)} method to define a provider for this interface.
     *
     * @param interfaces class type of the interface
     * @param clazz      class type of the implementing class
     * @param <R>        type of the interface to bind
     * @param <T>        type of the class bind to the interface
     */
    <R, T extends R> void bind(@NotNull Class<R> interfaces, @NotNull Class<T> clazz);

    /**
     * Defines a trivial binding to the class itself.
     *
     * @param clazz class type of the implemented and implementing class
     * @param <T>   generic type of the interface
     */
    default <T> void bind(@NotNull Class<T> clazz) {
        bind(clazz, clazz);
    }

    /**
     * Defines a {@link javax.inject.Provider} for a given type. The type can either be an interface or class type. This is a good way to integrate
     * third-party classes that aren't suitable for injection by default. Another use-case is when you need to make some configuration for new
     * instance before it is used for dependency injection. Providers can be combined with {@link javax.inject.Singleton}'s. When a type is marked as
     * instance (has the annotation {@link javax.inject.Singleton}) and there is a provider defined for this type, then this provider will only be
     * executed exactly one time when the type is requested the first time.
     *
     * @param clazz    type of the class for which the provider is used
     * @param provider provider that will be called to get an instance of the given type
     * @param <T>      generic type of the class/interface
     */
    <T> void bindProvider(@NotNull Class<T> clazz, @NotNull Provider<T> provider);

    /**
     * Defines an instance that is used every time the given class type is requested. This way the given instance is effectively a singleton. This
     * method can also be used to define instances for interfaces or abstract classes that otherwise couldn't be instantiated without further
     * configuration.
     *
     * @param clazz    class type for that the instance will be bound
     * @param instance instance that will be bound
     * @param <T>      generic type of the class
     */
    <T> void bindSingleton(@NotNull Class<T> clazz, @NotNull T instance);

    /**
     * Defines that at most one instance that is used every time the given class type is requested. This way the given instance is effectively a
     * singleton. This method can also be used to define instances for interfaces or abstract classes that otherwise couldn't be instantiated without
     * further configuration.
     *
     * @param clazz class type for that the instance will be bound
     * @param <T>   generic type of the class
     */
    <T> void bindSingleton(@NotNull Class<T> clazz);

    /**
     * Instantiates an instance compatible with the given class.
     *
     * @param clazz compatible class of the requested instance
     * @param <T>   generic type of the class
     * @return instance compatible with the given clsass
     */
    public <T> T instance(@NotNull Class<T> clazz);
}
