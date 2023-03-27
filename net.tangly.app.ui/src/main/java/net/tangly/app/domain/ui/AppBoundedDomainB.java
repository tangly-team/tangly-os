/*
 * Copyright 2023-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.app.domain.ui;

import net.tangly.core.*;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Handler;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Defines a test bed for the vaadin user interface components library.
 * The entity classes implement the whole entity interfaces to validate the visualization components.
 * One2one and One2Many relations are also tested.
 * <p>Read-only mode is also excercised for exhaustive validateion of the user interface library.</p>
 */
public class AppBoundedDomainB extends BoundedDomain<AppBoundedDomainB.AppRealm, AppBoundedDomainB.AppBusinessLogic, AppBoundedDomainB.AppHandler, AppBoundedDomainB.AppPort> {
    public static final String DOMAIN = "App-B";

    public static class EntityThree implements HasOid, HasId, HasName, HasText, HasComments, HasTags, HasTimeInterval, Entity {
        private final long oid;
        private final String id;
        private final String name;
        private final String text;
        private final LocalDate from;
        private final LocalDate to;
        private final List<Comment> comments;
        private final Set<Tag> tags;

        public EntityThree(long oid, String id, String name, String text, LocalDate from, LocalDate to) {
            this.oid = oid;
            this.id = id;
            this.name = name;
            this.text = text;
            this.from = from;
            this.to = to;
            comments = new ArrayList<>();
            tags = new HashSet<>();
        }

        @Override
        public long oid() {
            return oid;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String text() {
            return text;
        }

        @Override
        public List<Comment> comments() {
            return Collections.unmodifiableList(comments);
        }

        @Override
        public void add(@NotNull Comment comment) {
            comments.add(comment);
        }

        @Override
        public void remove(@NotNull Comment comment) {
            comments.remove(comment);
        }

        @Override
        public Set<Tag> tags() {
            return Collections.unmodifiableSet(tags);
        }

        @Override
        public boolean add(Tag tag) {
            return tags.add(tag);
        }

        @Override
        public void clearTags() {
            tags.clear();
        }

        @Override
        public boolean remove(Tag tag) {
            return tags.remove(tag);
        }

        @Override
        public LocalDate from() {
            return from;
        }

        @Override
        public LocalDate to() {
            return to;
        }

    }

    public static class EntityFour implements HasOid, HasId, HasName, HasText, HasComments, HasTags, HasTimeInterval, Entity {
        private final long oid;
        private final String id;
        private final String name;
        private final String text;
        private final LocalDate from;
        private final LocalDate to;
        private final List<Comment> comments;
        private final Set<Tag> tags;

        public EntityFour(long oid, String id, String name, String text, LocalDate from, LocalDate to) {
            this.oid = oid;
            this.id = id;
            this.name = name;
            this.text = text;
            this.from = from;
            this.to = to;
            comments = new ArrayList<>();
            tags = new HashSet<>();
        }

        @Override
        public long oid() {
            return oid;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String text() {
            return text;
        }

        @Override
        public List<Comment> comments() {
            return Collections.unmodifiableList(comments);
        }

        @Override
        public void add(@NotNull Comment comment) {
            comments.add(comment);
        }

        @Override
        public void remove(@NotNull Comment comment) {
            comments.remove(comment);
        }

        @Override
        public Set<Tag> tags() {
            return Collections.unmodifiableSet(tags);
        }

        @Override
        public boolean add(Tag tag) {
            return tags.add(tag);
        }

        @Override
        public void clearTags() {
            tags.clear();
        }

        @Override
        public boolean remove(Tag tag) {
            return tags.remove(tag);
        }

        @Override
        public LocalDate from() {
            return from;
        }

        @Override
        public LocalDate to() {
            return to;
        }
    }

    public static class AppRealm implements Realm {
        private ProviderInMemory<EntityThree> providerOne;
        private ProviderInMemory<EntityFour> providerTwo;

        public AppRealm() {
            providerOne = new ProviderInMemory<>();
            providerTwo = new ProviderInMemory<>();
            load();
        }

        public Provider<EntityThree> oneEntities() {
            return providerOne;
        }

        public Provider<EntityFour> twoEntities() {
            return providerTwo;
        }

        private void load() {
            IntStream.rangeClosed(1, 100).forEach(o -> {
                var entity = new EntityThree(o, Integer.toString(o), "entity three-" + o, "entity three text-" + o,
                    LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2000, Month.DECEMBER, 31));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 12),
                    "John Doee", "Comment for entity three " + o));
                oneEntities().update(entity);
            });
            IntStream.rangeClosed(1, 100).forEach(o -> {
                var entity = new EntityFour(o, Integer.toString(o), "entity four-" + o, "entity four text-" + o,
                    LocalDate.of(2010, Month.JANUARY, 1), LocalDate.of(2010, Month.DECEMBER, 31));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18),
                    "John Doe", "Comment for entity four " + o));
                twoEntities().update(entity);
            });
        }
    }

    public static class AppBusinessLogic {}

    public static class AppHandler implements Handler<AppRealm> {
        private final AppRealm realm;

        public AppHandler(AppRealm realm) {
            this.realm = realm;
        }

        public AppRealm realm() {
            return realm;
        }
    }

    public static class AppPort {}

    public static AppBoundedDomainB create() {
        AppRealm realm = new AppRealm();
        return new AppBoundedDomainB(realm, new AppBusinessLogic(), new AppHandler(realm), new AppPort(), new TypeRegistry());
    }

    private AppBoundedDomainB(AppRealm realm, AppBusinessLogic logic, AppHandler handler, AppPort port, TypeRegistry registry) {
        super(DOMAIN, realm, logic, handler, port, registry);
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, EntityThree.class, realm().oneEntities()), new DomainEntity<>(DOMAIN, EntityFour.class, realm().twoEntities()));
    }
}
