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

package net.tangly.core.domain;

import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.Entity;
import net.tangly.core.HasOid;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * Realm is responsible for the handling of entities and value objects part of the domain model.
 * <p>One key feature of entities is their unique object identifier defined in the context of the bounded domain. The realm provides functions to generate
 * unique object identifiers and set them in the entities in need of them. The object identifiers are created in the application.</p>
 */
public interface Realm {
    static <T extends HasOid> long maxOid(Provider<T> provider) {
        return provider.items().stream().mapToLong(HasOid::oid).max().orElse(HasOid.UNDEFINED_OID);
    }

    static <T extends HasOid> T setOid(@NotNull T entity, long oid) {
        ReflectionUtilities.set(entity, "oid", oid);
        return entity;
    }

    static <T extends Entity> void checkEntities(Provider<T> provider) {
        provider.items().forEach(e -> {
            if (!e.check()) {
                throw new IllegalStateException("Entity Check Error for %s".formatted(e));
            }
        });
    }

    <T extends HasOid> T registerOid(@NotNull T entity);
}
