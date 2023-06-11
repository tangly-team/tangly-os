/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.agile.model;

import net.tangly.core.ExternalEntity;
import net.tangly.core.ExternalEntityImp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Feature extends ExternalEntityImp implements ExternalEntity {
    private final List<Story> stories;

    public Feature(@NotNull String id) {
        super(id);
        stories = new ArrayList<>();
    }

    public List<Story> stories() {
        return Collections.unmodifiableList(stories);
    }

    public void add(@NotNull Story story) {
        stories.add(story);
    }

    public boolean remove(@NotNull Story story) {
        return stories.remove(story);
    }

    public Optional<Story> findById(String id) {
        return stories.stream().filter(o -> Objects.equals(id, o.id())).findAny();
    }
}
