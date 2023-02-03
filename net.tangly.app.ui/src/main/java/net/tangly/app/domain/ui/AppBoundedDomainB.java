/*
 * Copyright 2023 Marcel Baumann
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

public class AppBoundedDomainB extends BoundedDomain<AppBoundedDomainB.AppRealm, AppBoundedDomainB.AppBusinessLogic, AppBoundedDomainB.AppHandler, AppBoundedDomainB.AppPort> {
    public static final String DOMAIN = "App-B";

    public record EntityThree(String id, String name, String text) implements HasId, HasName, HasText {
    }

    public record EntityFour(String id, String name, String text) implements HasId, HasName, HasText {
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
            IntStream.rangeClosed(1, 100).forEach(o -> oneEntities().update(
                new EntityThree(Integer.toString(o), "entity three-" + o, "entity three text-" + o)));
            IntStream.rangeClosed(1, 100).forEach(o -> twoEntities().update(
                new EntityFour(Integer.toString(o), "entity four-" + o, "entity four text-" + o)));
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
