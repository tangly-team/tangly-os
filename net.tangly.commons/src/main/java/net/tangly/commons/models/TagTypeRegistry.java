/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.models;

import java.util.*;

public class TagTypeRegistry {
    private Set<TagType> types;

    public TagTypeRegistry() {
        types = new HashSet<>();
    }

    public void register(TagType type) {
        types.add(type);
    }

    public List<String> namespaces() {
        return Collections.emptyList();
    }

    public List<String> tagsForNamespace(String namespace) {
        return Collections.emptyList();
    }

    public Optional<TagType> find(String namespace, String name) {
        return Optional.empty();
    }
}

