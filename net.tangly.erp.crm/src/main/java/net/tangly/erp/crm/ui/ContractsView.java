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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.ContractExtension;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;
import java.util.Locale;

/**
 * Regular CRUD view on contracts abstraction. The grid and edition dialog were optimized for usability.
 */

class ContractsView extends EntityView<Contract> {
    static class ContractForm extends MutableEntityForm<Contract, ContractsView> {
        public ContractForm(@NotNull ContractsView parent) {
            super(parent, Contract::new);
            initEntityForm();
            addTabAt("details", details(), 1);
            addTabAt("extensions", extensions(), 2);
        }

        private FormLayout details() {
            BankConnectionField bankConnection = new BankConnectionField();
            Select<Locale> locale = new Select<>();
            locale.setLabel("Language");
            locale.setItems(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH);
            Select<Currency> currency = new Select<>();
            currency.setLabel("Currency");
            currency.setItems(Currency.getInstance("CHF"), Currency.getInstance("EUR"));

            One2OneField<LegalEntity> seller = new One2OneField<>("Seller", LegalEntity.class, view().domain().realm().legalEntities());
            One2OneField<LegalEntity> sellee = new One2OneField<>("Sellee", LegalEntity.class, view().domain().realm().legalEntities());

            var form = new FormLayout();
            form.add(bankConnection, locale, currency);
            form.add(new HtmlComponent("br"));
            form.add(seller, sellee);

            binder().forField(bankConnection).bind(Contract::bankConnection, Contract::bankConnection);
            binder().forField(locale).bind(Contract::locale, Contract::locale);
            binder().forField(currency).bind(Contract::currency, Contract::currency);
            binder().forField(seller).bind(Contract::seller, Contract::seller);
            binder().forField(sellee).bind(Contract::sellee, Contract::sellee);
            return form;
        }

        private One2ManyOwnedField<ContractExtension> extensions() {
            One2ManyOwnedField<ContractExtension> extensions =
                new One2ManyOwnedField<>(new ContractExtensionsView((CrmBoundedDomainUi) view().domainUi(), Mode.EDITABLE));
            binder().bind(extensions, Contract::contractExtensions, (o, v) -> o.contractExtensions(extensions.generateModelValue()));
            return extensions;
        }
    }

    public ContractsView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(Contract.class, domain, domain.domain().realm().contracts(), mode);
        form(() -> new ContractForm(this));
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    @Override
    protected void addActions(@NotNull GridMenu<Contract> menu) {
        menu().add(new Hr());
        menu().add("Activate", e -> Cmd.ofItemCmd(e, this::contractActivated), GridMenu.MenuItemType.ITEM);
    }

    private void init() {
        var grid = grid();
        VaadinUtils.addColumn(grid, e -> e.sellee().name(), "customer", "Customer");
        VaadinUtils.addColumn(grid, Contract::currency, "currency", "Currency");
        grid.addColumn(new NumberRenderer<>(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setKey(AMOUNT).setHeader(AMOUNT_LABEL)
            .setComparator(o -> o.amountWithoutVat()).setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(Contract::budgetInHours, VaadinUtils.FORMAT)).setKey("budgetInHours").setHeader("Budget In Hours")
            .setComparator(o -> o.amountWithoutVat()).setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
    }

    /**
     * Activates the contract in the ERP system. The {@link net.tangly.erp.crm.events.ContractSignedEvent} is published to the event bus.
     * The activation states the intent to start working on the contract.
     *
     * @param contract contract to activate
     */
    private void contractActivated(@NotNull Contract contract) {
        var event = Contract.of(contract);
        domain().channel().submit(event);
    }
}
