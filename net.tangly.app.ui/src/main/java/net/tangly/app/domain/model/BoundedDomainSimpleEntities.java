/*
 * Copyright 2023-2023 Marcel Baumann
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

package net.tangly.app.domain.model;

import net.tangly.core.DateRange;
import net.tangly.core.Entity;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Port;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.LongStream;

/**
 * Define a test bed for the vaadin user interface components library. All CRUD operations are exercised to validate the CRUD grid component with details. Each domain entity is a
 * simple entity with an internal identifier, an external identifier, a name, a temporal validity interval and a textual description. Entities are defined as immutable record
 * instances.
 */
public class BoundedDomainSimpleEntities extends
    BoundedDomain<BoundedDomainSimpleEntities.AppRealm, BoundedDomainSimpleEntities.AppBusinessLogic, BoundedDomainSimpleEntities.AppPort> {
    public static final String DOMAIN = "Simple Entities";

    private BoundedDomainSimpleEntities(AppRealm realm, AppBusinessLogic logic, AppPort port) {
        super(DOMAIN, realm, logic, port, new TypeRegistry());
    }

    public static BoundedDomainSimpleEntities create() {
        AppRealm realm = new AppRealm();
        return new BoundedDomainSimpleEntities(realm, new AppBusinessLogic(), new AppPort(realm));
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, SimpleEntityOne.class, realm().oneEntities()), new DomainEntity<>(DOMAIN, simpleEntityTwo.class, realm().twoEntities()));
    }

    /**
     * Simple entity one demonstrates oid, id, name, time interval, text.
     */

    public record SimpleEntityOne(long oid, String id, String name, DateRange range, String text) implements Entity {
    }

    /**
     * Simple entity two demonstrates oid, id, name, time interval, text.
     */
    public record simpleEntityTwo(long oid, String id, String name, DateRange range, String text) implements Entity {
    }

    public static class AppRealm implements Realm {
        private final ProviderInMemory<SimpleEntityOne> providerOne;
        private final ProviderInMemory<simpleEntityTwo> providerTwo;

        public AppRealm() {
            providerOne = new ProviderInMemory<>();
            providerTwo = new ProviderInMemory<>();
            load();
        }

        public Provider<SimpleEntityOne> oneEntities() {
            return providerOne;
        }

        public Provider<simpleEntityTwo> twoEntities() {
            return providerTwo;
        }

        private void load() {
            LocalDate from = LocalDate.of(2000, Month.JANUARY, 1);
            LocalDate to = LocalDate.of(2000, Month.DECEMBER, 31);
            LongStream.rangeClosed(1, 100)
                .forEach(o -> oneEntities().update(new SimpleEntityOne(o, Long.toString(o), "Simple entity one-" + o, DateRange.of(from, to), "Simple entity _one_ text-" + o)));
            LongStream.rangeClosed(1, 100)
                .forEach(o -> twoEntities().update(new simpleEntityTwo(o, Long.toString(o), "simple entity two-" + o, DateRange.of(from, to), "Simple entity _two_ text-" + o)));
        }
    }

    public static class AppBusinessLogic {
    }

    public static class AppPort implements Port<AppRealm> {
        private final AppRealm realm;

        public AppPort(@NotNull AppRealm realm) {
            this.realm = realm;
        }

        public AppRealm realm() {
            return realm;
        }
    }
}
