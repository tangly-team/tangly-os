/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.core.crm.LegalEntity;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.BankConnectionField;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.InternalEntitiesView;
import net.tangly.ui.components.One2OneField;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Currency;
import java.util.Locale;

/**
 * Regular CRUD view on contracts abstraction. The grid and edition dialog wre optimized for usability.
 */

class ContractsView extends InternalEntitiesView<Contract> {
    private final transient CrmBoundedDomain domain;

    public ContractsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Contract.class, mode, domain.realm().contracts(), domain.registry());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        var grid = grid();
        InternalEntitiesView.addQualifiedEntityColumns(grid);
        grid.addColumn(e -> e.sellee().name()).setKey("customer").setHeader("Customer").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new NumberRenderer<>(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true)
            .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        addAndExpand(filterCriteria(false, false, InternalEntitiesView::addQualifiedEntityFilters), grid(), gridButtons());
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Contract entity) {
        boolean readonly = mode.readOnly();
        EntityField<Contract> entityField = new EntityField<>();
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
        One2OneField<LegalEntity, LegalEntitiesView> seller = new One2OneField<>("Seller", new LegalEntitiesView(domain, mode));
        seller.setReadOnly(readonly);
        One2OneField<LegalEntity, LegalEntitiesView> sellee = new One2OneField<>("Sellee", new LegalEntitiesView(domain, mode));
        sellee.setReadOnly(readonly);

        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);
        form.add(new HtmlComponent("br"));
        form.add(locale, currency);
        form.add(new HtmlComponent("br"));
        form.add(bankConnection);
        form.add(new HtmlComponent("br"));
        form.add(seller, sellee);

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

    @Override
    protected Contract updateOrCreate(Contract entity) {
        return EntitiesView.updateOrCreate(entity, binder, Contract::new);
    }
}
