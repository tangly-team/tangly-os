/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.products.ports;

import javax.inject.Inject;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.Product;
import net.tangly.bus.products.ProductsRealm;
import net.tangly.bus.providers.InstanceProvider;
import net.tangly.bus.providers.InstanceProviderInMemory;
import net.tangly.bus.providers.Provider;
import net.tangly.bus.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

public class ProductsEntities implements ProductsRealm {
    private final TagTypeRegistry registry;
    private final InstanceProvider<Product> products;
    private final InstanceProvider<Assignment> assignments;
    private final ProviderInMemory<Effort> efforts;


    @Inject
    public ProductsEntities(@NotNull TagTypeRegistry registry) {
        this.registry = registry;
        this.products = new InstanceProviderInMemory<>();
        this.assignments = new InstanceProviderInMemory<>();
        this.efforts = new ProviderInMemory<>();
    }

    @Override
    public TagTypeRegistry tagTypeRegistry() {
        return registry;
    }

    @Override
    public Provider<Assignment> assignements() {
        return assignments;
    }

    @Override
    public Provider<Effort> efforts() {
        return efforts;
    }

    @Override
    public Provider<Product> products() {
        return products;
    }
}
