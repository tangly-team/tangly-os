/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.providers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderInMemoryTest {
    static final int SIZE = 10;

    static record Entity(String name) {
    }

    @Test
    void testConstructors() {
        var provider = new ProviderInMemory<>(entities());
        assertThat(provider.items().size()).isEqualTo(SIZE);

        assertThat(ProviderInMemory.of(entities()).items().size()).isEqualTo(SIZE);

        provider = new ProviderInMemory<>();
        provider.updateAll(entities());
        assertThat(provider.items().size()).isEqualTo(SIZE);
    }

    @Test
    void testUpdates() {
        var entities = entities();
        var provider = new ProviderInMemory<>(entities);
        assertThat(provider.items().size()).isEqualTo(SIZE);
        entities.forEach(provider::update);
        assertThat(provider.items().size()).isEqualTo(SIZE);
        provider.delete(entities.get(0));
        assertThat(provider.items().size()).isEqualTo(SIZE - 1);
    }

    @Test
    void testFindBy() {
        var provider = new ProviderInMemory<>(entities());
        assertThat(provider.findBy(o -> o.name(), "name" + (SIZE - 1))).isPresent();
        assertThat(provider.findBy(o -> o.name(), "name" + SIZE)).isNotPresent();

    }

    private static List<Entity> entities() {
        return IntStream.range(0, SIZE).mapToObj(o -> new Entity("name" + o)).toList();
    }
}
