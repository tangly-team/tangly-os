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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

class SubjectsView extends EntityView<Subject> {
    static class SubjectForm extends EntityForm<Subject, SubjectsView> {
        public SubjectForm(@NotNull SubjectsView parent) {
            super(parent, Subject::new);
        }

        public void init() {
            EntityField<Subject> entityField = new EntityField<>();
            //                        entityField.setReadOnly(readonly);
            //            //            One2OneField<NaturalEntity> user = new One2OneField<>("User", new NaturalEntitiesView(domain, mode));

            FormLayout form = new FormLayout();
            VaadinUtils.set3ResponsiveSteps(form);
            //            entityField.addEntityComponentsTo(form);
            form.add(new HtmlComponent("br"));
            EmailField gravatarEmail = new EmailField("Avatar Email");
            //            gravatarEmail.setReadOnly(readonly);
            gravatarEmail.setClearButtonVisible(true);

            //            Image image = new Image(new StreamResource("avatar.jpg", () -> new ByteArrayInputStream(entity.avatar())), "avatar");
            //            image.setWidth("200px");
            //            image.setHeight("200px");
            //            form.add(gravatarEmail, image, user);

            //            entityField.bind(binder());
            //            binder().forField(user).bind(Subject::user, Subject::user);
            //            binder().forField(gravatarEmail).bind(Subject::gravatarEmail, Subject::gravatarEmail);
        }

        @Override
        protected Subject createOrUpdateInstance(Subject entity) throws ValidationException {
            return null;
        }
    }

    private final transient CrmBoundedDomain domain;

    public SubjectsView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(Subject.class, domain, domain.realm().subjects(), mode);
        this.domain = domain;
        initEntityView();
    }

    @Override
    protected void initEntityView() {
    }

}
