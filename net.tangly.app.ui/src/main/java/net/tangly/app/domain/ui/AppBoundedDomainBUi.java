/*
 * Copyright 2023 Marcel Baumann
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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppBoundedDomainBUi implements BoundedDomainUi {
    public static abstract class AppEntityView<T extends HasId & HasName & HasText> extends EntityView<T> {
        static class AppEntityFilter<T extends HasId & HasName & HasText> {
            private final GridListDataView<T> dataView;
            private String id;
            private String name;
            private String text;

            public AppEntityFilter(@NotNull GridListDataView<T> dataView) {
                this.dataView = dataView;
                this.dataView.addFilter(this::test);
            }

            public void id(String id) {
                this.id = id;
                this.dataView.refreshAll();
            }

            public void name(String name) {
                this.name = name;
                this.dataView.refreshAll();
            }

            public void text(String text) {
                this.text = text;
                this.dataView.refreshAll();
            }

            public boolean test(@NotNull T entity) {
                return matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text);
            }

            private boolean matches(String value, String searchTerm) {
                boolean searchTermUndefined = (searchTerm == null) || (searchTerm.isBlank());
                return searchTermUndefined || ((value == null) || value.toLowerCase().contains(searchTerm.toLowerCase()));
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
            public void fillForm(T entity) {
                if (entity != null) {
                    binder.readBean(entity);
                }
            }

            @Override
            public T updateEntity() {
                if (Objects.nonNull(selectedItem())) {
                    parent.provider().delete(selectedItem());
                }
                T entity = createInstance();
                parent.provider().update(entity);
                parent.dataView().refreshAll();
                cancel();
                return entity;
            }

            @Override
            protected void nameActionButton(@NotNull Mode mode) {
                super.nameActionButton(mode);
                id.setReadOnly(mode.readonly());
                name.setReadOnly(mode.readonly());
                text.setReadOnly(mode.readonly());
            }

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

            protected abstract T createInstance();
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

    public static class EntityThreeView extends AppEntityView<AppBoundedDomainB.EntityThree> {

        public EntityThreeView(@NotNull Class<AppBoundedDomainB.EntityThree> entityClass, @NotNull Provider<AppBoundedDomainB.EntityThree> provider) {
            super(entityClass, provider);
            form = new EntityThreeForm(this);
            init();
        }

        public static class EntityThreeForm extends AppEntityForm<AppBoundedDomainB.EntityThree> {
            public EntityThreeForm(@NotNull EntityThreeView parent) {
                super(parent);
                init();
            }

            @Override
            protected void init() {
                FormLayout fieldsLayout = initFormFields();
                form().add(fieldsLayout, createButtonsBar());
                binder = new Binder<>(AppBoundedDomainB.EntityThree.class);
                binder.forField(id).bind(AppBoundedDomainB.EntityThree::id, null);
                binder.forField(name).bind(AppBoundedDomainB.EntityThree::name, null);
                binder.forField(text).bind(AppBoundedDomainB.EntityThree::text, null);
            }

            @Override
            protected AppBoundedDomainB.EntityThree createInstance() {
                return new AppBoundedDomainB.EntityThree(id.getValue(), name.getValue(), text.getValue());

            }
        }

        protected void init() {
            var grid = grid();
            grid.addColumn(AppBoundedDomainB.EntityThree::id).setKey(ID).setHeader(ENTITY_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainB.EntityThree::name).setKey(NAME).setHeader(ENTITY_NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainB.EntityThree::text).setKey(TEXT).setHeader(ENTITY_TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            super.init();
        }
    }

    public static class EntityFourView extends AppEntityView<AppBoundedDomainB.EntityFour> {
        public EntityFourView(@NotNull Class<AppBoundedDomainB.EntityFour> entityClass, @NotNull Provider<AppBoundedDomainB.EntityFour> provider) {
            super(entityClass, provider);
            form = new EntityFourForm(this);
            init();
        }

        public static class EntityFourForm extends AppEntityForm<AppBoundedDomainB.EntityFour> {
            public EntityFourForm(@NotNull EntityFourView parent) {
                super(parent);
                init();
            }


            @Override
            protected void init() {
                FormLayout fieldsLayout = initFormFields();
                form().add(fieldsLayout, createButtonsBar());

                binder = new Binder<>(AppBoundedDomainB.EntityFour.class);
                binder.forField(id).bind(AppBoundedDomainB.EntityFour::id, null);
                binder.forField(name).bind(AppBoundedDomainB.EntityFour::name, null);
                binder.forField(text).bind(AppBoundedDomainB.EntityFour::text, null);
            }

            @Override
            protected AppBoundedDomainB.EntityFour createInstance() {
                return new AppBoundedDomainB.EntityFour(id.getValue(), name.getValue(), text.getValue());
            }
        }

        protected void init() {
            var grid = grid();
            grid.addColumn(AppBoundedDomainB.EntityFour::id).setKey(ID).setHeader(ENTITY_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainB.EntityFour::name).setKey(NAME).setHeader(ENTITY_NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainB.EntityFour::text).setKey(TEXT).setHeader(ENTITY_TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            super.init();
        }
    }

    public static String DOMAIN_NAME = "App-B";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String ENTITY_ID_LABEL = "Id";
    private static final String ENTITY_NAME_LABEL = "Name";
    private static final String ENTITY_TEXT_LABEL = "Text";

    private final AppBoundedDomainB domain;
    private final EntityThreeView entityThreeView;
    private final EntityFourView entityFourView;
    private transient Component currentView;

    public AppBoundedDomainBUi(AppBoundedDomainB domain) {
        this.domain = domain;
        entityThreeView = new EntityThreeView(AppBoundedDomainB.EntityThree.class, domain.realm().oneEntities());
        entityFourView = new EntityFourView(AppBoundedDomainB.EntityFour.class, domain.realm().twoEntities());
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
        select(layout, currentView);
    }

    @Override
    public void select(@NotNull AppLayout layout, Component view) {
        currentView = Objects.isNull(view) ? currentView : view;
        layout.setContent(currentView);
    }
}
