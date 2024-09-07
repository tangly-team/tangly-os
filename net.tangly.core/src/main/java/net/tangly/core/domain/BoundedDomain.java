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

import net.tangly.commons.logger.EventData;
import net.tangly.core.*;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * A bounded domain as defined in the DDD approach has a domain-specific model and a set of adapters.
 * A bounded domain is a cohesive and decoupled unit of functionality that is relevant to the business.
 * It has a unique name.
 * <p>The lifecycle of a bounded domain is:</p>
 * <ol>
 *     <li>Bounded domain creation. The creation sets the realm, logic, ports, tenant directory are set.</li>
 *     <li>Define codes and tags into the domain type registry. The port interface provides a method
 *     {@link Port#importConfiguration(DomainAudit, TypeRegistry)}</li>
 *     <li>Startup of the domain after construction</li>
 *     <li>Shutdown of the domain after startup</li>
 * </ol>
 *
 * <p>The tenant directory provides access to tenant configuration information.</p>
 * <p>The type registry is the source of codes and tags. These concepts support dynamic extension points into the domain.
 * Each tenant can defines code values and tags.</p>
 *
 * @param <R> realm handles all entities and values objects of the domain model
 * @param <B> business logic provides complex domain business logic functions
 * @param <P> port empowers the business domain to communicate with outer layers or external systems.
 *            The communication is generally asynchronous
 */
public class BoundedDomain<R extends Realm, B, P extends Port<R>> implements HasName, DomainAudit {
    private final String name;
    private final R realm;
    private final P port;
    private final B logic;
    private final boolean enabled;
    private final TenantDirectory directory;
    private final TypeRegistry registry;
    private final SubmissionPublisher<Object> channel;
    private final SubmissionPublisher<Object> internalChannel;
    private final List<EventData> auditEvents;

    /**
     * Defines a refined event listener interface to handle events in the domain or from another domain.
     *
     * @see Flow.Subscriber
     */
    @FunctionalInterface
    public interface EventListener extends Flow.Subscriber<Object> {
        @Override
        default void onComplete() {
        }

        @Override
        default void onError(Throwable throwable) {
        }

        @Override
        default void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        void onNext(Object event);
    }

    /**
     * Constructor of the bounded domain.
     *
     * @param name      human-readable name of the domain
     * @param realm     realm handles all entities and values objects of the domain model
     * @param logic     logic provides complex domain business logic functions
     * @param port      port empowers the business domain to communicate with external systems
     * @param directory directory of the tenant to support inter-domain communication
     */
    public BoundedDomain(@NotNull String name, @NotNull R realm, @NotNull B logic, @NotNull P port, TenantDirectory directory) {
        this.name = name;
        this.realm = realm;
        this.logic = logic;
        this.port = port;
        this.registry = new TypeRegistry();
        this.directory = directory;
        channel = new SubmissionPublisher<>(Executors.newVirtualThreadPerTaskExecutor(), Flow.defaultBufferSize());
        internalChannel = new SubmissionPublisher<>(Executors.newVirtualThreadPerTaskExecutor(), Flow.defaultBufferSize());
        auditEvents = new ArrayList<>();
        enabled = Boolean.valueOf(directory.getProperty("%s.enabled".formatted(name)));
    }

    protected static <I extends HasOid & HasMutableTags> void addTagCounts(@NotNull TypeRegistry registry, @NotNull Provider<I> provider,
                                                                           Map<TagType<?>, Integer> counts) {
        addTagCounts(registry, provider.items(), counts);
    }

    protected static <I extends HasTags> void addTagCounts(@NotNull TypeRegistry registry, @NotNull List<I> entities, Map<TagType<?>, Integer> counts) {
        entities.stream().flatMap(e -> e.tags().stream()).map(registry::find).flatMap(Optional::stream)
            .forEach(e -> counts.merge(e, 1, (oldValue, _) -> ++oldValue));
    }

    /**
     * Subscribes to the public event channel of the domain.
     *
     * @param listener event listener
     */
    public void subscribe(@NotNull EventListener listener) {
        channel.subscribe(listener);
    }

    /**
     * Subscribes to the internal event channel of the domain.
     *
     * @param listener event listener
     */
    public void subscribeInternally(@NotNull EventListener listener) {
        internalChannel.subscribe(listener);
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

    public TenantDirectory directory() {
        return directory;
    }

    public List<DomainEntity<?>> entities() {
        return Collections.emptyList();
    }

    public List<EventData> auditEvents() {
        return auditEvents;
    }

    public boolean enabled() {
        return enabled;
    }

    // region DomainAudit

    @Override
    public void log(@NotNull EventData auditEvent) {
        EventData.log(auditEvent);
        auditEvents.add(auditEvent);
    }

    @Override
    public void entityImported(@NotNull String entityName) {
        submitInterally(new EntityChangedInternalEvent(name(), entityName, Operation.ALL));
    }

    @Override
    public void submitInterally(@NotNull Object event) {
        internalChannel().submit(event);
    }

    @Override
    public void submit(@NotNull Object event) {
        channel().submit(event);
    }


    // endregion

    public SubmissionPublisher<Object> channel() {
        return channel;
    }

    public SubmissionPublisher<Object> internalChannel() {
        return internalChannel;
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
