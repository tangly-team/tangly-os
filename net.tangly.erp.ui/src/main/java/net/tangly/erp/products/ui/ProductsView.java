/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.products.ui;

import net.tangly.erp.products.domain.Product;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

class ProductsView extends InternalEntitiesView<Product> {
    private final ProductsBoundedDomain domain;

    @Inject
    public ProductsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(Product.class, mode, domain.realm().products(), domain.registry());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        InternalEntitiesView.addQualifiedEntityColumns(grid());
        addAndExpand(filterCriteria(false, false, InternalEntitiesView::addQualifiedEntityFilters), grid(), gridButtons());
    }

    @Override
    protected Product updateOrCreate(Product entity) {
        return EntitiesView.updateOrCreate(entity, binder, Product::new);
    }
}
