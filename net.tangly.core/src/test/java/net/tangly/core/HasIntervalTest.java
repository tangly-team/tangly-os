/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class HasIntervalTest {
    final LocalDate FROM = LocalDate.of(2000, Month.JANUARY, 1);
    final LocalDate TO = LocalDate.of(2000, Month.DECEMBER, 31);

    @Test
    void testIsActive() {
        assertThat(HasInterval.isActive(FROM.minusDays(1), FROM, TO)).isFalse();
        assertThat(HasInterval.isActive(FROM, FROM, TO)).isTrue();
        assertThat(HasInterval.isActive(FROM.plusDays(180), FROM, TO)).isTrue();
        assertThat(HasInterval.isActive(TO, FROM, TO)).isTrue();
        assertThat(HasInterval.isActive(TO.plusDays(1), FROM, TO)).isFalse();
    }

    @Test
    void testIsActiveWithoutTo() {
        assertThat(HasInterval.isActive(FROM.minusDays(1), FROM, null)).isFalse();
        assertThat(HasInterval.isActive(FROM, FROM, null)).isTrue();
        assertThat(HasInterval.isActive(FROM.plusDays(180), FROM, null)).isTrue();
        assertThat(HasInterval.isActive(TO, FROM, null)).isTrue();
        assertThat(HasInterval.isActive(TO.plusDays(1), FROM, null)).isTrue();
    }

    @Test
    void testIsActiveWithoutFrom() {
        assertThat(HasInterval.isActive(FROM.minusDays(1), null, TO)).isTrue();
        assertThat(HasInterval.isActive(FROM, null, TO)).isTrue();
        assertThat(HasInterval.isActive(FROM.plusDays(180), null, TO)).isTrue();
        assertThat(HasInterval.isActive(TO, null, TO)).isTrue();
        assertThat(HasInterval.isActive(TO.plusDays(1), null, TO)).isFalse();
    }
}
