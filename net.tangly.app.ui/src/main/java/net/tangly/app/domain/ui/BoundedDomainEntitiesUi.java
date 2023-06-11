/*
 * Copyright 2023-2023 Marcel Baumann
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

package net.tangly.app.domain.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.app.domain.model.BoundedDomainEntities;
import net.tangly.core.providers.Provider;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.CodeField;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.One2OneField;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoundedDomainEntitiesUi implements BoundedDomainUi {
    private final BoundedDomainEntities domain;
    private final EntityThreeView entityThreeView;
    private final EntityFourView entityFourView;
    private final DomainView domainView;
    private transient Component currentView;

    public BoundedDomainEntitiesUi(BoundedDomainEntities domain) {
        this.domain = domain;
        entityThreeView = new EntityThreeView(BoundedDomainEntities.EntityThree.class, domain, domain.realm().threeEntities(), ItemView.Mode.VIEW);
        entityFourView = new EntityFourView(BoundedDomainEntities.EntityFour.class, domain, domain.realm().fourEntities(), ItemView.Mode.EDIT);
        domainView = new DomainView(domain);
        currentView = entityThreeView;
    }

    @Override
    public String name() {
        return domain.name();
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Entity Three", e -> select(layout, entityThreeView));
        subMenu.addItem("Entity Four", e -> select(layout, entityFourView));
        addAdministration(layout, menuBar, domain, domainView, null);
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
            form = new EntityThreeForm(this);
            init();
        }

        public static class EntityThreeForm extends EntityForm<BoundedDomainEntities.EntityThree, EntityThreeView> {
            public EntityThreeForm(@NotNull EntityThreeView parent) {
                super(parent);
                init();
            }

            @Override
            protected BoundedDomainEntities.EntityThree createOrUpdateInstance(BoundedDomainEntities.EntityThree entity) throws ValidationException {
                return createOrUpdateInstance(entity, BoundedDomainEntities.EntityThree::new);
            }
        }
    }

    static class EntityFourView extends EntityView<BoundedDomainEntities.EntityFour> {
        public EntityFourView(@NotNull Class<BoundedDomainEntities.EntityFour> entityClass, @NotNull BoundedDomainEntities domain,
                              @NotNull Provider<BoundedDomainEntities.EntityFour> provider, Mode mode) {
            super(entityClass, domain, provider, mode);
            form = new EntityFourForm(this);
            init();
        }

        @Override
        public BoundedDomainEntities domain() {
            return (BoundedDomainEntities) super.domain();
        }

        public static class EntityFourForm extends EntityForm<BoundedDomainEntities.EntityFour, EntityFourView> {
            private One2OneField<BoundedDomainEntities.EntityThree> one2oneField;

            public EntityFourForm(@NotNull EntityFourView parent) {
                super(parent);
                init();
            }

            @Override
            protected void init() {
                super.init();
                one2oneField = new One2OneField<>("one2one", parent().domain().realm().threeEntities());
                binder().bind(one2oneField, BoundedDomainEntities.EntityFour::one2one, BoundedDomainEntities.EntityFour::one2one);
                CodeField<BoundedDomainEntities.ActivityCode> codeField =
                    new CodeField<>(parent().registry().find(BoundedDomainEntities.ActivityCode.class).orElseThrow(), "Activity Code");
                binder().bind(codeField, BoundedDomainEntities.EntityFour::activity, BoundedDomainEntities.EntityFour::activity);

                FormLayout details = new FormLayout();
                details.add(codeField, one2oneField);
                addTabAt("details", details, 1);

                FormLayout one2many = new FormLayout();
                // TODO one2many ui
                addTabAt("one2many", one2many, 2);
            }

            @Override
            public void mode(@NotNull ItemView.Mode mode) {
                super.mode(mode);
                one2oneField.setReadOnly(mode.readonly());
            }

            @Override
            protected BoundedDomainEntities.EntityFour createOrUpdateInstance(BoundedDomainEntities.EntityFour entity) throws ValidationException {
                return createOrUpdateInstance(entity, BoundedDomainEntities.EntityFour::new);
            }
        }
    }
}
