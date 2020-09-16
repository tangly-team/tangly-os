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

package net.tangly.commons.invoices.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.invoices.Product;
import net.tangly.bus.invoices.ProductCode;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.vaadin.CodeField;
import net.tangly.commons.vaadin.ExternalEntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class ProductsView extends ExternalEntitiesView<Product> {
    private final TextField name;
    private final TextField text;
    private final CodeField<ProductCode> code;
    private final TextField unit;
    private final TextField unitPrice;
    private final TextField vatRate;


    /**
     * Constructor of the CRUD view for a product.
     *
     * @param provider provider of the class
     */
    public ProductsView(@NotNull Provider<Product> provider, @NotNull Mode mode) {
        super(Product.class, mode, provider);
        name = VaadinUtils.createTextField("Name", "name");
        text = VaadinUtils.createTextField("Text", "text");
        code = new CodeField<>(CodeType.of(ProductCode.class), "code");
        unit = VaadinUtils.createTextField("Unit", "unit");
        unitPrice = VaadinUtils.createTextField("Unit Price", "unit price");
        vatRate = VaadinUtils.createTextField("VAT Rate", "VAT rate");
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<Product> grid = grid();
        grid.addColumn(Product::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Product::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Product::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Product::unit).setKey("unit").setHeader("Unit").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(o -> o.unitPrice(), VaadinUtils.FORMAT)).setKey("unitPrice").setHeader("Unit Price").setAutoWidth(true)
                .setResizable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Product::vatRate).setKey("vatRate").setHeader("VAT Rate").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected Product create() {
        return new Product(id.getValue(), name.getValue(), text.getValue(), code.getValue(), VaadinUtils.toBigDecimal(unitPrice.getValue()), unit.getValue(),
                VaadinUtils.toBigDecimal(vatRate.getValue()));
    }

    @Override
    protected FormLayout prefillFrom(@NotNull Operation operation, Product entity, @NotNull FormLayout form) {
        boolean readonly = Operation.isReadOnly(operation);
        id.setReadOnly(readonly);
        id.setRequired(true);

        name.setReadOnly(readonly);
        name.setRequired(true);

        text.setReadOnly(readonly);

        code.setReadOnly(readonly);

        unitPrice.setReadOnly(readonly);
        unitPrice.setRequired(true);

        unit.setReadOnly(readonly);

        vatRate.setReadOnly(readonly);
        vatRate.setRequired(true);

        id.setValue(entity.id());
        name.setValue(entity.name());
        text.setValue(entity.text());
        code.setValue(entity.code());
        unit.setValue(entity.unit());
        unitPrice.setValue(entity.unitPrice().toPlainString());
        vatRate.setValue(entity.vatRate().toPlainString());
        form.add(id, name, unit, unitPrice, vatRate, new HtmlComponent("br"));
        form.add(text, 3);
        return form;
    }
}
