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
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.HasComments;
import net.tangly.core.HasTags;
import net.tangly.core.NamedEntity;
import net.tangly.core.QualifiedEntity;
import net.tangly.core.TypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The entity form provides an interface to all fields of an entity instance. The fields are grouped in tabs.
 * <ul>
 *    <li>The overview panel shows the regular fields of an entity.</li>
 *    <li>The comments panel shows all comments associated with the entity. This panel is a Crud&lt; Comment&gt; instance.</li>
 *    <li>The tags panel shows all tags associated with the entity. This panel is a Crud&lt;Tag&gt; instance.</li>
 * </ul>
 * <p> When a new instance is selected, The entity in the form will be updated. THe associated views shall also be updated.</p>
 */
public final class EntitiesView {
    public static final String OID_WIDTH = "5em";
    public static final String ID_WIDTH = "12em";
    public static final String NAME_WIDTH = "20em";
    public static final String DATE_WIDTH = "10em";

    static <T extends QualifiedEntity> void addQualifiedEntityColumns(Grid<T> grid) {
        grid.addColumn(QualifiedEntity::oid).setKey("oid").setHeader("Oid").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(OID_WIDTH);
        grid.addColumn(QualifiedEntity::id).setKey("id").setHeader("Id").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(ID_WIDTH);
        grid.addColumn(QualifiedEntity::name).setKey("name").setHeader("Name").setResizable(true).setSortable(true).setFlexGrow(0).setWidth(NAME_WIDTH);
// TODO
//        grid.addColumn(new LocalDateRenderer<>(QualifiedEntity::from, DateTimeFormatter.ISO_DATE)).setKey("from").setHeader("From").setResizable(true)
//            .setSortable(true).setFlexGrow(0).setWidth(DATE_WIDTH);
//        grid.addColumn(new LocalDateRenderer<>(QualifiedEntity::to, DateTimeFormatter.ISO_DATE)).setKey("to").setHeader("To").setResizable(true)
//            .setSortable(true).setFlexGrow(0).setWidth(DATE_WIDTH);
    }

    static <T extends HasComments & HasTags> void registerTabs(@NotNull TabSheet tabSheet, @NotNull T entity, @NotNull TypeRegistry registry) {
        // TODO tabSheet.add("Overview", createOverallView(entity));
        tabSheet.add("Comments", new CommentsView(entity));
        tabSheet.add("Tags", new TagsView(entity, registry));
    }

    static <T extends NamedEntity> FormLayout createOverallView(@NotNull T entity) {
        EntityField<T> entityField = new EntityField<>();

        FormLayout form = new FormLayout();
        VaadinUtils.set3ResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);

       Binder<T> binder = new Binder<>();
        entityField.bind(binder);
        if (Objects.nonNull(entity)) {
            binder.readBean(entity);
        }
        return form;
    }
}
