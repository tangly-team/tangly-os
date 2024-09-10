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

package net.tangly.erp.invoices.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on articles abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("invoices-activities")
class ArticlesView extends ItemView<Article> {

    static class ArticleFilter extends ItemView.ItemFilter<Article> {
        @Override
        public boolean test(@NotNull Article entity) {
            return true;
        }
    }

    static class ArticleForm extends ItemForm<Article, ArticlesView> {
        ArticleForm(@NotNull ArticlesView view) {
            super(view);
            addTabAt("details", details(), 0);
        }

        private FormLayout details() {
            var id = new TextField("Id", "id");
            id.setRequired(true);
            var name = new TextField("Name", "name");
            name.setRequired(true);
            var text = new TextField("Text", "text");
            ComboBox<ArticleCode> code = ItemForm.createCodeField(CodeType.of(ArticleCode.class), "code");
            var unit = new TextField("Unit", "unit");
            unit.setRequired(true);
            var unitPrice = new BigDecimalField("Unit Price", "unit price");
            var vatRate = new BigDecimalField("VAT Rate", "VAT rate");
            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(id, name, text, code, unit, unitPrice, vatRate);

            binder().bindReadOnly(id, Article::id);
            binder().bindReadOnly(name, Article::name);
            binder().bindReadOnly(text, Article::text);
            binder().bindReadOnly(code, Article::code);
            binder().bindReadOnly(unit, Article::unit);
            binder().bindReadOnly(unitPrice, Article::unitPrice);
            return form;
        }

        @Override
        protected Article createOrUpdateInstance(Article entity) throws ValidationException {
            return new Article(fromBinder("id"), fromBinder("name"), fromBinder("text"), fromBinder("code"), fromBinder("unitPrice"), fromBinder("unit"));
        }
    }

    public ArticlesView(@NotNull InvoicesBoundedDomainUi domain, @NotNull Mode mode) {
        super(Article.class, domain, domain.domain().realm().articles(), new ArticleFilter(), mode);
        form(() -> new ArticleForm(this));
        init();
    }

    private void init() {
        Grid<Article> grid = grid();
        grid.addColumn(Article::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Article::unit).setKey("unit").setHeader("Unit").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(Article::unitPrice, VaadinUtils.FORMAT)).setKey("unitPrice").setHeader("Unit Price")
            .setComparator(Article::unitPrice).setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Article::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
    }
}
