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

package net.tangly.commons.crm.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.commons.vaadin.BankConnectionField;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.commons.vaadin.One2OneField;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

public class ContractsView extends CrmEntitiesView<Contract> {
    public ContractsView(@NotNull Crm crm, @NotNull Mode mode) {
        super(crm, Contract.class, mode, ContractsView::defineContractsGrid, crm.contracts());
        grid().addColumn(o -> VaadinUtils.format(crm().invoicedAmount(o))).setKey("invoicedAmount").setHeader("Invoiced").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    public static void defineContractsGrid(@NotNull Grid<Contract> grid) {
        InternalEntitiesView.defineGrid(grid);
        grid.addColumn(e -> e.sellee().name()).setKey("customer").setHeader("Customer").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::amountWithoutVat).setKey("amount").setHeader("Amount").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected Contract create() {
        return new Contract();
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Contract entity) {
        boolean readonly = Mode.readOnly(mode);
        EntityField entityField = new EntityField();
        BankConnectionField bankConnection = new BankConnectionField();
        One2OneField<LegalEntity, LegalEntitiesView> seller = new One2OneField<>("Seller", new LegalEntitiesView(crm(), mode));
        One2OneField<LegalEntity, LegalEntitiesView> sellee = new One2OneField<>("Sellee", new LegalEntitiesView(crm(), mode));

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField);
        form.add(new HtmlComponent("br"));
        form.add(bankConnection);
        form.add(new HtmlComponent("br"));
        form.add(seller, sellee);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.forField(bankConnection).withValidator(bankConnection.validator()).bind(Contract::bankConnection, Contract::bankConnection);
        binder.forField(seller).bind(Contract::seller, Contract::seller);
        binder.forField(sellee).bind(Contract::sellee, Contract::sellee);
        binder.readBean(entity);
        return form;
    }
}
