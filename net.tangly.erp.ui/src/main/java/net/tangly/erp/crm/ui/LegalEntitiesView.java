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

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.TypeRegistry;
import net.tangly.core.crm.CrmTags;
import net.tangly.core.crm.LegalEntity;
import net.tangly.core.crm.VcardType;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.One2ManyReferencesField;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Regular CRUD view on the legal entity abstraction. The grid and edition dialog are optimized for usability.
 */
@PageTitle("crm-legal-entities")
class LegalEntitiesView extends EntityView<LegalEntity> {

    /**
     * The form has all properties of an entity through inheritance. Additional properties are defined in the <i>details</i> tab.
     */
    static class LegalEntityForm extends EntityForm<LegalEntity, LegalEntitiesView> {

        public LegalEntityForm(@NotNull LegalEntitiesView parent, @NotNull TypeRegistry registry) {
            super(parent, LegalEntity::new);
            init();
            addTabAt("details", details(), 1);
        }

        private FormLayout details() {
            var site = VaadinUtils.createTextField("Web Site", "website", false);
            var vatNr = VaadinUtils.createTextField("VAT Nr", "vatNr", false);
            FormLayout form = new FormLayout();
            form.add(site, vatNr);

            binder().bind(site, e -> e.site(VcardType.work).orElse(null), (e, v) -> e.site(VcardType.work, v));
            binder().bind(vatNr, LegalEntity::vatNr, LegalEntity::vatNr);
            return form;
        }

        private CustomField<Collection<Employee>> employees() {
            One2ManyReferencesField<Employee> employees =
                new One2ManyReferencesField<>(Employee.class, ProviderView.of(parent().domain().realm().employees(), o -> value().oid() == o.organization().oid()));
            return employees;
        }
    }

    public LegalEntitiesView(@NotNull CrmBoundedDomain domain, @NotNull Mode mode) {
        super(LegalEntity.class, domain, domain.realm().legalEntities(), mode);
        form(new LegalEntityForm(this, domain.registry()));
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    @Override
    protected void init() {
        super.init();
        var grid = grid();
        grid.addColumn(VaadinUtils.linkedInComponentRenderer(CrmTags::linkedInTag, true)).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true);
        grid.addColumn(VaadinUtils.urlComponentRenderer(CrmTags.CRM_SITE_WORK)).setKey("webSite").setHeader("Web Site").setAutoWidth(true);
        grid.addColumn(o -> o.value(CrmTags.CRM_RESPONSIBLE).orElse(null)).setKey("responsible").setHeader("Responsible").setAutoWidth(true).setSortable(true);
    }
}
