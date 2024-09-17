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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.core.HasDate;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceItem;
import net.tangly.ui.app.domain.AnalyticsView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

import static net.tangly.ui.components.ItemView.AMOUNT;
import static net.tangly.ui.components.ItemView.AMOUNT_LABEL;

public class AnalyticsProductsView extends AnalyticsView {
    private static final String ArticlesUsage = "Articles Usage";
    private final InvoicesBoundedDomainUi domain;
    private final Grid<Article> articleGrid;
    private HasDate.IntervalFilter<Invoice> dateFilter;


    public AnalyticsProductsView(@NotNull InvoicesBoundedDomainUi domain) {
        this.domain = domain;
        articleGrid = articlesGrid();
        dateFilter = new HasDate.IntervalFilter<>(from(), to());
        var layout = new HorizontalLayout(articleGrid);
        layout.setSizeFull();
        tabSheet().add(ArticlesUsage, layout);
    }


    @Override
    public void refresh() {
        dateFilter = new HasDate.IntervalFilter<>(from(), to());
        if (Objects.nonNull(articleGrid)) {
            articleGrid.getDataProvider().refreshAll();
        }
    }

    private Grid<Article> articlesGrid() {
        Grid<Article> grid = new Grid<>();
        grid.setPageSize(8);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
        grid.setWidth("1200px");

        grid.setItems(DataProvider.ofCollection(ProviderView.of(domain.domain().realm().articles(), o -> o.code() == ArticleCode.work).items()));
        grid.addColumn(Article::id).setKey("id").setHeader("Id").setSortable(true);
        grid.addColumn(Article::name).setKey("name").setHeader("Name").setSortable(true);
        grid.addColumn(Article::text).setKey("text").setHeader("Text").setSortable(true);
        grid.addColumn(Article::unit).setKey("unit").setHeader("Unit").setSortable(true);
        grid.addColumn(new NumberRenderer<>(Article::unitPrice, VaadinUtils.FORMAT)).setKey("unitPrice").setHeader("Price").setComparator(this::articleAmount)
            .setAutoWidth(true).setResizable(true).setSortable(true);

        grid.addColumn(this::nrOfInvoices).setKey("nrOfInvoices").setHeader("# Invoices").setSortable(true);
        grid.addColumn(new NumberRenderer<>(this::articleAmount, VaadinUtils.FORMAT)).setKey(AMOUNT).setHeader(AMOUNT_LABEL).setComparator(this::articleAmount)
            .setAutoWidth(true).setResizable(true).setSortable(true);
        return grid;
    }

    private long nrOfInvoices(@NotNull Article article) {
        return domain.domain().realm().invoices().items().stream().filter(dateFilter)
            .filter(o -> o.items().stream().map(InvoiceItem::article).anyMatch(e -> e.id().equals(article.id()))).count();
    }

    private BigDecimal articleAmount(@NotNull Article article) {
        return domain.domain().realm().invoices().items().stream().filter(dateFilter).flatMap(i -> i.items().stream())
            .filter(e -> e.article().id().equals(article.id())).map(InvoiceItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
