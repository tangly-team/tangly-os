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

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.crm.Activity;
import net.tangly.bus.crm.ActivityCode;
import net.tangly.bus.crm.Subject;
import net.tangly.commons.vaadin.CommentsView;
import net.tangly.commons.vaadin.CrudActionsListener;
import net.tangly.commons.vaadin.CrudForm;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.TagsView;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

public class ActivitiesView extends CrmEntitiesView<Activity> {
    public ActivitiesView(@NotNull Crm crm, @NotNull Mode mode) {
        super(crm, Activity.class, mode, crm.activities());
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Grid<Activity> grid = grid();
        grid.addColumn(Activity::durationInMinutes).setKey("durationInMinutes").setHeader("Duration (')").setAutoWidth(true).setResizable(true)
                .setSortable(true).setFrozen(true);
        grid.addColumn(Activity::code).setKey("code").setHeader("Code").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
    }

    @Override
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, Activity entity) {
        Activity workedOn = (entity != null) ? entity : create();
        tabs.add(new Tab("Overview"), createOverallView(mode, workedOn));
        tabs.add(new Tab("Comments"), new CommentsView(mode, workedOn));
        tabs.add(new Tab("Tags"), new TagsView(mode, workedOn, crm().tagTypeRegistry()));
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Activity entity) {
        String username = (String) VaadinUtils.getAttribute(this, "username");
        Optional<Subject> myself = crm().subjects().findBy(Subject::id, username);
        // TODO: wait on Jetty 10.0 which do not include Jakarta Mail
        //        ImapSession imapSession = new ImapSession(myself.get().gmailUsername(), myself.get().gmailPassword());

        boolean readonly = Mode.readOnly(mode);
        EntityField entityField = new EntityField();
        entityField.setReadOnly(readonly);
        IntegerField durationInMinutes = new IntegerField("Duration in Minutes", "duration (minutes)");

        CodeType<ActivityCode> activityCodeType = CodeType.of(ActivityCode.class);
        Select<ActivityCode> code = new Select<>();
        code.setItemLabelGenerator(ActivityCode::code);
        code.setItems(activityCodeType.activeCodes());

        TextArea email = new TextArea("Email Text");
        email.setReadOnly(true);
        email.getStyle().set("minHeight", "10em");
        Select<String> emails = new Select<>();
        emails.setEmptySelectionAllowed(true);
        List<String> emailIds = Activity.emailIds(entity.details());
        emails.addValueChangeListener(event -> {
            //            List<Object> multiparts = imapSession.selectTextPartsFrom((String)event.getValue());
            //            if (!multiparts.isEmpty()) {
            //                email.setValue((String) multiparts.get(0));
            //            }
        });
        if (!emailIds.isEmpty()) {
            emails.setItems(emailIds);
        } else {
            emails.setEnabled(false);
            email.setEnabled(false);
        }

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(entityField, new HtmlComponent("br"), code, durationInMinutes, new HtmlComponent("br"), emails, new HtmlComponent("br"));
        form.add(email, 3);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(durationInMinutes, Activity::durationInMinutes, Activity::durationInMinutes);
        binder.bind(code, Activity::code, Activity::code);
        binder.readBean(entity);
        return form;
    }

    @Override
    protected Activity create() {
        return new Activity();
    }
}
