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

package net.tangly.core.domain;

import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A bounded domain as defined in the DDD approach has a domain-specific model and a set of adapters.
 * A bounded domain is a cohesive and decoupled unit of functionality that is relevant to the business.
 * It has a unique name.
 * <p>The lifecycle of a bounded domain is:</p>
 * <ol>
 *     <li>Bounded domain creation</li>
 *     <li>Startup of the domain after construction</li>
 *     <li>Shutdown of the domain after startup</li>
 * </ol>
 *
 * @param <R> realm handles all entities and values objects of the domain model
 * @param <B> business logic provides complex domain business logic functions
 * @param <P> port empowers the business domain to communicate with outer layers or external systems.
 *            The communication is generally asynchronous
 */
public class BoundedDomain<R extends Realm, B, P extends Port<R>> {
    private final String name;
    private final R realm;
    private final P port;
    private final B logic;
    private final transient TypeRegistry registry;

    /**
     * Constructor of the bounded domain.
     *
     * @param name     human-readable name of the domain
     * @param realm    realm handles all entities and values objects of the domain model
     * @param logic    logic provides complex domain business logic functions
     * @param port     port empowers the business domain to communicate with external systems
     * @param registry registry where the tagged values and code types defined for the domain model are registered
     */
    public BoundedDomain(@NotNull String name, @NotNull R realm, @NotNull B logic, @NotNull P port, TypeRegistry registry) {
        this.name = name;
        this.realm = realm;
        this.logic = logic;
        this.port = port;
        this.registry = registry;
    }

    protected static <I extends HasOid & HasTags> void addTagCounts(@NotNull TypeRegistry registry, @NotNull Provider<I> provider, Map<TagType<?>, Integer> counts) {
        addTagCounts(registry, provider.items(), counts);
    }

    protected static <I extends HasTags> void addTagCounts(@NotNull TypeRegistry registry, @NotNull List<I> entities, Map<TagType<?>, Integer> counts) {
        entities.stream().flatMap(e -> e.tags().stream()).map(registry::find).flatMap(Optional::stream).forEach(e -> counts.merge(e, 1, (oldValue, _) -> oldValue++));
    }

    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        return counts;
    }

    public String name() {
        return name;
    }

    public R realm() {
        return realm;
    }

    public B logic() {
        return logic;
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

    public void startup() {
    }

    public void shutdown() {
        try {
            realm.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
