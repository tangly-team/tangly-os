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

package net.tangly.core.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.commons.generator.LongIdGenerator;
import net.tangly.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class BoundedDomain<R, B, H, P> {
    private final R realm;
    private final H handler;
    private final P port;
    private final B logic;
    private final TagTypeRegistry registry;
    private final Map<String, String> configuration;
    protected IdGenerator idGenerator;

    public BoundedDomain(R realm, B logic, H handler, P port, TagTypeRegistry registry) {
        this(realm, logic, handler, port, registry, Collections.emptyMap());
    }

    public BoundedDomain(R realm, B logic, H handler, P port, TagTypeRegistry registry, @NotNull Map<String, String> configuration) {
        this.realm = realm;
        this.logic = logic;
        this.handler = handler;
        this.port = port;
        this.registry = registry;
        this.configuration = new HashMap<>(configuration);
        idGenerator = new LongIdGenerator(1000);
        initialize();
    }

    public R realm() {
        return realm;
    }

    public B logic() {
        return logic;
    }

    public H handler() {
        return handler;
    }

    public P port() {
        return port;
    }

    public TagTypeRegistry registry() {
        return registry;
    }

    public IdGenerator idGenerator() {
        return idGenerator;
    }

    public Map<String, String> configuration() {
        return Collections.unmodifiableMap(configuration);
    }

    protected void initialize() {
    }
}
