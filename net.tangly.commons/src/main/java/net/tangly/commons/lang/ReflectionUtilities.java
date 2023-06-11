/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.lang;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Utility cass to manipulate objects through reflection.
 */
public final class ReflectionUtilities {
    /**
     * Private constructor for a utility class.
     */
    private ReflectionUtilities() {
    }

    public static <T> void set(@NotNull T entity, @NotNull String name, @NotNull Object value) {
        set(entity, findField(entity.getClass(), name).orElseThrow(
            () -> new NoSuchElementException("Missing Field From " + entity.getClass().getSimpleName() + " field " + name)), value);
    }

    public static <T> void set(@NotNull T entity, @NotNull Field field, @NotNull Object value) {
        field.setAccessible(true);
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static <T> Object get(@NotNull T entity, @NotNull String name) {
        return get(entity, findField(entity.getClass(), name).orElseThrow());
    }

    public static <T> Object get(@NotNull T entity, @NotNull Field field) {
        field.setAccessible(true);
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Finds the field with the given name.
     *
     * @param clazz class owning the field to retrieve
     * @param name  of the field to be found
     * @param <T>   type of the class owning the field
     * @return the requested field if found otherwise null
     */
    public static <T> Optional<Field> findField(@NotNull Class<T> clazz, @NotNull String name) {
        Optional<Field> field = Optional.empty();
        Class<?> pointer = clazz;
        while ((pointer != null) && (field.isEmpty())) {
            field = Arrays.stream(pointer.getDeclaredFields()).filter(o -> name.equals(o.getName())).findAny();
            pointer = pointer.getSuperclass();
        }
        return field;
    }
}
