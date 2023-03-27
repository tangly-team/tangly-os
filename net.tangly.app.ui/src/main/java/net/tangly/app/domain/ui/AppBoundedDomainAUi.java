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
 */

package net.tangly.app.domain.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasText;
import net.tangly.core.providers.Provider;
import net.tangly.ui.components.EntityView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppBoundedDomainAUi implements BoundedDomainUi {
    public static abstract class AppEntityView<T extends HasId & HasName & HasText> extends EntityView<T> {
        static class AppEntityFilter<T extends HasId & HasName & HasText> extends EntityView.EntityFilter<T>{
            private String id;
            private String name;
            private String text;

            public AppEntityFilter(@NotNull GridListDataView<T> dataView) {
                super(dataView);
            }

            public void id(String id) {
                this.id = id;
                refresh();
            }

            public void name(String name) {
                this.name = name;
                refresh();
            }

            public void text(String text) {
                this.text = text;
                refresh();
            }

            @Override
            public boolean test(@NotNull T entity) {
                return matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text);
            }

        }

        public static abstract class AppEntityForm<T extends HasId & HasName & HasText> extends EntityForm<T> {
            protected Binder<T> binder;
            protected TextField id;
            protected TextField name;
            protected TextField text;

            public AppEntityForm(@NotNull AppEntityView<T> parent) {
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
                id.setReadOnly(mode.readonly());
                name.setReadOnly(mode.readonly());
                text.setReadOnly(mode.readonly());
            }

            @Override
            protected void clear() {
                id.clear();
                name.clear();
                text.clear();
            }

            protected FormLayout initFormFields() {
                FormLayout fieldsLayout = new FormLayout();
                id = new TextField(ID);
                name = new TextField(NAME);
                text = new TextField(TEXT);
                fieldsLayout.add(id, name, text);
                fieldsLayout.setColspan(text, 3);
                fieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));
                return fieldsLayout;
            }

            protected abstract void init();

        }

        private AppEntityFilter<T> entityFilter;

        public AppEntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider) {
            super(entityClass, provider);
        }

        @Override
        protected void init() {
            entityFilter = new AppEntityFilter<>(dataView());
            grid().getHeaderRows().clear();
            HeaderRow headerRow = grid().appendHeaderRow();
            addFilter(headerRow, ID, ENTITY_ID_LABEL, entityFilter::id);
            addFilter(headerRow, NAME, ENTITY_NAME_LABEL, entityFilter::name);
            addFilter(headerRow, TEXT, ENTITY_TEXT_LABEL, entityFilter::text);
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
            protected void init() {
                FormLayout fieldsLayout = initFormFields();
                form().add(fieldsLayout, createButtonsBar());
                binder = new Binder<>(AppBoundedDomainA.EntityOne.class);
                binder.forField(id).bind(AppBoundedDomainA.EntityOne::id, null);
                binder.forField(name).bind(AppBoundedDomainA.EntityOne::name, null);
                binder.forField(text).bind(AppBoundedDomainA.EntityOne::text, null);
            }

            @Override
            protected AppBoundedDomainA.EntityOne createOrUpdateInstance(AppBoundedDomainA.EntityOne entity) {
                return new AppBoundedDomainA.EntityOne(id.getValue(), name.getValue(), text.getValue());
            }
        }

        protected void init() {
            var grid = grid();
            grid.addColumn(AppBoundedDomainA.EntityOne::id).setKey(ID).setHeader(ENTITY_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainA.EntityOne::name).setKey(NAME).setHeader(ENTITY_NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainA.EntityOne::text).setKey(TEXT).setHeader(ENTITY_TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            super.init();
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
            protected void init() {
                FormLayout fieldsLayout = initFormFields();
                form().add(fieldsLayout, createButtonsBar());

                binder = new Binder<>(AppBoundedDomainA.EntityTwo.class);
                binder.forField(id).bind(AppBoundedDomainA.EntityTwo::id, null);
                binder.forField(name).bind(AppBoundedDomainA.EntityTwo::name, null);
                binder.forField(text).bind(AppBoundedDomainA.EntityTwo::text, null);
            }

            @Override
            protected AppBoundedDomainA.EntityTwo createOrUpdateInstance(AppBoundedDomainA.EntityTwo entity) {
                return new AppBoundedDomainA.EntityTwo(id.getValue(), name.getValue(), text.getValue());
            }
        }

        @Override
        protected void init() {
            var grid = grid();
            grid.addColumn(AppBoundedDomainA.EntityTwo::id).setKey(ID).setHeader(ENTITY_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainA.EntityTwo::name).setKey(NAME).setHeader(ENTITY_NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainA.EntityTwo::text).setKey(TEXT).setHeader(ENTITY_TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            super.init();
        }
    }

    public static String DOMAIN_NAME = "App-A";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String ENTITY_ID_LABEL = "Id";
    private static final String ENTITY_NAME_LABEL = "Name";
    private static final String ENTITY_TEXT_LABEL = "Text";

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
