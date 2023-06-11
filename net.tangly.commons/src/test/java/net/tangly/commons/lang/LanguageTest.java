/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.lang;

import org.junit.jupiter.api.Test;

import static net.tangly.commons.lang.Preconditions.checkArgument;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LanguageTest {
    @Test
    void testPreconditionWithInterval() {
        Interval<Integer> interval = new Interval<>(1, 10);
        checkArgument(interval.lower().equals(1));
        checkArgument(interval.upper().equals(10));
        assertThrows(IllegalArgumentException.class, () -> checkArgument(interval.lower().equals(0)));
        assertThrows(IllegalArgumentException.class, () -> checkArgument(interval.upper().equals(8)));
    }
}
