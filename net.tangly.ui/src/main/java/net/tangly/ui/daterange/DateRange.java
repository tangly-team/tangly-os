/*
 * Copyright 2023 Marcel Baumann
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

package net.tangly.ui.daterange;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;

public class DateRange implements TemporalAmount {

    private LocalDate beginDate;
    private LocalDate endDate;

    public static DateRange between(LocalDate beginDate, LocalDate endDate) {
        DateRange dateRange = new DateRange();
        dateRange.setBeginDate(beginDate);
        dateRange.setEndDate(endDate);
        return dateRange;
    }

    public LocalDate getBeginDate() {
        return this.beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Period getPeriod() {
        LocalDate beginDate = Optional.ofNullable(this.beginDate).orElse(LocalDate.MIN);
        LocalDate endDate = Optional.ofNullable(this.endDate).orElse(LocalDate.MAX);
        return Period.between(beginDate, endDate);
    }

    @Override
    public long get(TemporalUnit unit) {
        return this.getPeriod().get(unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return this.getPeriod().getUnits();
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return this.getPeriod().addTo(temporal);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return this.getPeriod().subtractFrom(temporal);
    }

    @Override
    public String toString() {
        return this.getPeriod().toString();
    }

}
