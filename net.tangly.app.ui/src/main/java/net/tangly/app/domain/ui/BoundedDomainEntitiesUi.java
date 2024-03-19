/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.app.domain.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.app.domain.model.BoundedDomainEntities;
import net.tangly.core.providers.Provider;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.One2ManyReferencesField;
import net.tangly.ui.components.One2OneField;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoundedDomainEntitiesUi extends BoundedDomainUi<BoundedDomainEntities> {
    private final EntityThreeView entityThreeView;
    private final EntityFourView entityFourView;
    private final DomainView domainView;
    private transient Component currentView;

    public BoundedDomainEntitiesUi(@NotNull BoundedDomainEntities domain) {
        super(domain);
        entityThreeView = new EntityThreeView(BoundedDomainEntities.EntityThree.class, domain, domain.realm().threeEntities(), Mode.VIEW);
        entityFourView = new EntityFourView(BoundedDomainEntities.EntityFour.class, domain, domain.realm().fourEntities(), Mode.EDIT);
        domainView = new DomainView(domain);
        currentView = entityThreeView;
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Entity Three", e -> select(layout, entityThreeView));
        subMenu.addItem("Entity Four", e -> select(layout, entityFourView));
        addAdministration(layout, menuBar, domainView, null);
        select(layout, currentView);
    }

    @Override
    public void select(@NotNull AppLayout layout, Component view) {
        currentView = Objects.isNull(view) ? currentView : view;
        layout.setContent(currentView);
    }

    static class EntityThreeView extends EntityView<BoundedDomainEntities.EntityThree> {
        public EntityThreeView(@NotNull Class<BoundedDomainEntities.EntityThree> entityClass, @NotNull BoundedDomainEntities domain,
                               @NotNull Provider<BoundedDomainEntities.EntityThree> provider, Mode mode) {
            super(entityClass, domain, provider, mode);
            form(new EntityThreeForm(this));
            initEntityView();
        }

        public static class EntityThreeForm extends EntityForm<BoundedDomainEntities.EntityThree, EntityThreeView> {
            public EntityThreeForm(@NotNull EntityThreeView parent) {
                super(parent, BoundedDomainEntities.EntityThree::new);
                init();
            }
        }
    }

    static class EntityFourView extends EntityView<BoundedDomainEntities.EntityFour> {
        public EntityFourView(@NotNull Class<BoundedDomainEntities.EntityFour> entityClass, @NotNull BoundedDomainEntities domain,
                              @NotNull Provider<BoundedDomainEntities.EntityFour> provider, Mode mode) {
            super(entityClass, domain, provider, mode);
            form(new EntityFourForm(this));
            initEntityView();
        }

        @Override
        public BoundedDomainEntities domain() {
            return (BoundedDomainEntities) super.domain();
        }

        public static class EntityFourForm extends EntityForm<BoundedDomainEntities.EntityFour, EntityFourView> {
            private One2OneField<BoundedDomainEntities.EntityThree> one2one;
            private One2ManyReferencesField<BoundedDomainEntities.EntityThree> one2Many;
            private ComboBox<BoundedDomainEntities.ActivityCode> code;

            public EntityFourForm(@NotNull EntityFourView parent) {
                super(parent, BoundedDomainEntities.EntityFour::new);
                init();
            }

            @Override
            protected void init() {
                super.init();
                one2one = new One2OneField<>("one2one", BoundedDomainEntities.EntityThree.class, parent().domain().realm().threeEntities());
                binder().bind(one2one, BoundedDomainEntities.EntityFour::one2one, BoundedDomainEntities.EntityFour::one2one);
                code = ItemForm.createCodeField(parent().registry().find(BoundedDomainEntities.ActivityCode.class).orElseThrow(), "Activity Code");
                binder().bind(code, BoundedDomainEntities.EntityFour::activity, BoundedDomainEntities.EntityFour::activity);
                FormLayout details = new FormLayout();
                details.add(code, one2one);
                details.setColspan(one2one, 2);
                addTabAt("details", details, 1);

                one2Many = new One2ManyReferencesField<>(BoundedDomainEntities.EntityThree.class, parent().domain().realm().threeEntities());
                binder().bind(one2Many, BoundedDomainEntities.EntityFour::one2many, (o, v) -> o.one2many(one2Many.generateModelValue()));
                FormLayout one2many = new FormLayout();
                one2many.add(one2Many);
                addTabAt("one2many", one2many, 2);
            }

            @Override
            public void mode(@NotNull Mode mode) {
                super.mode(mode);
                one2Many.mode(mode);
                code.setReadOnly(mode.readonly());
                one2one.setReadOnly(mode.readonly());
            }
        }
    }
}
