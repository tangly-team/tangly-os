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

import java.util.Currency;
import java.util.Locale;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.RealmCrm;
import net.tangly.bus.invoices.BusinessLogicInvoices;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.commons.vaadin.BankConnectionField;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.One2OneField;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class ContractsView extends CrmEntitiesView<Contract> {
    private final RealmInvoices realmInvoices;

    public ContractsView(@NotNull RealmCrm realmCrm, RealmInvoices realmInvoices, @NotNull Mode mode) {
        super(realmCrm, Contract.class, mode, realmCrm.contracts());
        this.realmInvoices = realmInvoices;
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        BusinessLogicInvoices logic = new BusinessLogicInvoices(realmInvoices);
        Grid<Contract> grid = grid();
        grid.addColumn(e -> e.sellee().name()).setKey("customer").setHeader("Customer").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true)
                .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(o -> logic.invoicedAmountWithoutVatForContract(o.id(), null, null), VaadinUtils.FORMAT)).setKey("invoicedAmount")
                .setHeader("Invoiced").setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);

    }

    @Override
    protected Contract create() {
        return new Contract();
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Contract entity) {
        boolean readonly = Mode.readOnly(mode);
        EntityField entityField = new EntityField();
        entityField.setReadOnly(readonly);
        BankConnectionField bankConnection = new BankConnectionField();
        bankConnection.setReadOnly(readonly);
        Select<Locale> locale = new Select<>();
        locale.setLabel("Language");
        locale.setItems(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH);
        locale.setReadOnly(readonly);
        Select<Currency> currency = new Select<>();
        currency.setLabel("Currency");
        currency.setItems(Currency.getInstance("CHF"), Currency.getInstance("EUR"));
        currency.setReadOnly(readonly);
        One2OneField<LegalEntity, LegalEntitiesView> seller = new One2OneField<>("Seller", new LegalEntitiesView(realm(), mode));
        seller.setReadOnly(readonly);
        One2OneField<LegalEntity, LegalEntitiesView> sellee = new One2OneField<>("Sellee", new LegalEntitiesView(realm(), mode));
        sellee.setReadOnly(readonly);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField);
        form.add(new HtmlComponent("br"));
        form.add(locale, currency);
        form.add(new HtmlComponent("br"));
        form.add(bankConnection);
        form.add(new HtmlComponent("br"));
        form.add(seller);
        form.add(new HtmlComponent("br"));
        form.add(sellee);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.forField(locale).bind(Contract::locale, Contract::locale);
        binder.forField(currency).bind(Contract::currency, Contract::currency);
        binder.forField(bankConnection).withValidator(bankConnection.validator()).bind(Contract::bankConnection, Contract::bankConnection);
        binder.forField(seller).bind(Contract::seller, Contract::seller);
        binder.forField(sellee).bind(Contract::sellee, Contract::sellee);
        binder.readBean(entity);
        return form;
    }
}
