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

package net.tangly.commons.crm.products.ui;

import javax.inject.Inject;

import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.products.Product;
import net.tangly.bus.products.RealmProducts;
import net.tangly.commons.vaadin.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;

public class ProductsView extends InternalEntitiesView<Product> {
    private final RealmProducts realmProducts;

    @Inject
    public ProductsView(@NotNull RealmProducts realmProducts, @NotNull Mode mode) {
        super(Product.class, mode, realmProducts.products(), realmProducts.tagTypeRegistry());
        this.realmProducts = realmProducts;
        initializeGrid();
    }

    @Override
    protected void initializeGrid() {
        InternalEntitiesView.addQualifiedEntityColumns(grid());
        addAndExpand(filterCriteria(grid()), grid(), createCrudButtons());
    }

    @Override
    protected Product updateOrCreate(Product entity) {
        Product product = (entity != null) ? entity : new Product();
        try {
            binder.writeBean(product);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        return product;
    }
}
