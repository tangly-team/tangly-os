/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.erp.invoices.domain;

import net.tangly.core.DateRange;
import net.tangly.core.HasDateRange;
import net.tangly.core.codes.Code;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

public enum VatCode implements Code, HasDateRange {
    F0(new BigDecimal("7.5"), DateRange.of(LocalDate.of(1999, Month.JANUARY, 1), LocalDate.of(2000, Month.DECEMBER, 31))),
    F1(new BigDecimal("7.6"), DateRange.of(LocalDate.of(2001, Month.JANUARY, 1), LocalDate.of(2010, Month.DECEMBER, 31))),
    F2(new BigDecimal("8.0"), DateRange.of(LocalDate.of(2011, Month.JANUARY, 1), LocalDate.of(2017, Month.DECEMBER, 31))),
    F3(new BigDecimal("7.7"), DateRange.of(LocalDate.of(2018, Month.JANUARY, 1), LocalDate.of(2023, Month.DECEMBER, 31))),
    F4(new BigDecimal("8.1"), DateRange.of(LocalDate.of(2024, Month.JANUARY, 1), LocalDate.of(2030, Month.DECEMBER, 31)));

    private VatCode(@NotNull BigDecimal rate, @NotNull DateRange range) {
        this.rate = rate;
        this.range = range;
    }

    @Override
    public int id() {
        return this.ordinal();
    }

    @Override
    public String code() {
        return this.name();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public DateRange range() {
        return range;
    }

    public BigDecimal rate() {
        return rate;
    }

    private final BigDecimal rate;
    private final DateRange range;
}
