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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.InteractionCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Regular CRUD view on interactions abstraction. The grid and edition dialog wre optimized for usability.
 */
@PageTitle("crm-interactions")
class InteractionsView extends EntityView<Interaction> {
    static class InteractionForm extends EntityForm<Interaction, InteractionsView> {
        protected InteractionForm(@NotNull InteractionsView parent) {
            super(parent, Interaction::new);
            initEntityForm();
            addTabAt("details", details(), 1);
            var activities = new One2ManyOwnedField<>(new ActivitiesView(parent.domain(), parent.mode()));
            addTabAt("activities", activities, 2);
        }

        private FormLayout details() {
            var potential = new BigDecimalField("Potential", "potential");
            var probability = new BigDecimalField("Probability", "probability");
            ComboBox<InteractionCode> code = ItemForm.createCodeField(CodeType.of(InteractionCode.class), "code");

            FormLayout form = new FormLayout();
            form.add(potential, probability, code);

            binder().bind(potential, Interaction::potential, Interaction::potential);
            binder().bind(probability, Interaction::probability, Interaction::probability);
            binder().forField(code).bind(Interaction::code, Interaction::code);
            return form;
        }
    }

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    public InteractionsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Interaction.class, domain, domain.realm().interactions(), mode);
        form(new InteractionForm(this));
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Interaction::code).setKey("state").setHeader("State").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(e -> VaadinUtils.format(e.potential())).setKey("potential").setHeader("Potential").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(new NumberRenderer<>(e -> HUNDRED.multiply(e.probability()), VaadinUtils.FORMAT)).setKey("probability").setHeader("Probability (%)").setAutoWidth(true)
            .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(e -> e.potential().multiply(e.probability()), VaadinUtils.FORMAT)).setKey("forecast").setHeader("Forecast").setAutoWidth(true)
            .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
    }
}
