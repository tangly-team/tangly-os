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

package net.tangly.erp.products.services;

import net.tangly.app.Application;
import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.UsersProvider;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.domain.Product;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ProductsBoundedDomain extends BoundedDomain<ProductsRealm, ProductsBusinessLogic, ProductsPort> {
    public static final String DOMAIN = "products";

    public ProductsBoundedDomain(ProductsRealm realm, ProductsBusinessLogic logic, ProductsPort port, TypeRegistry registry, UsersProvider usersProvider) {
        super(DOMAIN, realm, logic, port, registry, usersProvider);
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        addTagCounts(registry(), realm().products(), counts);
        addTagCounts(registry(), realm().assignments(), counts);
        return counts;
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, Assignment.class, realm().assignments()), new DomainEntity<>(DOMAIN, Effort.class, realm().efforts()),
            new DomainEntity<>(DOMAIN, Product.class, realm().products()));
    }

    @Override
    public ProductsRealm realm() {
        return super.realm();
    }

    @Override
    public void startup() {
        Application.instance().tenant("tangly").getBoundedDomain(CrmBoundedDomain.DOMAIN).ifPresent(o -> o.subscribe(new CrmEventsProcessor(this)));
    }
}
