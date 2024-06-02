/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.app.services;

import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.User;

import java.util.List;

public class AppsBoundedDomain extends BoundedDomain<AppsRealm, AppsBusinessLogic, AppsPort> {
    public static final String DOMAIN = "applications";

    public AppsBoundedDomain(AppsRealm realm, AppsBusinessLogic logic, AppsPort port, TypeRegistry registry) {
        super(DOMAIN, realm, logic, port, registry);
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, User.class, realm().users()));
    }

    @Override
    public AppsRealm realm() {
        return super.realm();
    }
}
