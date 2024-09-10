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

package net.tangly.erp.products.ui;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.Product;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.MutableEntityForm;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * Regular CRUD view on the product entity. The grid and edition dialog are optimized for usability.
 */
@PageTitle("products-products")
class ProductsView extends EntityView<Product> {
    static class ProductForm extends MutableEntityForm<Product, ProductsView> {
        public ProductForm(@NotNull ProductsView view) {
            super(view, Product::new);
            initEntityForm();
            addTabAt("details", details(), 1);
        }

        private FormLayout details() {
            FormLayout form = new FormLayout();
            TextField ids = new TextField("Contract IDs");
            form.add(ids);
            binder().bind(ids, o -> String.join(",", o.contractIds()), (o, v) -> o.contractIds(List.of(v.split(","))));
            return form;
        }
    }

    public ProductsView(@NotNull ProductsBoundedDomainUi domain, @NotNull Mode mode) {
        super(Product.class, domain, domain.domain().realm().products(), mode);
        form(() -> new ProductForm(this));
        initEntityView();
    }
}
