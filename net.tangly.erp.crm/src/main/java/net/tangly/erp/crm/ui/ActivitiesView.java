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

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.app.Application;
import net.tangly.core.codes.CodeType;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.ActivityCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Regular CRUD view on activity abstraction.
 */
class ActivitiesView extends ItemView<Activity> {
    private static final String CODE = "code";
    private static final String DURATION_IN_MINUTES = "durationInMinutes";
    private static final String AUTHOR = "author";

    private static final String AUTHOR_LABEL = "Author";
    private static final String CODE_LABEL = "Code";

    record CmdRefreshActivities(ActivitiesView view) implements Cmd {
        @Override
        public void execute() {
            List interactions = view.domain().realm().opportunities().items().stream().flatMap(e -> e.activities().stream()).toList();
            view.provider(ProviderInMemory.of(interactions));
        }
    }

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

        @Override
        public boolean test(@NotNull Activity entity) {
            return ItemFilter.matches(entity.author(), author);
        }
    }

    static class ActivityForm extends ItemForm<Activity, ActivitiesView> {
        private DatePicker date;
        private Select<String> author;

        public ActivityForm(@NotNull ActivitiesView parent) {
            super(parent);
            date = new DatePicker(EntityView.DATE_LABEL);
            author = authors();
            addTabAt("details", details(), 0);
            addTabAt("text", textForm(), 1);
        }

        @Override
        public void create() {
            super.create();
            date.setValue(LocalDate.now());
            author.setValue(BoundedDomainUi.username());
        }

        @Override
        public void duplicate(@NotNull Activity entity) {
            super.duplicate(entity);
            date.setValue(LocalDate.now());
            author.setValue(BoundedDomainUi.username());
        }

        @Override
        protected Activity createOrUpdateInstance(Activity entity) throws ValidationException {
            var updatedEntity = Objects.nonNull(entity) ? entity : new Activity();
            binder().writeBean(updatedEntity);
            return updatedEntity;
        }

        private FormLayout details() {
            var form = new FormLayout();
            var code = ItemForm.createCodeField(CodeType.of(ActivityCode.class), "code");
            var durationInMinutes = new IntegerField(DURATION_IN_MINUTES);
            var text = new TextField(EntityView.TEXT);

            form.add(date, code, durationInMinutes, author, text);
            form.setColspan(text, 3);
            form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2),
                new FormLayout.ResponsiveStep("500px", 3));

            binder().forField(date).bind(Activity::date, Activity::date);
            binder().forField(code).bind(Activity::code, Activity::code);
            binder().forField(durationInMinutes).bind(Activity::duration, Activity::duration);
            binder().forField(author).bind(Activity::author, Activity::author);
            binder().forField(text).bind(Activity::text, Activity::text);
            return form;
        }

        private Select<String> authors() {
            Select<String> authors = new Select<>();
            authors.setLabel("Author");
            authors.setItems(Application.instance().tenant("tangly").apps().logic().usersFor(CrmBoundedDomain.DOMAIN));
            return authors;
        }
    }

    public ActivitiesView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode, boolean isViewEmbedded) {
        super(Activity.class, domain, domain.domain().realm().activities(), new ActivityFilter(), mode, isViewEmbedded);
        form(() -> new ActivityForm(this));
        init();
    }

    public ActivitiesView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        this(domain, mode, false);
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    @Override
    protected void buildCrudMenu(Mode mode) {
        if (isViewEmbedded()) {
            super.buildCrudMenu(mode);
        } else {
            menu().addItem(Mode.VIEW_TEXT, event -> event.getItem().ifPresent(o -> form().get().display(o)));
        }
    }

    @Override
    protected void addActions(@NotNull GridContextMenu<Activity> menu) {
        if (!isViewEmbedded()) {
            menu().add(new Hr());
            menu().addItem("RefreshAll", _ -> new CmdRefreshActivities(this).execute());
        }
    }

    private void init() {
        var grid = grid();
        VaadinUtils.addColumn(grid, Activity::date, DATE, DATE_LABEL).setFlexGrow(0).setWidth("10em");
        VaadinUtils.addColumn(grid, Activity::code, CODE, CODE_LABEL).setFlexGrow(0).setWidth("10em");
        VaadinUtils.addColumn(grid, Activity::duration, DURATION_IN_MINUTES, "Duration").setFlexGrow(0).setWidth("5em");
        VaadinUtils.addColumn(grid, Activity::author, AUTHOR, AUTHOR_LABEL);
        VaadinUtils.addColumn(grid, Activity::text, TEXT, TEXT_LABEL);
    }
}
