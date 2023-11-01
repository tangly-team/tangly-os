/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.InteractionCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
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
        }

        @Override
        public void init() {
            EntityField<Interaction> entityField = new EntityField<>();

            TextField potential = VaadinUtils.createTextField("Potential", "potential");
            TextField probability = VaadinUtils.createTextField("Probability", "probability");
            ComboBox<InteractionCode> code = ItemForm.createCodeField(CodeType.of(InteractionCode.class), "code");

            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            form.add(entityField, new HtmlComponent("br"), potential, probability);

            entityField.bind(binder(), true);
            binder().bind(potential, e -> VaadinUtils.format(e.potential()), (e, v) -> e.potential(VaadinUtils.toBigDecimal(v)));
            binder().bind(probability, e -> VaadinUtils.format(e.probability()), (e, v) -> e.probability(VaadinUtils.toBigDecimal(v)));
            binder().forField(code).bind(Interaction::code, Interaction::code);
        }


        @Override
        protected Interaction createOrUpdateInstance(Interaction entity) throws ValidationException {
            return null;
        }

        private void registerTabs(@NotNull TabSheet tabSheet, @NotNull Mode mode, @NotNull Interaction entity) {
            // TODO comments and tabe
            //            comments = new CollectionField<>(new CommentsView(parent.domain(), parent.mode()));
            //            tags = new CollectionField<>(new TagsView(parent.domain(), parent.mode()));
            //            One2ManyView<Activity> activities = new One2ManyView<>(Activity.class, mode, InteractionsView::defineOne2ManyActivities,
            //                ProviderView.of(ProviderInMemory.of(entity.activities())), new ActivitiesView(domain(), mode));
            //            tabSheet.add("Activities", activities);
        }

    }

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    public InteractionsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Interaction.class, domain, domain.realm().interactions(), mode);
        init();
    }

    @Override
    protected void init() {
        var grid = grid();
        grid.addColumn(Interaction::code).setKey("state").setHeader("State").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(e -> VaadinUtils.format(e.potential())).setKey("potential").setHeader("Potential").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(new NumberRenderer<>(e -> HUNDRED.multiply(e.probability()), VaadinUtils.FORMAT)).setKey("probability").setHeader("Probability (%)").setAutoWidth(true)
            .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(e -> e.potential().multiply(e.probability()), VaadinUtils.FORMAT)).setKey("forecast").setHeader("Forecast").setAutoWidth(true)
            .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
    }

    public static void defineOne2ManyActivities(@NotNull Grid<Activity> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Activity::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::author).setKey("author").setHeader("Author").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::duration).setKey("duration").setHeader("Duration").setAutoWidth(true).setResizable(true).setSortable(true);
    }

}
