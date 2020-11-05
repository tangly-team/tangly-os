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

import java.io.ByteArrayInputStream;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import net.tangly.bus.crm.CrmBoundedDomain;
import net.tangly.bus.crm.CrmBusinessLogic;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.Subject;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.commons.vaadin.One2OneField;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class SubjectsView extends InternalEntitiesView<Subject> {
    private final CrmBoundedDomain domain;

    public SubjectsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Subject.class, mode, domain.realm().subjects(), domain.registry());
        this.domain = domain;
        initialize();
    }

    @Override
    protected void initialize() {
        InternalEntitiesView.addQualifiedEntityColumns(grid());
        addAndExpand(grid(), gridButtons());
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull Subject entity) {
        boolean readonly = Mode.readOnly(mode);
        EntityField<Subject> entityField = new EntityField<>();
        entityField.setReadOnly(readonly);
        One2OneField<NaturalEntity, NaturalEntitiesView> user = new One2OneField<>("User", new NaturalEntitiesView(domain, mode));

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);
        form.add(new HtmlComponent("br"));
        EmailField gravatarEmail = new EmailField("Avatar Email");
        gravatarEmail.setReadOnly(readonly);
        gravatarEmail.setClearButtonVisible(true);

        Image image = new Image(new StreamResource("avatar.jpg", () -> new ByteArrayInputStream(entity.avatar())), "avatar");
        image.setWidth("200px");
        image.setHeight("200px");
        form.add(gravatarEmail, image, user);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.forField(user).bind(Subject::user, Subject::user);
        binder.forField(gravatarEmail).bind(Subject::gravatarEmail, Subject::gravatarEmail);
        binder.readBean(entity);
        return form;
    }

    @Override
    protected Subject updateOrCreate(Subject entity) {
        return EntitiesView.updateOrCreate(entity, binder, Subject::new);
    }
}
