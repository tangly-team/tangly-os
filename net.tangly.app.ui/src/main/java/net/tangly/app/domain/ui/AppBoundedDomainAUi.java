/*
 * Copyright 2023-2023 Marcel Baumann
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
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.providers.Provider;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.components.EntityField;
import net.tangly.ui.components.EntityView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppBoundedDomainAUi implements BoundedDomainUi {
    public static final String DOMAIN_NAME = "App-A";

    public abstract static class AppEntityView<T extends HasOid & HasId & HasName & HasTimeInterval & HasText> extends EntityView<T> {
        public abstract static class AppEntityForm<T extends HasOid & HasId & HasName & HasTimeInterval & HasText> extends ItemForm<T> {
            protected Binder<T> binder;
            protected EntityField<T> entity;

            protected AppEntityForm(@NotNull AppEntityView<T> parent) {
                super(parent);
            }

            @Override
            public void fill(T entity) {
                if (entity != null) {
                    binder.readBean(entity);
                }
            }

            @Override
            protected void mode(@NotNull Mode mode) {
                // TODO entity.mode(mode);
            }

            @Override
            protected void clear() {
                entity.clear();
            }

            protected FormLayout initFormFields() {
                FormLayout fieldsLayout = new FormLayout();
                entity = new EntityField<>();
                fieldsLayout.add(entity);
                fieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));
                return fieldsLayout;
            }

            protected void init() {
                FormLayout fieldsLayout = new FormLayout();
                entity = new EntityField<>();
                fieldsLayout.add(entity);
                fieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));
                form().add(fieldsLayout, createButtonsBar());

                binder = new Binder<>(parent.entityClass());
                entity.bind(binder, false);
            }
        }

        protected AppEntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider) {
            super(entityClass, provider, true);
        }

        @Override
        protected void init() {
            addEntityColumns(grid());
            addEntityFilters(grid(), filter());
            initMenu();
        }
    }

    public static class EntityOneView extends AppEntityView<AppBoundedDomainA.EntityOne> {

        public EntityOneView(@NotNull Class<AppBoundedDomainA.EntityOne> entityClass, @NotNull Provider<AppBoundedDomainA.EntityOne> provider) {
            super(entityClass, provider);
            form = new EntityOneView.EntityOneForm(this);
            init();
        }

        public static class EntityOneForm extends AppEntityForm<AppBoundedDomainA.EntityOne> {
            public EntityOneForm(@NotNull EntityOneView parent) {
                super(parent);
                init();
            }

            @Override
            protected AppBoundedDomainA.EntityOne createOrUpdateInstance(AppBoundedDomainA.EntityOne entity) {
                return new AppBoundedDomainA.EntityOne(entity.oid(), entity.id(), entity.name(), entity.from(), entity.to(), entity.text());
            }
        }
    }

    public static class EntityTwoView extends AppEntityView<AppBoundedDomainA.EntityTwo> {
        public EntityTwoView(@NotNull Class<AppBoundedDomainA.EntityTwo> entityClass, @NotNull Provider<AppBoundedDomainA.EntityTwo> provider) {
            super(entityClass, provider);
            form = new EntityTwoForm(this);
            init();
        }

        public static class EntityTwoForm extends AppEntityForm<AppBoundedDomainA.EntityTwo> {
            public EntityTwoForm(@NotNull EntityTwoView parent) {
                super(parent);
                init();
            }

            @Override
            protected AppBoundedDomainA.EntityTwo createOrUpdateInstance(AppBoundedDomainA.EntityTwo entity) {
                return new AppBoundedDomainA.EntityTwo(entity.oid(), entity.id(), entity.name(), entity.from(), entity.to(), entity.text());
            }
        }
    }

    private final AppBoundedDomainA domain;
    private final EntityOneView entityOneView;
    private final EntityTwoView entityTwoView;
    private transient Component currentView;

    public AppBoundedDomainAUi(AppBoundedDomainA domain) {
        this.domain = domain;
        entityOneView = new EntityOneView(AppBoundedDomainA.EntityOne.class, domain.realm().oneEntities());
        entityTwoView = new EntityTwoView(AppBoundedDomainA.EntityTwo.class, domain.realm().twoEntities());
        currentView = entityOneView;
    }

    @Override
    public String name() {
        return domain.name();
    }

    @Override
    public void select(@NotNull AppLayout layout, @NotNull MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(ENTITIES);
        SubMenu subMenu = menuItem.getSubMenu();
        subMenu.addItem("Entity One", e -> select(layout, entityOneView));
        subMenu.addItem("Entity Two", e -> select(layout, entityTwoView));
        select(layout, currentView);
    }

    @Override
    public void select(@NotNull AppLayout layout, Component view) {
        currentView = Objects.isNull(view) ? currentView : view;
        layout.setContent(currentView);
    }
}
