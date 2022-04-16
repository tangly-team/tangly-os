/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * A bounded domain as defined in the DDD approach has a domain specific model and a set of adapters.
 *
 * @param <R> realm handles all entities and values objects of the domain model
 * @param <B> business logic provides complex domain business logic functions
 * @param <H> handler provides an interface to interact with the bounded domain from outer layers
 * @param <P> port empowers the business domain to communicate with outer layers or external systems
 */
public class BoundedDomain<R extends Realm, B, H extends Handler<?>, P> {
    private final String name;
    private final R realm;
    private final H handler;
    private final P port;
    private final B logic;
    private final transient TypeRegistry registry;

    public BoundedDomain(String name, R realm, B logic, H handler, P port, TypeRegistry registry) {
        this.name = name;
        this.realm = realm;
        this.logic = logic;
        this.handler = handler;
        this.port = port;
        this.registry = registry;
        initialize();
    }

    protected static <I extends HasOid & HasTags> void addTagCounts(@NotNull TypeRegistry registry, @NotNull Provider<I> provider, Map<TagType<?>, Integer> counts) {
        addTagCounts(registry, provider.items(), counts);
    }

    protected static <I extends HasTags> void addTagCounts(@NotNull TypeRegistry registry, @NotNull List<I> entities, Map<TagType<?>, Integer> counts) {
        entities.stream().flatMap(e -> e.tags().stream()).map(registry::find).flatMap(Optional::stream).forEach(e ->
            counts.merge(e, 1, (oldValue, _$) -> oldValue++));
    }

    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        return counts;
    }

    public R realm() {
        return realm;
    }

    public B logic() {
        return logic;
    }

    public H handler() {
        return handler;
    }

    public P port() {
        return port;
    }

    public TypeRegistry registry() {
        return registry;
    }

    public List<DomainEntity<?>> entities() {
        return Collections.emptyList();
    }

    protected void initialize() {
    }
}
