/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.BankConnectionField;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;
import java.util.Locale;

/**
 * Regular CRUD view on contracts abstraction. The grid and edition dialog wre optimized for usability.
 */

class ContractsView extends EntityView<Contract> {

    static class ContractForm extends net.tangly.ui.components.ItemForm<Contract, ContractsView> {
        protected EntityField<Contract> entity;
        protected BankConnectionField bankConnection;
        protected Select<Locale> locale;
        protected Select<Currency> currency;

        public ContractForm(@NotNull ContractsView parent) {
            super(parent);
            init();
        }

        @Override
        public void clear() {

        }

        @Override
        protected Contract createOrUpdateInstance(Contract entity) throws ValidationException {
            return null;
        }

        protected void init() {
            FormLayout form = new FormLayout();

            EntityField<Contract> entityField = new EntityField<>();
            BankConnectionField bankConnection = new BankConnectionField();
            Select<Locale> locale = new Select<>();
            locale.setLabel("Language");
            locale.setItems(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH);
            Select<Currency> currency = new Select<>();
            currency.setLabel("Currency");
            currency.setItems(Currency.getInstance("CHF"), Currency.getInstance("EUR"));
            //            One2OneField<LegalEntity, LegalEntitiesView> seller = new One2OneField<>("Seller", new LegalEntitiesView(domain, mode));
            //            One2OneField<LegalEntity, LegalEntitiesView> sellee = new One2OneField<>("Sellee", new LegalEntitiesView(domain, mode));

            //            fields(Set.of(entityField, bankConnection, locale, currency));

            form.add(entityField, bankConnection, locale, currency);
            form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));

            entityField.bind(binder(), true);
            binder().forField(locale).bind(Contract::locale, Contract::locale);
            binder().forField(currency).bind(Contract::currency, Contract::currency);
            //            binder().forField(bankConnection).withValidator(bankConnection.validator()).bind(Contract::bankConnection, Contract::bankConnection);
            //            binder.forField(seller).bind(Contract::seller, Contract::seller);
            //            binder.forField(sellee).bind(Contract::sellee, Contract::sellee);

        }
    }

    private final transient CrmBoundedDomain domain;

    public ContractsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Contract.class, domain, domain.realm().contracts(), mode);
        this.domain = domain;
        form = new ContractForm(this);
        init();
    }

    @Override
    protected void init() {
        var grid = grid();
        // TODO lEntitiesView.addQualifiedEntityColumns(grid);
        grid.addColumn(e -> e.sellee().name()).setKey("customer").setHeader("Customer").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true).setResizable(true)
            .setSortable(true).setTextAlign(ColumnTextAlign.END);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid().appendHeaderRow();

        //        if (filter() instanceof ContractFilter filter) {
        //            addFilterText(headerRow, ID, ID_LABEL, filter::id);
        //            addFilterText(headerRow, NAME, NAME_LABEL, filter::name);
    }
}

