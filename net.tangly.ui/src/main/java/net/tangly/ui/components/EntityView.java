/*
 * Copyright 2023-2024 Marcel Baumann
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

package net.tangly.ui.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import net.tangly.core.*;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * The CRUD view for a domain entity. A domain entity has an internal object identifier, external identifier, a name, a temporal validity range and a text. An entity can have
 * optional tags and comments. These tags and comments are displayed in separate views.
 *
 * @param <T> entity to display
 */
public class EntityView<T extends Entity> extends ItemView<T> {
    public static final String OID = "oid";
    public static final String OID_LABEL = "OID";

    public static final String ID = "id";
    public static final String ID_LABEL = "ID";

    public static final String NAME = "name";
    public static final String NAME_LABEL = "Name";

    public static final String TEXT = "text";
    public static final String TEXT_LABEL = "Text";

    public static final String FROM = "from";
    public static final String FROM_LABEL = "From";

    public static final String TO = "to";
    public static final String TO_LABEL = "To";

    public static final String DATE = "date";
    public static final String DATE_LABEL = "Date";


    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    public static <T extends Entity> EntityView<T> of(@NotNull Class<T> entityClass, BoundedDomain<?, ?, ?> domain, @NotNull Provider<T> provider, @NotNull Mode mode) {
        EntityView<T> view = new EntityView<>(entityClass, domain, provider, mode);
        view.initEntityView();
        return view;
    }

    public EntityView(@NotNull Class<T> entityClass, BoundedDomain<?, ?, ?> domain, @NotNull Provider<T> provider, @NotNull Mode mode) {
        super(entityClass, domain, provider, new EntityFilter<>(), mode);
    }

    @Override
    public EntityFilter<T> filter() {
        return (EntityFilter<T>) super.filter();
    }

    protected final void initEntityView() {
        addEntityColumns(grid());
        addEntityFilterFields(grid(), filter());
        buildMenu();
    }

    protected void addEntityColumns(Grid<T> grid) {
        grid.addColumn(HasOid::oid).setKey(OID).setHeader(OID_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("4em");
        grid.addColumn(HasId::id).setKey(ID).setHeader(ID_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("4em");
        grid.addColumn(HasName::name).setKey(NAME).setHeader(NAME_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("16em");
        grid.addColumn(new LocalDateRenderer<>(HasDateRange::from, ISO_DATE_FORMAT)).setKey(FROM).setHeader(FROM_LABEL).setResizable(true).setSortable(true).setFlexGrow(0)
            .setWidth("8em");
        grid.addColumn(new LocalDateRenderer<>(HasDateRange::to, ISO_DATE_FORMAT)).setKey(TO).setHeader(TO_LABEL).setResizable(true).setSortable(true).setFlexGrow(0)
            .setWidth("8em");
        grid.addColumn(HasText::text).setKey(TEXT).setHeader(TEXT_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("30em");
    }

    protected void addEntityFilterFields(@NotNull Grid<T> grid, @NotNull EntityFilter<T> filter) {
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.getHeaderRows().clear();
        headerRow.getCell(grid.getColumnByKey(EntityView.OID)).setComponent(createIntegerFilterField(o -> filter.oid(o.longValue())));
        headerRow.getCell(grid.getColumnByKey(EntityView.ID)).setComponent(createTextFilterField(filter::id));
        headerRow.getCell(grid.getColumnByKey(EntityView.NAME)).setComponent(createTextFilterField(filter::name));
        headerRow.getCell(grid.getColumnByKey(EntityView.FROM)).setComponent(createDateRangeField(filter::fromRange));
        headerRow.getCell(grid.getColumnByKey(EntityView.TO)).setComponent(createDateRangeField(filter::toRange));
        headerRow.getCell(grid.getColumnByKey(EntityView.TEXT)).setComponent(createTextFilterField(filter::text));
    }
}
