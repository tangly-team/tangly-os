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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.Document;
import net.tangly.core.domain.DomainEntity;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DocumentsView;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class InvoicesBoundedDomainUi extends BoundedDomainUi<InvoicesBoundedDomain> implements BoundedDomain.EventListener {
    public static final String INVOICES = "Invoices";
    public static final String ARTICLES = "Articles";

    public InvoicesBoundedDomainUi(@NotNull InvoicesBoundedDomain domain) {
        super(domain);
        addView(Article.class, new LazyReference<>(() -> new ArticlesView(this, Mode.EDITABLE)));
        addView(Invoice.class, new LazyReference<>(() -> new InvoicesView(this, Mode.EDITABLE)));
        addView(Document.class, new LazyReference<>(() -> new DocumentsView(this, domain().realm().documents())));
        addView(DomainEntity.class, new LazyReference<>(() -> new DomainView(this)));
        addView(AnalyticsProductsView.class, new LazyReference<>(() -> new AnalyticsProductsView(this)));
        currentView(Invoice.class.getSimpleName());
        domain.subscribeInternally(this);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(ARTICLES, e -> select(layout, view(Article.class).orElseThrow()));
        subMenu.addItem(INVOICES, e -> select(layout, view(Invoice.class).orElseThrow()));
        subMenu.addItem(DOCUMENTS, e -> select(layout, view(Document.class).orElseThrow()));

        addAnalytics(layout, menuBar, view(AnalyticsProductsView.class).orElseThrow());
        addAdministration(layout, menuBar, view(DomainEntity.class).orElseThrow());
        select(layout);
    }
}
