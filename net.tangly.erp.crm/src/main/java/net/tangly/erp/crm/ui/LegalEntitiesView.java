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

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.crm.domain.CrmTags;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.domain.VcardType;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.components.*;
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
    static class LegalEntityForm extends MutableEntityForm<LegalEntity, LegalEntitiesView> {

        public LegalEntityForm(@NotNull LegalEntitiesView view) {
            super(view, LegalEntity::new);
            initEntityForm();
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
            return new One2ManyReferencesField<>(Employee.class,
                ProviderView.of(view().domain().realm().employees(), o -> value().oid() == o.organization().oid()));
        }
    }

    public LegalEntitiesView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(LegalEntity.class, domain, domain.domain().realm().legalEntities(), mode);
        form(() -> new LegalEntityForm(this));
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return (CrmBoundedDomain) super.domain();
    }

    private void init() {
        initEntityView();
        var grid = grid();
        VaadinUtils.addColumn(grid, VaadinUtils.linkedInComponentRenderer(CrmTags::linkedInTag, true), "linkedIn", "LinkedIn");
        VaadinUtils.addColumn(grid, o -> o.value(CrmTags.CRM_RESPONSIBLE).orElse(null), "responsible", "Responsible");
        VaadinUtils.addColumn(grid, zefixComponentRenderer(), "zefix", "Zefix");
        VaadinUtils.addColumn(grid, VaadinUtils.urlComponentRenderer(CrmTags.CRM_SITE_WORK), "website", "Web Site");
    }

    public static <T extends LegalEntity> ComponentRenderer<Anchor, T> zefixComponentRenderer() {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            anchor.setText(item.id());
            if ((item.id() != null) && item.id().startsWith("CHE-")) {
                anchor.setHref(LegalEntity.organizationZefixUrl(item));
                anchor.setTarget("_blank");
            }
            return anchor;
        });
    }
}
