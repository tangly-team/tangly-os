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

package net.tangly.core.providers;

import net.tangly.commons.generator.LongIdGenerator;
import net.tangly.core.HasOid;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderInMemoryTest {
    static final int SIZE = 10;

    record Entity(long oid, String name) implements HasOid {
    }

    static List<Entity> entities() {
        return LongStream.range(0, SIZE).mapToObj(o -> new Entity(o, "name%d".formatted(o))).toList();
    }

    @Test
    void testConstructors() {
        var provider = ProviderInMemory.of(entities());
        assertThat(provider.items()).hasSize(SIZE);
        assertThat(ProviderInMemory.of(entities()).items()).hasSize(SIZE);

        provider = ProviderInMemory.of();
        provider.updateAll(entities());
        assertThat(provider.items()).hasSize(SIZE);
    }

    @Test
    void testUpdates() {
        var entities = entities();
        var provider = ProviderInMemory.of(entities);
        assertThat(provider.items()).hasSize(SIZE);
        entities.forEach(provider::update);
        assertThat(provider.items()).hasSize(SIZE);
        provider.delete(entities.getFirst());
        assertThat(provider.items()).hasSize(SIZE - 1);
    }

    @Test
    void testFindBy() {
        var provider = ProviderInMemory.of(entities());
        assertThat(provider.findBy(Entity::name, "name%d".formatted(SIZE - 1))).isPresent();
        assertThat(provider.findBy(Entity::name, "name%d".formatted(SIZE))).isNotPresent();
    }

    @Test
    void testOidHandling() {
        var provider = ProviderHasOid.of(new LongIdGenerator(0), entities());
        assertThat(provider.items()).hasSize(SIZE);
        provider.items().forEach(o -> assertThat(o.oid != HasOid.UNDEFINED_OID));
    }
}
