/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.products.ui;

import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.Product;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

@PageTitle("products-products")
class ProductsView extends EntityView<Product> {
    public ProductsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        // TODO do we need the mode parameter?
        super(Product.class, domain, domain.realm().products(), Mode.LIST);
        initEntityView();
    }

    @Override
    protected void initEntityView() {
        addEntityColumns(grid());
        addEntityFilterFields(grid(), filter());
        buildMenu();
    }
}
