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
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.Provider;
import net.tangly.ui.components.CommentsView;
import net.tangly.ui.components.EntityView;
import net.tangly.ui.components.TagsView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppBoundedDomainBUi implements BoundedDomainUi {
    private static final String OID = "oid";
    private static final String FROM = "from";
    private static final String TO = "to";

    public static String DOMAIN_NAME = "App-B";
    private static final String ENTITY_OID_LABEL = "Id";
    private static final String ID = "id";
    private static final String NAME = "name";
    public AppBoundedDomainBUi(AppBoundedDomainB domain) {
        this.domain = domain;
        entityThreeView = new EntityThreeView(AppBoundedDomainB.EntityThree.class, domain.realm().oneEntities(), domain.registry());
        entityFourView = new EntityFourView(AppBoundedDomainB.EntityFour.class, domain.realm().twoEntities(), domain.registry());
        currentView = entityThreeView;
    }

    static abstract class AppEntityView<T extends HasOid & HasId & HasName & HasText & HasTimeInterval & HasTags & HasComments> extends EntityView<T> {
        private final TypeRegistry registry;

        public AppEntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider, @NotNull TypeRegistry registry) {
            super(entityClass, provider);
            this.registry = registry;
        }

        protected TypeRegistry registry() {
            return registry;
        }
        private AppEntityFilter<T> entityFilter;

        @Override
        protected void init() {
            entityFilter = new AppEntityFilter<>(dataView());
            grid().getHeaderRows().clear();
            HeaderRow headerRow = grid().appendHeaderRow();
            // TODO addFilter(headerRow, OID, ENTITY_OID_LABEL, entityFilter::oid);
            addFilter(headerRow, ID, ENTITY_ID_LABEL, entityFilter::id);
            addFilter(headerRow, NAME, ENTITY_NAME_LABEL, entityFilter::name);
            addFilter(headerRow, TEXT, ENTITY_TEXT_LABEL, entityFilter::text);
            initMenu();
        }

        static class AppEntityFilter<T extends HasOid & HasId & HasName & HasText> {
            private final GridListDataView<T> dataView;
            private Long oid;
            private String id;
            private String name;
            private String text;

            public AppEntityFilter(@NotNull GridListDataView<T> dataView) {
                this.dataView = dataView;
                this.dataView.addFilter(this::test);
            }

            public void oid(Long oid) {
                this.oid = oid;
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
                return (Objects.isNull(oid) || (entity.oid() == oid)) && matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text);
            }

            private boolean matches(String value, String searchTerm) {
                boolean searchTermUndefined = (searchTerm == null) || (searchTerm.isBlank());
                return searchTermUndefined || ((value == null) || value.toLowerCase().contains(searchTerm.toLowerCase()));
            }
        }

        public static abstract class AppEntityForm<T extends HasOid & HasId & HasName & HasText & HasTimeInterval & HasTags & HasComments> extends EntityForm<T> {
            protected TagsView tagsView;
            protected CommentsView commentsView;
            protected Binder<T> binder;
            protected IntegerField oid;
            protected TextField id;
            protected TextField name;
            protected DatePicker from;
            protected DatePicker to;
            protected TextField text;

            public AppEntityForm(@NotNull AppEntityView<T> parent) {
                super(parent);
            }

            @Override
            public void fill(T entity) {
                if (entity != null) {
                    binder.readBean(entity);
                    tagsView.fill(entity);
                    commentsView.fill(entity);
                }
            }

            @Override
            protected void mode(Mode mode) {
                oid.setReadOnly(mode.readonly());
                id.setReadOnly(mode.readonly());
                name.setReadOnly(mode.readonly());
                from.setReadOnly(mode.readonly());
                to.setReadOnly(mode.readonly());
                text.setReadOnly(mode.readonly());
            }

            @Override
            protected void clear() {
                oid.clear();
                id.clear();
                name.clear();
                from.clear();
                to.clear();
                text.clear();
            }

            protected TabSheet initEntityFields() {
                FormLayout fieldsLayout = new FormLayout();
                oid = new IntegerField(OID);
                id = new TextField(ID);
                name = new TextField(NAME);
                from = new DatePicker(FROM);
                to = new DatePicker(TO);
                text = new TextField(TEXT);
                fieldsLayout.add(oid, id, name, from, to, text);
                fieldsLayout.setColspan(text, 3);
                fieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));

                binder = new Binder<>(parent.entityClass());
                //  TODO              binder.forField(oid).bind(AppBoundedDomainB.EntityThree::oid, null);
                binder.forField(id).bind(T::id, null);
                binder.forField(from).bind(T::from, null);
                binder.forField(to).bind(T::to, null);
                binder.forField(name).bind(T::name, null);
                binder.forField(text).bind(T::text, null);

                tagsView = new TagsView(null, null);
                commentsView = new CommentsView(null);
                TabSheet tabSheet = new TabSheet();
                tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);
                tabSheet.add("entity", new Div(fieldsLayout));
                tabSheet.add("tags", new Div(tagsView));
                tabSheet.add("comments", new Div(commentsView));
                return tabSheet;
            }

            protected abstract void init();
        }
    }
    private static final String TEXT = "text";

    public static class EntityThreeView extends AppEntityView<AppBoundedDomainB.EntityThree> {

        public EntityThreeView(@NotNull Class<AppBoundedDomainB.EntityThree> entityClass, @NotNull Provider<AppBoundedDomainB.EntityThree> provider, @NotNull TypeRegistry registry) {
            super(entityClass, provider, registry);
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
                TabSheet fieldsLayout = initEntityFields();
                form().add(fieldsLayout, createButtonsBar());

            }

            @Override
            protected AppBoundedDomainB.EntityThree createOrUpdateInstance(AppBoundedDomainB.EntityThree entity) {
                // TODO
                return new AppBoundedDomainB.EntityThree(0, id.getValue(), name.getValue(), text.getValue(), null, null);

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
    private static final String ENTITY_ID_LABEL = "Id";
    private static final String ENTITY_NAME_LABEL = "Name";
    private static final String ENTITY_TEXT_LABEL = "Text";

    private final AppBoundedDomainB domain;
    private final EntityThreeView entityThreeView;
    private final EntityFourView entityFourView;
    private transient Component currentView;

    public static class EntityFourView extends AppEntityView<AppBoundedDomainB.EntityFour> {
        public EntityFourView(@NotNull Class<AppBoundedDomainB.EntityFour> entityClass, @NotNull Provider<AppBoundedDomainB.EntityFour> provider, @NotNull TypeRegistry registry) {
            super(entityClass, provider, registry);
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
                TabSheet fieldsLayout = initEntityFields();
                form().add(fieldsLayout, createButtonsBar());
            }

            @Override
            protected AppBoundedDomainB.EntityFour createOrUpdateInstance(AppBoundedDomainB.EntityFour entity) {
                return new AppBoundedDomainB.EntityFour(0, id.getValue(), name.getValue(), text.getValue(), null, null);
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
