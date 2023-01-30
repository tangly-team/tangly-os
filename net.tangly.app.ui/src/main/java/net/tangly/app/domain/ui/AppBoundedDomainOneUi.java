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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.SingleSelect;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasText;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppBoundedDomainOneUi implements BoundedDomainUi {
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

    public static abstract class AppEntityView<T extends HasId & HasName & HasText> extends EntityView<T> {
        private AppEntityFilter<T> entityFilter;

        public AppEntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider) {
            super(entityClass, provider);
        }

        protected void init() {
            entityFilter = new AppEntityFilter<>(dataView());
            grid().getHeaderRows().clear();
            HeaderRow headerRow = grid().appendHeaderRow();
            addFilter(headerRow, ID, ENTITY_ID_LABEL, entityFilter::id);
            addFilter(headerRow, NAME, ENTITY_NAME_LABEL, entityFilter::name);
            addFilter(headerRow, TEXT, ENTITY_TEXT_LABEL, entityFilter::text);
        }
    }

    public static class EntityOneView extends AppEntityView<AppBoundedDomainOne.EntityOne> {

        public EntityOneView(@NotNull Class<AppBoundedDomainOne.EntityOne> entityClass, @NotNull Provider<AppBoundedDomainOne.EntityOne> provider) {
            super(entityClass, provider);
            init();
        }

        protected void init() {
            var grid = grid();
            grid.addColumn(AppBoundedDomainOne.EntityOne::id).setKey(ID).setHeader(ENTITY_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainOne.EntityOne::name).setKey(NAME).setHeader(ENTITY_NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainOne.EntityOne::text).setKey(TEXT).setHeader(ENTITY_TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            super.init();
        }
    }

    public static class EntityTwoView extends AppEntityView<AppBoundedDomainOne.EntityTwo> {
        private EntityTwoForm form;

        public EntityTwoView(@NotNull Class<AppBoundedDomainOne.EntityTwo> entityClass, @NotNull Provider<AppBoundedDomainOne.EntityTwo> provider) {
            super(entityClass, provider);
            form = new EntityTwoForm(this);
            init();
        }

        public static class EntityTwoForm extends EntityView.EntityForm<AppBoundedDomainOne.EntityTwo> {
            private Binder<AppBoundedDomainOne.EntityTwo> binder;
            TextField id;
            TextField name;
            TextField text;
            Button action;

            public EntityTwoForm(@NotNull EntityTwoView parent) {
                super(parent);
                init();
            }

            @Override
            public void display(AppBoundedDomainOne.EntityTwo entity, Mode mode) {
                mode(mode);
                displayEntity(entity);
                parent.add(form);
            }


            @Override
            public void displayEntity(AppBoundedDomainOne.EntityTwo entity) {
                this.selectedItem = entity;
                clear();
                if (entity != null) {
                    binder.readBean(entity);
                }
            }

            @Override
            public AppBoundedDomainOne.EntityTwo updateEntity() {
                if (Objects.nonNull(selectedItem())) {
                    parent.provider().delete(selectedItem());
                }
                AppBoundedDomainOne.EntityTwo entity = new AppBoundedDomainOne.EntityTwo(id.getValue(), name.getValue(), text.getValue());
                parent.provider().update(entity);
                if (selectedItem() == entity) {
                    parent.dataView().refreshItem(entity);
                } else {
                    parent.dataView().refreshAll();
                }
                discard();
                return entity;
            }

            protected void mode(@NotNull Mode mode) {
                id.setReadOnly(mode.readonly());
                name.setReadOnly(mode.readonly());
                text.setReadOnly(mode.readonly());
                action.setText(mode.readonly() ? "Close" : "Save");
                action.addClickListener(event -> discard());

            }

            protected void clear() {
                id.clear();
                name.clear();
                text.clear();
            }

            protected void init() {
                FormLayout fieldsLayout = new FormLayout();
                id = new TextField(ID);
                name = new TextField(NAME);
                text = new TextField(TEXT);
                fieldsLayout.add(id, name, text);
                fieldsLayout.setColspan(text, 3);
                fieldsLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("320px", 2),
                    new FormLayout.ResponsiveStep("500px", 3));

                form = new VerticalLayout();
                Button cancel = new Button("Cancel");
                cancel.addClickListener(event -> discard());
                Button action = new Button();
                action.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                HorizontalLayout buttons = new HorizontalLayout(cancel, action);
                Binder<AppBoundedDomainOne.EntityTwo> binder = new Binder<>(AppBoundedDomainOne.EntityTwo.class);
                binder.forField(id).bind(AppBoundedDomainOne.EntityTwo::id, null);
                binder.forField(name).bind(AppBoundedDomainOne.EntityTwo::name, null);
                binder.forField(text).bind(AppBoundedDomainOne.EntityTwo::text, null);
                form.add(fieldsLayout, buttons);
            }
        }

        protected void init() {
            var grid = grid();
            grid.addColumn(AppBoundedDomainOne.EntityTwo::id).setKey(ID).setHeader(ENTITY_ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainOne.EntityTwo::name).setKey(NAME).setHeader(ENTITY_NAME_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AppBoundedDomainOne.EntityTwo::text).setKey(TEXT).setHeader(ENTITY_TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            super.init();

            SingleSelect<Grid<AppBoundedDomainOne.EntityTwo>, AppBoundedDomainOne.EntityTwo> selection = grid.asSingleSelect();
            selection.addValueChangeListener(e -> {
                AppBoundedDomainOne.EntityTwo selectedItem = e.getValue();
                form.display(selectedItem, Mode.VIEW);
            });

            GridContextMenu<AppBoundedDomainOne.EntityTwo> menu = grid.addContextMenu();
            menu.addItem(Mode.VIEW_TEXT, event -> form.display(event.getItem().orElse(null), Mode.VIEW));
            menu.add(new Hr());
            menu.addItem(Mode.EDIT_TEXT, event -> form.display(event.getItem().orElse(null), Mode.EDIT));
            menu.addItem(Mode.CREATE_TEXT, event -> form.create(Mode.CREATE));
            menu.addItem(Mode.DUPLICATE_TEXT, event -> form.create(Mode.DUPLICATE));
            menu.addItem(Mode.DELETE_TEXT, event -> form.display(event.getItem().orElse(null), Mode.DELETE));
        }
    }

    public static String DOMAIN_NAME = "App";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TEXT = "text";
    private static final String ENTITY_ID_LABEL = "Id";
    private static final String ENTITY_NAME_LABEL = "Name";
    private static final String ENTITY_TEXT_LABEL = "Text";

    private final AppBoundedDomainOne domain;
    private final EntityOneView entityOneView;
    private final EntityTwoView entityTwoView;
    private transient Component currentView;

    public AppBoundedDomainOneUi(AppBoundedDomainOne domain) {
        this.domain = domain;
        entityOneView = new EntityOneView(AppBoundedDomainOne.EntityOne.class, domain.realm().oneEntities());
        entityTwoView = new EntityTwoView(AppBoundedDomainOne.EntityTwo.class, domain.realm().twoEntities());
        currentView = entityOneView;
    }

    @Override
    public String name() {
        // TODO update BoundedDomain to provide name
        return DOMAIN_NAME;
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
