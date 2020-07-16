/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.io.IOException;
import java.nio.file.Paths;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import net.tangly.commons.crm.ui.ContractsView;
import net.tangly.commons.crm.ui.EmployeesView;
import net.tangly.commons.crm.ui.LegalEntitiesView;
import net.tangly.commons.crm.ui.NaturalEntitiesView;
import net.tangly.crm.ports.Crm;
import net.tangly.crm.ports.CrmWorkflows;
import org.jetbrains.annotations.NotNull;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends VerticalLayout {
    private Component currentView;
    private final Crm crm;
    private NaturalEntitiesView naturalEntitiesView;
    private LegalEntitiesView legalEntitiesView;
    private EmployeesView employeesView;
    private ContractsView contractsView;

    public MainView() throws IOException {
        crm = new Crm();
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntitiesFromTsv(Paths.get("/Users/Shared/tangly/"));

        naturalEntitiesView = new NaturalEntitiesView(crm);
        legalEntitiesView = new LegalEntitiesView(crm);
        employeesView = new EmployeesView(crm);
        contractsView = new ContractsView(crm);

        setSizeFull();
        currentView = naturalEntitiesView;
        add(menuBar(), naturalEntitiesView);
    }

    private MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);

        MenuItem crm = menuBar.addItem("CRM");
        SubMenu crmSubMenu = crm.getSubMenu();
        MenuItem legalEntities = crmSubMenu.addItem("Legal Entities", e -> select(legalEntitiesView));
        MenuItem naturalEntities = crmSubMenu.addItem("Natural Entities", e -> select(naturalEntitiesView));
        MenuItem contracts = crmSubMenu.addItem("Contracts", e -> select(contractsView));

        MenuItem activities = menuBar.addItem("Activities");
        SubMenu activitiesSubMenu = activities.getSubMenu();
        MenuItem products = activitiesSubMenu.addItem("Products");
        MenuItem projects = activitiesSubMenu.addItem("Projects");

        MenuItem ledger = menuBar.addItem("Financials");
        SubMenu ledgerSubMenu = ledger.getSubMenu();
        return menuBar;
    }

    private void select(@NotNull Component view) {
        this.remove(currentView);
        this.add(view);
        currentView = view;
    }
}
