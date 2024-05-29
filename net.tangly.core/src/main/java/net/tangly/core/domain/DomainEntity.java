/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.core.domain;

import net.tangly.core.HasMutableComments;
import net.tangly.core.HasMutableId;
import net.tangly.core.HasMutableTags;
import net.tangly.core.HasOid;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * Define the meta-information describing the characteristics of a domain entity. Helper methods are provided to collect statistical information on the
 * instances of the domain entity.
 *
 * @param <T> type of the domain entity.
 */
public record DomainEntity<T>(@NotNull String domain, @NotNull Class<T> clazz, Provider<T> provider) {
    public boolean hasOid() {
        return HasOid.class.isAssignableFrom(clazz);
    }

    public boolean hasId() {
        return HasMutableId.class.isAssignableFrom(clazz);
    }

    public boolean hasComments() {
        return HasMutableComments.class.isAssignableFrom(clazz);
    }

    public boolean hasTags() {
        return HasMutableTags.class.isAssignableFrom(clazz);
    }

    public String name() {
        return clazz().getSimpleName();
    }

    public long entitiesCount() {
        return provider().items().size();
    }

    public long tagsCount() {
        return provider().items().stream().map(o -> ((HasMutableTags) o).tags().size()).reduce(0, Integer::sum);
    }

    public long commentsCount() {
        return provider().items().stream().map(o -> ((HasMutableComments) o).comments().size()).reduce(0, Integer::sum);
    }
}
