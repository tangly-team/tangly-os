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
 *
 */

package net.tangly.ui.components;

import net.tangly.core.DateRange;
import net.tangly.core.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Define a canonical filter for entities. The filter provides access to the internal identifier, external identifier, name, text and time interval fields.
 * <p>Subclass the filter to add additional criteria specific to your entity type to the filter.</p>
 *
 * @param <T> Type of the entity which instances shall be filtered
 */
public class EntityFilter<T extends Entity> extends ItemView.ItemFilter<T> {

    private Long oid;
    private String id;
    private String name;
    private String text;
    private DateRange.DateFilter fromRange;
    private DateRange.DateFilter toRange;

    public EntityFilter() {
    }

    public void oid(Long oid) {
        this.oid = oid;
        refresh();
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

    public void fromRange(@NotNull DateRange range) {
        this.fromRange = new DateRange.DateFilter(range);
        refresh();
    }

    public void toRange(@NotNull DateRange range) {
        this.toRange = new DateRange.DateFilter(range);
        refresh();
    }

    @Override
    public boolean test(@NotNull T entity) {
        return (Objects.isNull(oid) || oid.equals(entity.oid())) && matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text) &&
            (Objects.isNull(fromRange) || fromRange.test(entity.from())) && (Objects.isNull(toRange) || toRange.test(entity.to()));
    }
}
