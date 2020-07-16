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

package net.tangly.commons.crm.ui;

import java.util.function.Consumer;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.core.Entity;
import net.tangly.commons.orm.InstanceProvider;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

/**
 * The CRM entity form provides a CRUD abstraction to all properties of an entity instance part of the CRM domain model.
 *
 * @param <T> type displayed in the CRUD view
 */
public abstract class CrmEntitiesView<T extends Entity> extends EntitiesView<T> {
    private final Crm crm;
    private final InstanceProvider<T> instanceProvider;

    protected CrmEntitiesView(@NotNull Crm crm, @NotNull Class<T> clazz, Consumer<Grid<T>> gridConfigurator, InstanceProvider<T> instanceProvider) {
        super(clazz, gridConfigurator, instanceProvider.getAll(), DataProvider.ofCollection(instanceProvider.getAll()), crm.tagTypeRegistry());
        this.crm = crm;
        this.instanceProvider = instanceProvider;
    }

    protected Crm crm() {
        return crm;
    }

    // region Crud Action Listener

    @Override
    public void entityAdded(DataProvider<T, ?> provider, T entity) {
        instanceProvider.update(entity);
        provider.refreshAll();
    }

    @Override
    public void entityDeleted(DataProvider<T, ?> provider, T entity) {
        instanceProvider.delete(entity);
        provider.refreshAll();
    }

    @Override
    public void entityUpdated(DataProvider<T, ?> provider, T entity) {
        instanceProvider.update(entity);
        provider.refreshItem(entity);
    }

    // endregion
}
