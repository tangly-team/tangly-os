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
import net.tangly.core.domain.DomainEntity;
import net.tangly.erp.crm.domain.*;
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
    public static final String OPPORTUNITIES = "Opportunities";
    public static final String ACTIVITIES = "Activities";
    public static final String ANALYTICS = "Analytics";

    public CrmBoundedDomainUi(@NotNull CrmBoundedDomain crmDomain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        super(crmDomain);
        addView(Lead.class, new LazyReference<>(() -> new LeadsView(this, Mode.EDITABLE)));
        addView(NaturalEntity.class, new LazyReference<>(() -> new NaturalEntitiesView(this, Mode.EDITABLE)));
        addView(LegalEntity.class, new LazyReference<>(() -> new LegalEntitiesView(this, Mode.EDITABLE)));
        addView(Employee.class, new LazyReference<>(() -> new EmployeesView(this, Mode.EDITABLE)));
        addView(Contract.class, new LazyReference<>(() -> new ContractsView(this, Mode.EDITABLE)));
        addView(Opportunity.class, new LazyReference<>(() -> new OpportunitiesView(this, Mode.EDITABLE)));
        addView(Activity.class, new LazyReference<>(() -> new ActivitiesView(this, Mode.EDITABLE)));
        addView(AnalyticsCrmView.class, new LazyReference<>(() -> new AnalyticsCrmView(this, invoicesDomain)));
        addView(DomainEntity.class, new LazyReference<>(() -> new DomainView(this)));
        currentView(NaturalEntity.class.getSimpleName());
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(LEADS, _ -> select(layout, view(Lead.class.getSimpleName()).orElseThrow()));
        subMenu.addItem(LEGAL_ENTITIES, _ -> select(layout, view(LegalEntity.class).orElseThrow()));
        subMenu.addItem(NATURAL_ENTITIES, _ -> select(layout, view(NaturalEntity.class).orElseThrow()));
        subMenu.addItem(CONTRACTS, _ -> select(layout, view(Contract.class).orElseThrow()));
        subMenu.addItem(EMPLOYEES, _ -> select(layout, view(Employee.class).orElseThrow()));
        subMenu.addItem(OPPORTUNITIES, _ -> select(layout, view(Opportunity.class).orElseThrow()));
        subMenu.addItem(ACTIVITIES, _ -> select(layout, view(Activity.class).orElseThrow()));

        addAnalytics(layout, menuBar, view(AnalyticsCrmView.class).orElseThrow());
        addAdministration(layout, menuBar, view(DomainEntity.class).orElseThrow());
        select(layout);
    }
}
