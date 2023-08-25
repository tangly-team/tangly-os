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

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * The view has two ways to handle a list of items.
 * <ul>
 *     <li>The view can be used to edit managed entities. Managed entities have a detailed view, edition, deletion, creation and duplication operationsIt supports all defined
 *     operations based on the mode. This approach is the framework CRUD approach.</li>
 *     <li>The view can be used to edit list of unmanaged entities.Unmanaged entities cannot be created, edited or deleted. It supports adding entities from a list and removing
 *     them from the current one if the mode is not read-only. This approach is the Vaadin user interface field approach.</li>
 * </ul>
 *
 * @param <T> type of the entities in the list.
 */
public class CollectionField<T> extends CustomField<Collection<T>> {
    private final ItemView<T> view;

    public CollectionField(@NotNull ItemView<T> view) {
        this.view = view;
        setHeightFull();
        setWidthFull();
        addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        view.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        add(view);
    }

    public Mode mode() {
        return view.mode();
    }

    public void mode(Mode mode) {
        view.mode(mode);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        view.mode(readOnly ? Mode.LIST : Mode.VIEW);
    }

    @Override
    public void clear() {
        super.clear();
        view.provider(ProviderInMemory.of());
    }

    @Override
    protected Collection<T> generateModelValue() {
        return view.provider().items();
    }

    @Override
    protected void setPresentationValue(Collection<T> items) {
        if (Objects.isNull(items)) {
            view.provider(ProviderInMemory.of());
        } else {
            view.provider(ProviderInMemory.of(items));
        }
    }
}
