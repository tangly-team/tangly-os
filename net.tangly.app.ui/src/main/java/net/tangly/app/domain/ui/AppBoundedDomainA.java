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

import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
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
import java.time.Month;
import java.util.List;
import java.util.stream.LongStream;

/**
 * Defines a test bed for the vaadin user interface components library. All CRUD operations are exercised to validate the CRUD grid component with details.
 * Entities are defined as immutable record instances.
 */
public class AppBoundedDomainA extends BoundedDomain<AppBoundedDomainA.AppRealm, AppBoundedDomainA.AppBusinessLogic, AppBoundedDomainA.AppHandler, AppBoundedDomainA.AppPort> {
    public static final String DOMAIN = "App-A";

    public record EntityOne(long oid, String id, String name, LocalDate from, LocalDate to, String text) implements HasOid, HasId, HasName, HasText, HasTimeInterval {
    }

    public record EntityTwo(long oid, String id, String name, LocalDate from, LocalDate to, String text) implements HasOid, HasId, HasName, HasText, HasTimeInterval {
    }

    public static class AppRealm implements Realm {
        private ProviderInMemory<EntityOne> providerOne;
        private ProviderInMemory<EntityTwo> providerTwo;

        public AppRealm() {
            providerOne = new ProviderInMemory<>();
            providerTwo = new ProviderInMemory<>();
            load();
        }

        public Provider<EntityOne> oneEntities() {
            return providerOne;
        }

        public Provider<EntityTwo> twoEntities() {
            return providerTwo;
        }

        private void load() {
            LocalDate from = LocalDate.of(2000, Month.JANUARY, 01);
            LocalDate to = LocalDate.of(2000, Month.DECEMBER, 31);
            LongStream.rangeClosed(1, 100).forEach(o -> oneEntities().update(new EntityOne(o, Long.toString(o), "entity one-" + o, from, to, "entity one text-" + o)));
            LongStream.rangeClosed(1, 100).forEach(o -> twoEntities().update(new EntityTwo(o, Long.toString(o), "entity two-" + o, from, to, "entity two text-" + o)));
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

    public static AppBoundedDomainA create() {
        AppRealm realm = new AppRealm();
        return new AppBoundedDomainA(realm, new AppBusinessLogic(), new AppHandler(realm), new AppPort(), new TypeRegistry());
    }

    private AppBoundedDomainA(AppRealm realm, AppBusinessLogic logic, AppHandler handler, AppPort port, TypeRegistry registry) {
        super(DOMAIN, realm, logic, handler, port, registry);
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, EntityOne.class, realm().oneEntities()), new DomainEntity<>(DOMAIN, EntityTwo.class, realm().twoEntities()));
    }
}
