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

import java.math.BigDecimal;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.crm.Interaction;
import net.tangly.bus.crm.InteractionCode;
import net.tangly.commons.vaadin.CommentsView;
import net.tangly.commons.vaadin.CrudForm;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.TagsView;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

public class InteractionsView extends CrmEntitiesView<Interaction> {
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public InteractionsView(@NotNull Crm crm) {
        super(crm, Interaction.class, InteractionsView::defineInteractionsGrid, crm.interactions());
    }

    public static void defineInteractionsGrid(@NotNull Grid<Interaction> grid) {
        InternalEntitiesView.defineGrid(grid);
        grid.addColumn(Interaction::state).setKey("state").setHeader("State").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(e -> VaadinUtils.format(e.potential())).setKey("potential").setHeader("Potential").setAutoWidth(true).setResizable(true)
                .setSortable(true).setFrozen(true);
        grid.addColumn(e -> VaadinUtils.format(HUNDRED.multiply(e.probability()))).setKey("probability").setHeader("Probability (%)").setAutoWidth(true).setResizable(true)
                .setSortable(true).setFrozen(true);
        grid.addColumn(e -> VaadinUtils.format(e.potential().multiply(e.probability()))).setKey("forecast").setHeader("Forecast").setAutoWidth(true)
                .setResizable(true).setSortable(true).setFrozen(true);
    }

    @Override
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull CrudForm.Operation operation, Interaction entity) {
        Interaction workedOn = (entity != null) ? entity : create();
        tabs.add(new Tab("Overview"), createOverallView(operation, workedOn));
        tabs.add(new Tab("Comments"), new CommentsView(workedOn));
        tabs.add(new Tab("Tags"), new TagsView(workedOn, crm().tagTypeRegistry()));
    }

    @Override
    protected FormLayout createOverallView(@NotNull Operation operation, @NotNull Interaction entity) {
        boolean readonly = Operation.isReadOnly(operation);
        EntityField entityField = new EntityField();
        TextField potential = VaadinUtils.createTextField("Potential", "potential");
        TextField probability = VaadinUtils.createTextField("Probability", "probability");

        CodeType<InteractionCode> interactionCodeType = CodeType.of(InteractionCode.class);

        Select<InteractionCode> code = new Select<>();
        code.setItemLabelGenerator(InteractionCode::code);
        code.setItems(interactionCodeType.activeCodes());

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField, new HtmlComponent("br"), potential, probability);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(potential, e -> VaadinUtils.format(e.potential()), (e, v) -> e.potential(VaadinUtils.toBigDecimal(v)));
        binder.bind(probability, e -> VaadinUtils.format(e.probability()), (e, v) -> e.probability(VaadinUtils.toBigDecimal(v)));
        binder.readBean(entity);
        return form;
    }

    @Override
    protected Interaction create() {
        return new Interaction();
    }
}
