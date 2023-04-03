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
import com.vaadin.flow.component.menubar.MenuBar;
import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.TypeRegistry;
import net.tangly.core.providers.Provider;
import net.tangly.ui.app.domain.BoundedDomainUi;
import net.tangly.ui.app.domain.DomainView;
import net.tangly.ui.components.EntityForm;
import net.tangly.ui.components.EntityView;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AppBoundedDomainBUi implements BoundedDomainUi {
    public static final String DOMAIN_NAME = "App-B";

    public AppBoundedDomainBUi(AppBoundedDomainB domain) {
        this.domain = domain;
        entityThreeView = new EntityThreeView(AppBoundedDomainB.EntityThree.class, domain.realm().threeEntities(), domain.registry());
        entityFourView = new EntityFourView(AppBoundedDomainB.EntityFour.class, domain.realm().fourEntities(), domain.registry());
        domainView = new DomainView(domain);
        currentView = entityThreeView;
    }

    abstract static class AppEntityView<T extends HasOid & HasId & HasName & HasTimeInterval & HasText & HasTags & HasComments> extends EntityView<T> {
        private final TypeRegistry registry;

        static class AppEntityFilter<T extends HasOid & HasId & HasTimeInterval & HasName & HasText> extends ItemFilter<T> {
            private Long oid;
            private String id;
            private String name;
            private String text;

            public AppEntityFilter() {
            }

            public void oid(Long oid) {
                this.oid = oid;
                dataView().refreshAll();
            }

            public void id(String id) {
                this.id = id;
                dataView().refreshAll();
            }

            public void name(String name) {
                this.name = name;
                dataView().refreshAll();
            }

            public void text(String text) {
                this.text = text;
                dataView().refreshAll();
            }

            public boolean test(@NotNull T entity) {
                return (Objects.isNull(oid) || (entity.oid() == oid)) && matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text);
            }
        }

        public abstract static class AppEntityForm<T extends HasOid & HasId & HasTimeInterval & HasName & HasText & HasTags & HasComments> extends EntityForm<T> {

            protected AppEntityForm(@NotNull AppEntityView<T> parent, @NotNull TypeRegistry registry) {
                super(parent, registry);
            }
        }

        protected AppEntityView(@NotNull Class<T> entityClass, @NotNull Provider<T> provider, @NotNull TypeRegistry registry) {
            super(entityClass, provider, true);
            this.registry = registry;
        }

        protected TypeRegistry registry() {
            return registry;
        }

        @Override
        protected void init() {
            addEntityColumns(grid());
            addEntityFilters(grid(), filter());
            initMenu();
        }
    }

    public static class EntityThreeView extends AppEntityView<AppBoundedDomainB.EntityThree> {
        public EntityThreeView(@NotNull Class<AppBoundedDomainB.EntityThree> entityClass, @NotNull Provider<AppBoundedDomainB.EntityThree> provider, @NotNull TypeRegistry registry) {
            super(entityClass, provider, registry);
            form = new EntityThreeForm(this, registry);
            init();
        }

        public static class EntityThreeForm extends AppEntityForm<AppBoundedDomainB.EntityThree> {
            public EntityThreeForm(@NotNull EntityThreeView parent, @NotNull TypeRegistry registry) {
                super(parent, registry);
                init();
            }

            @Override
            protected AppBoundedDomainB.EntityThree createOrUpdateInstance(AppBoundedDomainB.EntityThree entity) {
                return new AppBoundedDomainB.EntityThree(entity.oid(), entity.id(), entity.name(), entity.from(), entity.to(), entity.text());
            }
        }
    }

    public static class EntityFourView extends AppEntityView<AppBoundedDomainB.EntityFour> {
        public static class EntityFourForm extends AppEntityForm<AppBoundedDomainB.EntityFour> {
            public EntityFourForm(@NotNull EntityFourView parent, @NotNull TypeRegistry registry) {
                super(parent, registry);
                init();
            }

            @Override
            protected AppBoundedDomainB.EntityFour createOrUpdateInstance(AppBoundedDomainB.EntityFour entity) {
                return new AppBoundedDomainB.EntityFour(entity.oid(), entity.id(), entity.name(), entity.from(), entity.to(), entity.text());
            }
        }

        public EntityFourView(@NotNull Class<AppBoundedDomainB.EntityFour> entityClass, @NotNull Provider<AppBoundedDomainB.EntityFour> provider, @NotNull TypeRegistry registry) {
            super(entityClass, provider, registry);
            form = new EntityFourForm(this, registry);
            init();
        }
    }

    private final AppBoundedDomainB domain;
    private final EntityThreeView entityThreeView;
    private final EntityFourView entityFourView;
    private final DomainView domainView;
    private transient Component currentView;

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
}
