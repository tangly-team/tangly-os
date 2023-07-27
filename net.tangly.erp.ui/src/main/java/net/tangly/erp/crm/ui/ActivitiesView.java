/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.core.codes.CodeType;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.ActivityCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Regular CRUD view on activity abstraction.
 */
class ActivitiesView extends ItemView<Activity> {
    private static final String DATE = "date";
    private static final String CODE = "code";
    private static final String DURATION_IN_MINUTES = "durationInMinutes";
    private static final String AUTHOR = "author";

    private static final String AUTHOR_LABEL = "Author";
    private static final String DETAILS = "details";

    static class ActivityFilter extends ItemFilter<Activity> {
        private LocalDate date;
        private ActivityCode code;
        private String author;

        public ActivityFilter() {
        }

        public void date(LocalDate date) {
            this.date = date;
            dataView().refreshAll();
        }

        public void code(ActivityCode code) {
            this.code = code;
            dataView().refreshAll();
        }

        public void author(String author) {
            this.author = author;
            dataView().refreshAll();
        }

        public boolean test(@NotNull Activity entity) {
            return matches(entity.author(), author);
        }
    }

    static class ActivityForm extends ItemForm<Activity, ActivitiesView> {
        protected DatePicker date;
        protected ComboBox<ActivityCode> code;
        protected IntegerField durationInMinutes;
        protected TextField author;
        protected TextField text;
        protected TextField details;

        public ActivityForm(@NotNull ActivitiesView parent) {
            super(parent);
            init();
        }

        @Override
        public void clear() {

        }

        @Override
        protected Activity createOrUpdateInstance(Activity entity) throws ValidationException {
            return null;
        }

        protected void init() {
            FormLayout form = new FormLayout();
            date = new DatePicker(DATE);
            code = ItemForm.createCodeField(CodeType.of(ActivityCode.class), "code");
            durationInMinutes = new IntegerField(DURATION_IN_MINUTES);
            author = new TextField(AUTHOR);
            text = new TextField(EntityView.TEXT);
            details = new TextField(DETAILS);
            //            fields(Set.of(date, code, durationInMinutes, author, text, details));

            form.add(date, code, durationInMinutes, author, text, details);
            form.setColspan(text, 3);
            form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));

            binder().forField(date).bind(Activity::date, Activity::date);
            binder().forField(code).bind(Activity::code, Activity::code);
            binder().forField(durationInMinutes).bind(Activity::duration, Activity::duration);
            binder().forField(author).bind(Activity::author, Activity::author);
            binder().forField(text).bind(Activity::text, Activity::text);
            binder().forField(details).bind(Activity::details, Activity::details);
        }
    }

    private final transient CrmBoundedDomain domain;

    public ActivitiesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Activity.class, domain, domain.realm().activities(), new ActivityFilter(), mode);
        this.domain = domain;
        form = new ActivityForm(this);
        init();
    }

    @Override
    protected void init() {
        var grid = grid();
        grid.addColumn(Activity::date).setKey(DATE).setHeader("Date").setResizable(true).setSortable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Activity::code).setKey(CODE).setHeader("Code").setResizable(true).setSortable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Activity::duration).setKey(DURATION_IN_MINUTES).setHeader("Duration").setResizable(true).setSortable(true).setFlexGrow(0).setWidth("5em");
        grid.addColumn(Activity::text).setKey(EntityView.TEXT).setHeader(EntityView.TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::details).setKey(AUTHOR).setHeader("Author").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Activity::details).setKey(DETAILS).setHeader("Details").setAutoWidth(true).setResizable(true).setSortable(true);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid().appendHeaderRow();
        if (filter() instanceof ActivityFilter filter) {
            addFilterText(headerRow, AUTHOR, filter::author);
        }
        buildMenu();
    }
}
