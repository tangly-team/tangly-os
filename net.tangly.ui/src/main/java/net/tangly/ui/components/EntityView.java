/*
 * Copyright 2023 Marcel Baumann
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
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * The CRUD view for a domain entity. A domain entity has an internal object identifier, external identifier, a name, a temporal validity range and a text. An entity can have
 * optional tags and comments.
 *
 * @param <T> entity to display
 */
public abstract class EntityView<T extends HasOid & HasId & HasName & HasTimeInterval & HasText> extends ItemView<T> {
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

    public static final String ISO_DATE_FORMAT = "YYYY-MM-DD";

    protected EntityView(@NotNull Class<T> entityClass, @NotNull BoundedDomain<?, ?, ?, ?> domain, @NotNull Provider<T> provider, @NotNull Mode mode) {
        super(entityClass, domain, provider, new EntityFilter<>(), mode);
    }

    @Override
    public EntityFilter<T> filter() {
        return (EntityFilter<T>) super.filter();
    }

    @Override
    protected void init() {
        addEntityColumns(grid());
        addEntityFilters(grid(), new EntityFilter<>());
        buildMenu();
    }

    protected void addEntityColumns(Grid<T> grid) {
        grid.addColumn(HasOid::oid).setKey(OID).setHeader(OID_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("5em");
        grid.addColumn(HasId::id).setKey(ID).setHeader(ID_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("12em");
        grid.addColumn(HasName::name).setKey(NAME).setHeader(NAME_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("20em");
        grid.addColumn(new LocalDateRenderer<>(HasTimeInterval::from, ISO_DATE_FORMAT)).setKey(FROM).setHeader(FROM_LABEL).setResizable(true).setSortable(true).setFlexGrow(0)
            .setWidth("10em");
        grid.addColumn(new LocalDateRenderer<>(HasTimeInterval::to, ISO_DATE_FORMAT)).setKey(TO).setHeader(TO_LABEL).setResizable(true).setSortable(true).setFlexGrow(0)
            .setWidth("10em");
        grid.addColumn(HasText::text).setKey(TEXT).setHeader(TEXT_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("30em");
    }

    protected void addEntityFilters(@NotNull Grid<T> grid, @NotNull EntityFilter<T> filter) {
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.getHeaderRows().clear();
        addEntityFilters(grid, headerRow, filter);
    }

    protected void addEntityFilters(@NotNull Grid<T> grid, @NotNull HeaderRow headerRow, @NotNull EntityFilter<T> filter) {
        // TODO add long oid
        addFilterText(grid, headerRow, EntityView.ID, EntityView.ID_LABEL, filter::id);
        addFilterText(grid, headerRow, EntityView.NAME, EntityView.NAME_LABEL, filter::name);
        // TODO add time intervall
        addFilterText(grid, headerRow, EntityView.TEXT, EntityView.TEXT_LABEL, filter::text);
    }

    protected void addFilterText(@NotNull Grid<T> grid, @NotNull HeaderRow headerRow, @NotNull String key, @NotNull String label, @NotNull Consumer<String> attribute) {
        headerRow.getCell(grid.getColumnByKey(key)).setComponent(createFilterHeader(label, attribute));
    }

    protected void addFilterDate(@NotNull Grid<T> grid, @NotNull HeaderRow headerRow, @NotNull String key, @NotNull String label, @NotNull Consumer<LocalDate> attribute) {
        headerRow.getCell(grid.getColumnByKey(key)).setComponent(createDateFilterHeader(label, attribute));
    }
}
