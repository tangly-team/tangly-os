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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.domain.Opportunity;
import net.tangly.erp.crm.domain.OpportunityCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Regular CRUD view on interactions' abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("crm-interactions")
class OpportunitiesView extends EntityView<Opportunity> {
    static class InteractionForm extends MutableEntityForm<Opportunity, OpportunitiesView> {
        protected InteractionForm(@NotNull OpportunitiesView view) {
            super(view, Opportunity::new);
            initEntityForm();
            addTabAt("details", details(), 1);
            var activities = new One2ManyOwnedField<>(new ActivitiesView(view.domainUi(), Mode.EDITABLE, true));
            binder().bind(activities, Opportunity::activities, (o, v) -> o.activities(activities.generateModelValue()));
            addTabAt("activities", activities, 2);
        }

        private FormLayout details() {
            var potential = new BigDecimalField("Potential", "potential");
            var probability = new BigDecimalField("Probability", "probability");
            var code = ItemForm.createCodeField(CodeType.of(OpportunityCode.class), "code");
            var contact = new One2OneField<>("Contact", Employee.class, (view().domain().realm().employees()));

            FormLayout form = new FormLayout();
            form.add(potential, probability, code, contact);

            binder().bind(potential, Opportunity::potential, Opportunity::potential);
            binder().bind(probability, Opportunity::probability, Opportunity::probability);
            binder().forField(code).bind(Opportunity::code, Opportunity::code);
            binder().forField(contact).bind(Opportunity::contact, Opportunity::contact);
            return form;
        }
    }

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    public OpportunitiesView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(Opportunity.class, domain, domain.domain().realm().opportunities(), mode);
        form(() -> new InteractionForm(this));
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    @Override
    public CrmBoundedDomainUi domainUi() {
        return (CrmBoundedDomainUi) super.domainUi();
    }

    private void init() {
        initEntityView();
        var grid = grid();
        VaadinUtils.addColumn(grid, Opportunity::code, "state", "State");
        grid.addColumn(new NumberRenderer<>(Opportunity::potential, VaadinUtils.FORMAT)).setKey("potential").setHeader("Potential")
            .setComparator(o -> o.potential()).setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        VaadinUtils.addColumn(grid, new NumberRenderer<>(e -> HUNDRED.multiply(e.probability()), VaadinUtils.FORMAT), "probability", "Probability (%)")
            .setComparator(o -> o.probability()).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(e -> e.potential().multiply(e.probability()), VaadinUtils.FORMAT)).setKey("forecast").setHeader(
                "Forecast").setComparator(e -> e.potential().multiply(e.probability())).setAutoWidth(true).setResizable(true).setSortable(true)
            .setTextAlign(ColumnTextAlign.END);
    }
}
