/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeTest {
    final LocalDate FROM = LocalDate.of(2000, Month.JANUARY, 1);
    final LocalDate TO = LocalDate.of(2000, Month.DECEMBER, 31);

    @Test
    void testIsActive() {
        assertThat(DateRange.of(FROM, TO).isActive(FROM.minusDays(1))).isFalse();
        assertThat(DateRange.of(FROM, TO).isActive(FROM)).isTrue();
        assertThat(DateRange.of(FROM, TO).isActive(FROM.plusDays(180))).isTrue();
        assertThat(DateRange.of(FROM, TO).isActive(TO)).isTrue();
        assertThat(DateRange.of(FROM, TO).isActive(TO.plusDays(1))).isFalse();
    }

    @Test
    void testIsActiveWithoutTo() {
        assertThat(DateRange.of(FROM, null).isActive(FROM.minusDays(1))).isFalse();
        assertThat(DateRange.of(FROM, null).isActive(FROM)).isTrue();
        assertThat(DateRange.of(FROM, null).isActive(FROM.plusDays(180))).isTrue();
        assertThat(DateRange.of(FROM, null).isActive(TO)).isTrue();
        assertThat(DateRange.of(FROM, null).isActive(TO.plusDays(1))).isTrue();
    }

    @Test
    void testIsActiveWithoutFrom() {
        assertThat(DateRange.of(null, TO).isActive(FROM.minusDays(1))).isTrue();
        assertThat(DateRange.of(null, TO).isActive(FROM)).isTrue();
        assertThat(DateRange.of(null, TO).isActive(FROM.plusDays(180))).isTrue();
        assertThat(DateRange.of(null, TO).isActive(TO)).isTrue();
        assertThat(DateRange.of(null, TO).isActive(TO.plusDays(1))).isFalse();
    }
}
