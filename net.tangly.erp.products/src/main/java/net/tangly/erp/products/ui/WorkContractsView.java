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

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.products.domain.WorkContract;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Regular CRUD view on WorkContracts abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("invoices-activities")
class WorkContractsView extends ItemView<WorkContract> {

    static class WorkContractFilter extends ItemFilter<WorkContract> {
        @Override
        public boolean test(@NotNull WorkContract entity) {
            return true;
        }
    }

    static class WorkContractForm extends ItemForm<WorkContract, WorkContractsView> {
        WorkContractForm(@NotNull WorkContractsView parent) {
            super(parent);
            addTabAt("details", details(), 0);
        }

        private FormLayout details() {
            var id = new TextField("Id", "id");
            id.setRequired(true);
            var mainContractId = new TextField("MainContractId", ",main contract id");
            var from = new DatePicker("From");
            var to = new DatePicker("To");
            Select<Locale> locale = new Select<>();
            locale.setLabel("Language");
            locale.setItems(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH);
            var budgetInHours = new IntegerField("Budget in Hours");

            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(id, mainContractId, locale, from, to, budgetInHours);

            binder().bindReadOnly(id, WorkContract::id);
            binder().bindReadOnly(mainContractId, WorkContract::mainContractId);
            binder().bindReadOnly(locale, WorkContract::locale);
            binder().bindReadOnly(from, WorkContract::from);
            binder().bindReadOnly(to, WorkContract::to);
            binder().bindReadOnly(budgetInHours, WorkContract::budgetInHours);
            return form;
        }

        @Override
        protected WorkContract createOrUpdateInstance(WorkContract entity) throws ValidationException {
            return new WorkContract(fromBinder("id"), fromBinder("mainContractId"), fromBinder("form"), fromBinder("to"), fromBinder("locale"), fromBinder("budgetInHours"));
        }
    }

    public WorkContractsView(@NotNull ProductsBoundedDomain domain, @NotNull Mode mode) {
        super(WorkContract.class, domain, domain.realm().contracts(), new WorkContractFilter(), mode);
        form(() -> new WorkContractForm(this));
        init();
    }

    private void init() {
        Grid<WorkContract> grid = grid();
        grid.addColumn(WorkContract::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::mainContractId).setKey("context").setHeader("Context").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::from).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::to).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::locale).setKey("locale").setHeader("Locale").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::budgetInHours).setKey("budgetInHours").setHeader("Budget in Hours").setAutoWidth(true).setResizable(true).setSortable(true);
    }
}
