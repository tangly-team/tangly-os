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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.core.codes.CodeType;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.InteractionCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.CommentsView;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.InternalEntitiesView;
import net.tangly.ui.components.One2ManyView;
import net.tangly.ui.components.TabsComponent;
import net.tangly.ui.components.TagsView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Regular CRUD view on interactions abstraction. The grid and edition dialog wre optimized for usability.
 */
class InteractionsView extends InternalEntitiesView<Interaction> {
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    private final transient CrmBoundedDomain domain;

    public InteractionsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Interaction.class, mode, domain.realm().interactions(), domain.registry());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        var grid = grid();
        InternalEntitiesView.addQualifiedEntityColumns(grid);
        grid.addColumn(Interaction::code).setKey("state").setHeader("State").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(e -> VaadinUtils.format(e.potential())).setKey("potential").setHeader("Potential").setAutoWidth(true).setResizable(true)
            .setSortable(true).setFrozen(true);
        grid.addColumn(new NumberRenderer<>(e -> HUNDRED.multiply(e.probability()), VaadinUtils.FORMAT)).setKey("probability").setHeader("Probability (%)")
            .setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(e -> e.potential().multiply(e.probability()), VaadinUtils.FORMAT)).setKey("forecast").setHeader("Forecast")
            .setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        addAndExpand(filterCriteria(false, false, InternalEntitiesView::addQualifiedEntityFilters), grid(), gridButtons());
    }

    @Override
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, @NotNull Interaction entity) {
        tabs.add(new Tab("Overview"), createOverallView(mode, entity));
        tabs.add(new Tab("Comments"), new CommentsView(mode, entity));
        tabs.add(new Tab("Tags"), new TagsView(mode, entity, domain.registry()));
        One2ManyView<Activity> activities = new One2ManyView<>(Activity.class, mode, InteractionsView::defineOne2ManyActivities,
            ProviderView.of(ProviderInMemory.of(entity.activities())), new ActivitiesView(domain, mode));
        tabs.add(new Tab("Activities"), activities);
    }

    public static void defineOne2ManyActivities(@NotNull Grid<Activity> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Activity::date).setKey("date").setHeader("Date").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::author).setKey("author").setHeader("Author").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::duration).setKey("duration").setHeader("Duration").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Interaction entity) {
        boolean readonly = mode.readOnly();
        EntityField<Interaction> entityField = new EntityField<>();
        entityField.setReadOnly(readonly);
        TextField potential = VaadinUtils.createTextField("Potential", "potential");
        TextField probability = VaadinUtils.createTextField("Probability", "probability");

        CodeType<InteractionCode> interactionCodeType = CodeType.of(InteractionCode.class);

        Select<InteractionCode> code = new Select<>();
        code.setItemLabelGenerator(InteractionCode::code);
        code.setItems(interactionCodeType.activeCodes());

        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);
        form.add(new HtmlComponent("br"), potential, probability);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(potential, e -> VaadinUtils.format(e.potential()), (e, v) -> e.potential(VaadinUtils.toBigDecimal(v)));
        binder.bind(probability, e -> VaadinUtils.format(e.probability()), (e, v) -> e.probability(VaadinUtils.toBigDecimal(v)));
        binder.readBean(entity);
        return form;
    }

    @Override
    protected Interaction updateOrCreate(Interaction entity) {
        return EntitiesView.updateOrCreate(entity, binder, Interaction::new);
    }
}
