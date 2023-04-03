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
 *
 */

package net.tangly.app.domain.ui;

import net.tangly.core.Comment;
import net.tangly.core.Entity;
import net.tangly.core.EntityImp;
import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Handler;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Defines a test bed for the vaadin user interface components library. The entity classes implement the whole entity interfaces to validate the visualization components. One2one and One2Many
 * relations are also tested.
 * <p>Read-only mode is also exercised for exhaustive validation of the user interface library.</p>
 */
public class AppBoundedDomainB extends BoundedDomain<AppBoundedDomainB.AppRealm, AppBoundedDomainB.AppBusinessLogic, AppBoundedDomainB.AppHandler, AppBoundedDomainB.AppPort> {
    public static final String DOMAIN = "App-B";

    public static class EntityThree extends EntityImp implements HasOid, HasId, HasName, HasTimeInterval, HasText, HasComments, HasTags, Entity {
        public EntityThree(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
            super(oid, id, name, from, to, text);
        }
    }

    public static class EntityFour extends EntityImp implements HasOid, HasId, HasName, HasTimeInterval, HasText, HasComments, HasTags, Entity {
        public EntityFour(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
            super(oid, id, name, from, to, text);
        }
    }

    public static class AppRealm implements Realm {
        private ProviderInMemory<EntityThree> providerThree;
        private ProviderInMemory<EntityFour> providerFour;

        public AppRealm() {
            providerThree = new ProviderInMemory<>();
            providerFour = new ProviderInMemory<>();
            load();
        }

        public Provider<EntityThree> threeEntities() {
            return providerThree;
        }

        public Provider<EntityFour> fourEntities() {
            return providerFour;
        }

        private void load() {
            IntStream.rangeClosed(1, 100).forEach(o -> {
                var entity = new EntityThree(o, Integer.toString(o), "entity three-" + o, LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2000, Month.DECEMBER, 31), "entity three text-" + o);
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 12), "John Doee", "First comment for entity three " + o));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 15), "John Doee", "Second comment for entity three " + o));
                threeEntities().update(entity);
            });
            IntStream.rangeClosed(1, 100).forEach(o -> {
                var entity = new EntityFour(o, Integer.toString(o), "entity four-" + o, LocalDate.of(2010, Month.JANUARY, 1), LocalDate.of(2010, Month.DECEMBER, 31), "entity four text-" + o);
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18), "John Doe", "First comment for entity four " + o));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18), "John Doe", "Second comment for entity four " + o));
                fourEntities().update(entity);
            });
        }
    }

    public static class AppBusinessLogic {
    }

    public static class AppHandler implements Handler<AppRealm> {
        private final AppRealm realm;

        public AppHandler(AppRealm realm) {
            this.realm = realm;
        }

        public AppRealm realm() {
            return realm;
        }
    }

    public static class AppPort {
    }

    public static AppBoundedDomainB create() {
        AppRealm realm = new AppRealm();
        return new AppBoundedDomainB(realm, new AppBusinessLogic(), new AppHandler(realm), new AppPort(), new TypeRegistry());
    }

    private AppBoundedDomainB(AppRealm realm, AppBusinessLogic logic, AppHandler handler, AppPort port, TypeRegistry registry) {
        super(DOMAIN, realm, logic, handler, port, registry);
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, EntityThree.class, realm().threeEntities()), new DomainEntity<>(DOMAIN, EntityFour.class, realm().fourEntities()));
    }
}
