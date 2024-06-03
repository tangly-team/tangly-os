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

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class CrmBoundedDomainUi extends BoundedDomainUi<CrmBoundedDomain> {
    public static final String LEADS = "Leads";
    public static final String NATURAL_ENTITIES = "Natural Entities";
    public static final String LEGAL_ENTITIES = "Legal Entities";
    public static final String EMPLOYEES = "Employees";
    public static final String CONTRACTS = "Contracts";
    public static final String INTERACTIONS = "Interactions";
    public static final String ACTIVITIES = "Activities";
    public static final String ANALYTICS = "Analytics";

    public CrmBoundedDomainUi(@NotNull CrmBoundedDomain crmDomain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        super(crmDomain);
        addView(LEADS, new LazyReference<>(() -> new LeadsView(this, Mode.EDITABLE)));
        addView(NATURAL_ENTITIES, new LazyReference<>(() -> new NaturalEntitiesView(this, Mode.EDITABLE)));
        addView(LEGAL_ENTITIES, new LazyReference<>(() -> new LegalEntitiesView(this, Mode.EDITABLE)));
        addView(EMPLOYEES, new LazyReference<>(() -> new EmployeesView(this, Mode.EDITABLE)));
        addView(CONTRACTS, new LazyReference<>(() -> new ContractsView(this, Mode.EDITABLE)));
        addView(INTERACTIONS, new LazyReference<>(() -> new InteractionsView(this, Mode.EDITABLE)));
        addView(ACTIVITIES, new LazyReference<>(() -> new ActivitiesView(this, Mode.EDITABLE)));
        addView(ANALYTICS, new LazyReference<>(() -> new AnalyticsCrmView(this, invoicesDomain)));
        addView(ENTITIES, new LazyReference<>(() -> new DomainView(this)));
        currentView(NATURAL_ENTITIES);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(LEADS, _ -> select(layout, view(LEADS).orElseThrow()));
        subMenu.addItem(LEGAL_ENTITIES, _ -> select(layout, view(LEGAL_ENTITIES).orElseThrow()));
        subMenu.addItem(NATURAL_ENTITIES, _ -> select(layout, view(NATURAL_ENTITIES).orElseThrow()));
        subMenu.addItem(CONTRACTS, _ -> select(layout, view(CONTRACTS).orElseThrow()));
        subMenu.addItem(EMPLOYEES, _ -> select(layout, view(EMPLOYEES).orElseThrow()));
        subMenu.addItem(INTERACTIONS, _ -> select(layout, view(INTERACTIONS).orElseThrow()));
        subMenu.addItem(ACTIVITIES, _ -> select(layout, view(ACTIVITIES).orElseThrow()));

        addAnalytics(layout, menuBar, view(ANALYTICS).orElseThrow());
        addAdministration(layout, menuBar, view(ENTITIES).orElseThrow(), new CmdFilesUploadCrm(domain()));
        select(layout);
    }
}
