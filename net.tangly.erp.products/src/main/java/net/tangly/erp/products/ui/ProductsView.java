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
import net.tangly.core.TypeRegistry;
import net.tangly.erp.products.domain.Product;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;


@PageTitle("products-products")
class ProductsView extends EntityView<Product> {
    static class ProductForm extends EntityForm<Product, ProductsView> {
        public ProductForm(@NotNull ProductsView parent, @NotNull TypeRegistry registry) {
            super(parent, Product::new);
            initEntityForm();
            addTabAt("details", details(), 1);
        }

        private FormLayout details() {
            FormLayout form = new FormLayout();
            TextField ids = new TextField("Contract IDs");
            form.add(ids);
            binder().bind(ids, o -> o.contractIds().stream().collect(Collectors.joining(",")),
                (o, v) -> o.contractIds(List.of(v.split(","))));
            return form;
        }
    }

    public ProductsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        // TODO do we need the mode parameter?
        super(Product.class, domain, domain.realm().products(), mode);
        form(new ProductForm(this, domain.registry()));
        initEntityView();
    }
}