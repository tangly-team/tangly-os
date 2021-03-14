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
import net.tangly.components.grids.PaginatedGrid;
import net.tangly.core.codes.CodeType;
import net.tangly.bus.invoices.Article;
import net.tangly.bus.invoices.ArticleCode;
import net.tangly.bus.invoices.InvoicesBoundedDomain;
import net.tangly.commons.vaadin.CodeField;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on articles abstraction. The grid and edition dialog wre optimized for usability.
 */
class ArticlesView extends EntitiesView<Article> {
    private final transient InvoicesBoundedDomain domain;
    private final TextField id;
    private final TextField name;
    private final TextField text;
    private final CodeField<ArticleCode> code;
    private final TextField unit;
    private final TextField unitPrice;
    private final TextField vatRate;


    public ArticlesView(@NotNull InvoicesBoundedDomain domain, @NotNull Mode mode) {
        super(Article.class, mode, domain.realm().articles());
        this.domain = domain;
        id = new TextField("Id", "id");
        name = VaadinUtils.createTextField("Name", "name");
        text = VaadinUtils.createTextField("Text", "text");
        code = new CodeField<>(CodeType.of(ArticleCode.class), "code");
        unit = VaadinUtils.createTextField("Unit", "unit");
        unitPrice = VaadinUtils.createTextField("Unit Price", "unit price");
        vatRate = VaadinUtils.createTextField("VAT Rate", "VAT rate");
        initialize();
    }

    @Override
    protected void initialize() {
        PaginatedGrid<Article> grid = grid();
        grid.addColumn(Article::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::unit).setKey("unit").setHeader("Unit").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(Article::unitPrice, VaadinUtils.FORMAT)).setKey("unitPrice").setHeader("Unit Price").setAutoWidth(true)
                .setResizable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Article::vatRate).setKey("vatRate").setHeader("VAT Rate").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        addAndExpand(grid(), gridButtons());
    }

    @Override
    protected Article updateOrCreate(Article entity) {
        return new Article(id.getValue(), name.getValue(), text.getValue(), code.getValue(), VaadinUtils.toBigDecimal(unitPrice.getValue()), unit.getValue(),
                VaadinUtils.toBigDecimal(vatRate.getValue()));
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Article entity, @NotNull FormLayout form) {
        readOnly(operation.isReadOnly());

        VaadinUtils.configureId(operation, id);
        VaadinUtils.readOnly(operation, name, unit, unitPrice, vatRate, code, text);
        id.setRequired(true);
        name.setRequired(true);
        unitPrice.setRequired(true);
        vatRate.setRequired(true);

        if (entity != null) {
            id.setValue(entity.id());
            name.setValue(entity.name());
            text.setValue(entity.text());
            code.setValue(entity.code());
            unit.setValue(entity.unit());
            unitPrice.setValue(entity.unitPrice().toPlainString());
            vatRate.setValue(entity.vatRate().toPlainString());
        }
        form.add(id, name, unit, unitPrice, vatRate, new HtmlComponent("br"));
        form.add(text, 3);
        return form;
    }

    protected void readOnly(boolean readOnly) {
        id.setReadOnly(readOnly);
        name.setReadOnly(readOnly);
        unit.setReadOnly(readOnly);
        vatRate.setReadOnly(readOnly);
        text.setReadOnly(readOnly);
        code.setReadOnly(readOnly);
        unitPrice.setReadOnly(readOnly);
    }
}
