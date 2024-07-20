/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core;

import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Registry of tag types defined for a domain model.
 */
public class TypeRegistry {
    private final Set<TagType<?>> tagTypes;
    private final Set<CodeType<?>> codeTypes;

    public TypeRegistry() {
        tagTypes = new HashSet<>();
        codeTypes = new HashSet<>();
    }

    /**
     * Registers a tag type.
     *
     * @param type tag type to bind
     */
    public void register(@NotNull TagType<?> type) {
        tagTypes.add(type);
    }

    public Collection<TagType<?>> tagTypes() {
        return Collections.unmodifiableCollection(tagTypes);
    }

    /**
     * Returns all the namespaces registered.
     *
     * @return List of namespaces
     */
    public List<String> namespaces() {
        return tagTypes.stream().map(TagType::namespace).distinct().toList();
    }

    public List<String> tagNamesForNamespace(@NotNull String namespace) {
        return tagTypes.stream().filter(o -> Objects.equals(o.namespace(), namespace)).map(TagType::name).distinct().toList();
    }

    /**
     * Returns the tag type describing the requested namespace and name.
     *
     * @param namespace optional namespace of the tag type
     * @param name      mandatory name of the tag type
     * @param <T>       type of the code
     * @return requested tag type as optional
     */
    public <T> Optional<TagType<T>> find(String namespace, @NotNull String name) {
        Optional<TagType<?>> optional = tagTypes.stream().filter(o -> Objects.equals(o.namespace(), namespace) && Objects.equals(o.name(), name)).findAny();
        return optional.map(tagType -> (TagType<T>) tagType);
    }

    /**
     * Returns the tag type describing the tag.
     *
     * @param tag tag which type is requested
     * @param <T> type of the code
     * @return requested tag type
     * @see #find(String, String)
     */
    public <T> Optional<TagType<T>> find(@NotNull Tag tag) {
        return find(tag.namespace(), Objects.requireNonNull(tag).name());
    }

    /**
     * Registers a code type to the registry.
     *
     * @param type code type to register
     * @param <T>  type of the code
     */
    public <T extends Code> void register(@NotNull CodeType<T> type) {
        codeTypes.add(type);
    }

    /**
     * Returns the code type for the provided code class. The implementation takes care to return the expected generic type.
     *
     * @param clazz class of the code which code type we are looking for
     * @param <T>   type of code to look for
     * @return code type as optional
     */
    public <T extends Code> Optional<CodeType<T>> find(@NotNull Class<T> clazz) {
        Optional<CodeType<?>> optional = codeTypes.stream().filter(o -> clazz.equals(o.clazz())).findAny();
        return optional.map(codeType -> (CodeType<T>) codeType);
    }
}
