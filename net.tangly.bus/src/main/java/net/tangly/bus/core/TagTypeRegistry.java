/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.bus.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * Registry of tag types defined for a domain model.
 */
public class TagTypeRegistry {
    /**
     * registered tag types in the registry.
     */
    private final Set<TagType<?>> types;

    public TagTypeRegistry() {
        types = new HashSet<>();
    }

    /**
     * Register a tag type.
     *
     * @param type tag type to bind
     */
    public void register(@NotNull TagType<?> type) {
        types.add(type);
    }

    public Collection<TagType<?>> tagTypes() {
        return Collections.unmodifiableCollection(types);
    }

    /**
     * Returns all the namespaces registered.
     *
     * @return List of namespaces
     */
    public List<String> namespaces() {
        return types.stream().map(TagType::namespace).distinct().collect(Collectors.toUnmodifiableList());
    }

    public List<String> tagNamesForNamespace(@NotNull String namespace) {
        return types.stream().filter(o -> Objects.equals(o.namespace(), namespace)).map(TagType::name).distinct().collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns the tag type describing the requested namespace and name.
     *
     * @param namespace optional namespace of the tag type
     * @param name      mandatory name of the tag type
     * @return requested tag type
     */
    public Optional<TagType<?>> find(String namespace, @NotNull String name) {
        return types.stream().filter(o -> Objects.equals(o.namespace(), namespace) && Objects.equals(o.name(), name)).findAny();
    }

    /**
     * Returns the tag type describing the tag.
     *
     * @param tag tag which type is requested
     * @return requested tag type
     * @see #find(String, String)
     */
    public Optional<TagType<?>> find(@NotNull Tag tag) {
        return find(tag.namespace(), tag.name());
    }
}

