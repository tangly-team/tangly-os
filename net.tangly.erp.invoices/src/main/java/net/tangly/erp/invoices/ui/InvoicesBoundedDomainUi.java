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
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class InvoicesBoundedDomainUi extends BoundedDomainUi<InvoicesBoundedDomain> {
    private final ArticlesView articlesView;
    private final InvoicesView invoicesView;
    private final DomainView domainView;

    public InvoicesBoundedDomainUi(@NotNull InvoicesBoundedDomain domain) {
        super(domain);
        articlesView = new ArticlesView(domain, Mode.EDIT);
        invoicesView = new InvoicesView(domain, Mode.EDIT);
        domainView = new DomainView(domain);
        currentView(invoicesView);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Articles", e -> select(layout, articlesView));
        subMenu.addItem("Invoices", e -> select(layout, invoicesView));

        addAdministration(layout, menuBar, domainView, new CmdFilesUploadInvoices(domain()));
        select(layout);
    }

    @Override
    public void refreshViews() {
        articlesView.refresh();
        invoicesView.refresh();
    }
}