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
import net.tangly.core.DateRange;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.domain.WorkContract;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

/**
 * Regular CRUD view on WorkContracts abstraction. The grid and edition dialog are optimized for usability.
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
            binder().bindReadOnly(from, o -> o.range().from());
            binder().bindReadOnly(to, o -> o.range().to());
            binder().bindReadOnly(budgetInHours, WorkContract::budgetInHours);
            return form;
        }

        @Override
        protected WorkContract createOrUpdateInstance(WorkContract entity) throws ValidationException {
            return new WorkContract(fromBinder("id"), fromBinder("mainContractId"), DateRange.of(fromBinder("form"), fromBinder("to")), fromBinder("locale"),
                fromBinder("budgetInHours"));
        }
    }

    public WorkContractsView(@NotNull ProductsBoundedDomainUi domain, @NotNull Mode mode) {
        super(WorkContract.class, domain, domain.domain().realm().contracts(), new WorkContractFilter(), mode);
        form(() -> new WorkContractForm(this));
        init();
    }

    @Override
    public ProductsBoundedDomain domain() {
        return (ProductsBoundedDomain) super.domain();
    }

    private void init() {
        Grid<WorkContract> grid = grid();
        grid.addColumn(WorkContract::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::mainContractId).setKey("context").setHeader("Context").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.range().from()).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.range().to()).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::locale).setKey("locale").setHeader("Locale").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(WorkContract::budgetInHours).setKey("budgetInHours").setHeader("Budget in Hours").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(this::effortPerContract).setKey("effortInHours").setHeader("Effort in Hours").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(this::budgetForMainContract).setKey("mainBudgetInHours").setHeader("Main Budget in Hours").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(this::effortForMainContract).setKey("mainEffortInHours").setHeader("Main Effort in Hours").setAutoWidth(true).setResizable(true)
            .setSortable(true);
    }

    private BigDecimal effortPerContract(@NotNull WorkContract contract) {
        return Assignment.convert(
            domain().realm().efforts().items().stream().filter(o -> o.contractId().equals(contract.id())).mapToInt(Effort::duration).sum(), ChronoUnit.HOURS);
    }

    private BigDecimal budgetForMainContract(@NotNull WorkContract contract) {
        return Objects.isNull(contract.mainContractId()) ?
            new BigDecimal(domain().realm().contracts().items().stream().filter(o -> contract.id().equals(o.id()) || contract.id().equals(o.mainContractId()))
                .mapToInt(WorkContract::budgetInHours).sum()) : BigDecimal.ZERO;
    }

    private BigDecimal effortForMainContract(@NotNull WorkContract contract) {
        return Objects.isNull(contract.mainContractId()) ?
            domain().realm().contracts().items().stream().filter(o -> contract.id().equals(o.id()) || contract.id().equals(o.mainContractId())).map(
                    this::effortPerContract)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
    }
}
