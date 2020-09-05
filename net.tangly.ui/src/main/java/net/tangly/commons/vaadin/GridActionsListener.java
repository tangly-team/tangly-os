/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.util.function.Consumer;

import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.providers.Provider;

/**
 * Provides default actions for the grid view.
 * @param <T> type of items displayed in the grid.
 */
public class GridActionsListener<T> implements CrudActionsListener<T> {
    private final Provider<T> provider;
    private final DataProvider<T, ?> dataProvider;
    private final Consumer<T> selection;

    public GridActionsListener(DataProvider<T, ?> dataProvider, Consumer<T> selection) {
        this(null, dataProvider, selection);
    }

    public GridActionsListener(Provider<T> provider, DataProvider<T, ?> dataProvider, Consumer<T> selection) {
        this.provider = provider;
        this.dataProvider = dataProvider;
        this.selection = selection;
    }

    @Override
    public void entityAdded(T entity) {
        if (provider != null) {
            provider.update(entity);
        }
        dataProvider.refreshAll();
        selection.accept(entity);
    }

    @Override
    public void entityDeleted(T entity) {
        if (provider != null) {
            provider.delete(entity);
        }
        dataProvider.refreshAll();
        selection.accept(null);
    }

    @Override
    public void entityUpdated(T entity) {
        if (provider != null) {
            provider.update(entity);
        }
        dataProvider.refreshItem(entity);
    }
}