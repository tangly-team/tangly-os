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

import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

public record DomainEntity<T>(@NotNull String domain, @NotNull Class<T> clazz, Provider<T> provider) {
    public boolean hasOid() {
        return HasOid.class.isAssignableFrom(clazz);
    }

    public boolean hasId() {
        return HasId.class.isAssignableFrom(clazz);
    }

    public boolean hasComments() {
        return HasComments.class.isAssignableFrom(clazz);
    }

    public boolean hasTags() {
        return HasTags.class.isAssignableFrom(clazz);
    }

    public String name() {
        return clazz().getSimpleName();
    }

    public long EntitiesCount() {
        return provider().items().size();
    }

    public <U extends HasTags> long TagsCount() {
        return provider().items().stream().map(o -> ((HasTags) o).tags().size()).reduce(0, Integer::sum);
    }

    public <U extends HasComments> long CommentsCount() {
        return provider().items().stream().map(o -> ((HasComments) o).comments().size()).reduce(0, Integer::sum);
    }
}
