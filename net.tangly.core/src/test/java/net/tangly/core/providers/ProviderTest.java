/*
 * Copyright 2023-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core.providers;

import net.tangly.core.HasOid;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public interface ProviderTest {
    int AGGREGATE_SIZE = 5;
    int SIZE = 10;

    record Entity(long oid, String name) implements HasOid {
    }

    record ValueObject(String name, int value) {
    }

    record Aggregate(long oid, String name, List<ValueObject> valueObjects) implements HasOid {
    }

    static List<Entity> simpleEntities() {
        return LongStream.range(0, SIZE).mapToObj(o -> new Entity(o, STR."name\{o}")).toList();
    }

    static List<Aggregate> aggregates() {
        return LongStream.range(0, SIZE)
            .mapToObj(o -> new Aggregate(o, STR."name\{o}", IntStream.range(0, AGGREGATE_SIZE).mapToObj(i -> new ValueObject(STR."name\{i} of \{o}", i)).toList())).toList();
    }
}
