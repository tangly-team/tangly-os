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

import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasText;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Handler;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Defines a test bed for the vaadin user interface components library.
 * All CRUD operations are exercised to validate the CRUD grid component with details.
 */
public class AppBoundedDomainA extends BoundedDomain<AppBoundedDomainA.AppRealm, AppBoundedDomainA.AppBusinessLogic, AppBoundedDomainA.AppHandler, AppBoundedDomainA.AppPort> {
    public static final String DOMAIN = "App-A";

    public record EntityOne(String id, String name, String text) implements HasId, HasName, HasText {
    }

    public record EntityTwo(String id, String name, String text) implements HasId, HasName, HasText {
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
            IntStream.rangeClosed(1, 100).forEach(o -> oneEntities().update(
                new EntityOne(Integer.toString(o), "entity one-" + o, "entity one text-" + o)));
            IntStream.rangeClosed(1, 100).forEach(o -> twoEntities().update(
                new EntityTwo(Integer.toString(o), "entity two-" + o, "entity one text-" + o)));
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
