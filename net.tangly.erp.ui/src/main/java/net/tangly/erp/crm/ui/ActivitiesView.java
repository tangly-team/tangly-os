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

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.ActivityCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.CodeField;
import net.tangly.ui.components.VaadinUtils;
import net.tangly.ui.grids.GridDecorators;
import org.jetbrains.annotations.NotNull;

/**
 * Regular CRUD view on activities abstraction. The grid and edition dialog wre optimized for usability.
 */
class ActivitiesView extends EntitiesView<Activity> {
    private final transient CrmBoundedDomain domain;
    private Binder<Activity> binder;

    public ActivitiesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Activity.class, mode, domain.realm().activities());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        var grid = grid();
        grid.addColumn(Activity::date).setKey("date").setHeader("Date").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(DATE_WIDTH);
        grid.addColumn(Activity::code).setKey("code").setHeader("Code").setResizable(true).setSortable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Activity::duration).setKey("durationInMinutes").setHeader("Duration").setResizable(true).setSortable(true).setFlexGrow(0)
            .setWidth("5em");
        grid.addColumn(Activity::text).setKey("text").setHeader("Text").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::details).setKey("details").setHeader("Details").setAutoWidth(true).setResizable(true).setSortable(true);
        addAndExpand(filterCriteria(false, false, filters -> {
            filters.addFilter(new GridDecorators.FilterDate<>(filters)).addFilter(
                new GridDecorators.FilterCode<>(filters, (CodeType<ActivityCode>) domain.registry().find(ActivityCode.class).orElseThrow(), Activity::code,
                    "Code"));
        }), grid(), gridButtons());
    }

    @Override
    protected Activity updateOrCreate(Activity entity) {
        return EntitiesView.updateOrCreate(entity, binder, Activity::new);
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Activity entity, FormLayout form) {
        DatePicker date = new DatePicker("Date");
        TextField author = new TextField("Author", "author");
        IntegerField duration = new IntegerField("Duration", "duration");
        CodeField<ActivityCode> code = new CodeField<>(CodeType.of(ActivityCode.class), "code");
        TextArea text = new TextArea("Text", "text");
        TextArea details = new TextArea("Details", "details");

        VaadinUtils.readOnly(operation, author, date, duration, code, text, details);
        form.add(author, date, duration, code);
        form.add(text, 3);
        form.add(details, 3);

        binder = new Binder<>();
        binder.bind(date, Activity::date, Activity::date);
        binder.bind(author, Activity::author, Activity::author);
        binder.bind(duration, Activity::duration, Activity::duration);
        binder.bind(code, Activity::code, Activity::code);
        binder.bind(text, Activity::text, Activity::text);
        binder.bind(details, Activity::details, Activity::details);
        if (entity != null) {
            binder.readBean(entity);
        }
        return form;
    }
}
