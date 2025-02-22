/*
 * Copyright 2021-2024 Marcel Baumann
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

package net.tangly.erp.agile.model;

import net.tangly.core.MutableExternalEntity;
import net.tangly.core.MutableExternalEntityImp;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Product extends MutableExternalEntityImp implements MutableExternalEntity {
    private final List<Feature> features;

    public Product(@NotNull String id) {
        super(id);
        features = new ArrayList<>();
    }

    public List<Feature> features() {
        return Collections.unmodifiableList(features);
    }

    public void add(@NotNull Feature feature) {
        features.add(feature);
    }

    public boolean remove(@NotNull Feature feature) {
        return features.remove(feature);
    }

    public Optional<Feature> findById(String id) {
        return features.stream().filter(o -> Objects.equals(id, o.id())).findAny();
    }
}
