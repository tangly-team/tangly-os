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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.commons.lang.functional.LazyReference;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.User;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.domain.WorkContract;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.app.domain.View;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

public class ProductsBoundedDomainUi extends BoundedDomainUi<ProductsBoundedDomain> implements BoundedDomain.EventListener {
    public static final String PRODUCTS = "Products";
    public static final String WORK_CONTRACTS = "Contracts";
    public static final String ASSIGNMENTS = "Assignments";
    public static final String EFFORTS = "Efforts";
    private Provider<Effort> efforts;
    private Provider<Assignment> assignments;

    public ProductsBoundedDomainUi(@NotNull ProductsBoundedDomain domain) {
        super(domain);
        efforts = domain.realm().efforts();
        assignments = domain.realm().assignments();
        addView(PRODUCTS, new LazyReference<>(() -> new ProductsView(this, Mode.EDITABLE)));
        addView(WORK_CONTRACTS, new LazyReference<>(() -> new WorkContractsView(this, Mode.EDITABLE)));
        addView(ASSIGNMENTS, new LazyReference<>(() -> new AssignmentsView(this, Mode.EDITABLE)));
        addView(EFFORTS, new LazyReference<>(() -> new EffortsView(this, Mode.EDITABLE)));
        addView(ENTITIES, new LazyReference<>(() -> new DomainView(this)));
        currentView(view(PRODUCTS).orElseThrow());
        domain.subscribeInternally(this);
    }

    @Override
    public void userChanged(@NotNull User user) {
        var rights = user.accessRightsFor(ProductsBoundedDomain.DOMAIN).orElseThrow();
        boolean isRestricted = rights.right() == AccessRightsCode.restrictedUser;
        String username = user.username();
        efforts = isRestricted ? ProviderView.of(domain().realm().efforts(), u -> u.assignment().name().equals(username)) : domain().realm().efforts();
        assignments = isRestricted ? ProviderView.of(domain().realm().assignments(), u -> u.name().equals(username)) : domain().realm().assignments();
        view(EFFORTS).ifPresent(v -> v.ifPresent(o -> ((EffortsView) o).provider(efforts)));
        view(ASSIGNMENTS).ifPresent(v -> v.ifPresent(o -> ((AssignmentsView) o).provider(assignments)));
        super.userChanged(user);
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(BoundedDomainUi.ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem(PRODUCTS, _ -> select(layout, view(PRODUCTS).orElseThrow()));
        subMenu.addItem(WORK_CONTRACTS, _ -> select(layout, view(WORK_CONTRACTS).orElseThrow()));
        subMenu.addItem(ASSIGNMENTS, _ -> select(layout, view(ASSIGNMENTS).orElseThrow()));
        subMenu.addItem(EFFORTS, _ -> select(layout, view(EFFORTS).orElseThrow()));
        addAdministration(layout, menuBar, view(ENTITIES).orElseThrow());
        select(layout);
    }

    Provider<Effort> efforts() {
        return efforts;
    }

    Provider<Assignment> assignments() {
        return assignments;
    }

    @Override
    public void onNext(Object event) {
        if (event instanceof EntityChangedInternalEvent entityChanged) {
            if (entityChanged.entityName().equals(Effort.class.getSimpleName())) {
                view(EFFORTS).ifPresent(v -> UI.getCurrent().access(() -> v.ifPresent(View::refresh)));
            } else if (entityChanged.entityName().equals(WorkContract.class.getSimpleName())) {
                view(WORK_CONTRACTS).ifPresent(v -> UI.getCurrent().access(() -> v.ifPresent(View::refresh)));
            }
        }
    }
}
