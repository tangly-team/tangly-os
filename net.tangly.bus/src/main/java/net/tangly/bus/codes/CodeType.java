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

package net.tangly.bus.codes;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The code type describes a reference code table entity and all the existing code values
 *
 * @param <T> reference code type
 */
public class CodeType<T extends Code> {
    private final Class<T> clazz;
    private final List<T> codes;

    /**
     * Builder for a reference code implemented as an enumeration. Provides a simple approach to map any enumeration type into a reference code.
     *
     * @param clazz class of the reference code table enumeration
     * @param <E>   reference code type
     * @return code type of the reference code
     */
    public static <E extends Enum<E> & Code> CodeType<E> of(Class<E> clazz) {
        return new CodeType<>(clazz, Arrays.asList(clazz.getEnumConstants()));
    }

    /**
     * Builder for a reference code implemented as a Java class. Provides a pragmatic approach to map a class and a set of instances into a reference
     * code.
     *
     * @param clazz class of the reference code table
     * @param codes the set of code instances associated with the code class
     * @param <E>   reference code type
     * @return code type of the reference code
     */
    public static <E extends Code> CodeType<E> of(Class<E> clazz, List<E> codes) {
        return new CodeType<>(clazz, codes);
    }

    /**
     * Constructor of the class representing the type of the reference code and all existing instances.
     *
     * @param clazz class of the reference code table
     * @param codes list of all reference code table values
     */
    public CodeType(@NotNull Class<T> clazz, @NotNull List<T> codes) {
        this.clazz = clazz;
        this.codes = List.copyOf(codes);
    }

    /**
     * Returns the class of the reference code.
     *
     * @return class of the reference code
     */
    public Class<T> clazz() {
        return this.clazz;
    }

    /**
     * Returns all codes defined for the code table.
     *
     * @return list of codes of the code table
     */
    public List<T> codes() {
        return Collections.unmodifiableList(codes);
    }

    /**
     * Returns all active codes defined for the code table.
     *
     * @return list of active codes of the code table
     */
    public List<T> activeCodes() {
        return codes().stream().filter(Code::isEnabled).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns all inactive codes defined for the code table.
     *
     * @return list of inactive codes of the code table
     */

    public List<T> inactiveCodes() {
        return codes().stream().filter(item -> !item.isEnabled()).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Find the code with the given identifier.
     *
     * @param id identifier of the requested code
     * @return the requested code or empty optional
     */
    public Optional<T> findCode(int id) {
        return codes().stream().filter(item -> id == item.id()).findAny();
    }

    /**
     * Find the first code with the given identifier.
     *
     * @param code code of the requested code
     * @return the requested code or empty optional
     */
    public Optional<T> findCode(String code) {
        return codes().stream().filter(item -> code.equals(item.code())).findAny();
    }
}
