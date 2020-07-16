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

package net.tangly.bus.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.tangly.bus.core.HasOid;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance provider with instances in memory.
 *
 * @param <T> type of the instances handled in the provider
 */
public class InstanceProviderInMemory<T extends HasOid> implements InstanceProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(InstanceProviderInMemory.class);
    private final List<T> items;

    public InstanceProviderInMemory() {
        items = new ArrayList<>();
    }

    public static <T extends HasOid> Provider<T> of(Collection<? extends T> items) {
        Provider<T> provider = new InstanceProviderInMemory<>();
        provider.updateAll(items);
        return provider;
    }

    @Override
    public Optional<T> find(long oid) {
        return findBy(T::oid, oid);
    }

    @Override
    public List<T> getAll() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void update(@NotNull T entity) {
        Optional<T> found = find(entity.oid());
        found.ifPresent(o -> {
            if (o != entity) {
                items.remove(o);
                logger.atDebug().addArgument(found).log("Duplicate instance with same oid found {}");
            }
        });
        items.add(entity);
    }

    @Override
    public void delete(@NotNull T entity) {
        items.remove(entity);
    }
}
