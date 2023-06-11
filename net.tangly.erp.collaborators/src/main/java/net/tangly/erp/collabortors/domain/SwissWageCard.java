/*
 * Copyright 2021-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collabortors.domain;

import lombok.Builder;
import net.tangly.core.Address;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Builder
public record SwissWageCard(@NotNull String socialNumber, String oldSocialNumber, @NotNull LocalDate birthday, @NotNull LocalDate fromDate, @NotNull LocalDate toDate,
                            Boolean freeTransportToWork, Boolean mealChecks, BigDecimal grossSalary, BigDecimal netSalary, BigDecimal pensionFund,
                            BigDecimal pensionFundPayment, boolean effectiveExpenses, LocalDate createdAt, Address createdBy) {
    public SwissWageCard {
        Objects.requireNonNull(birthday);
        Objects.requireNonNull(fromDate);
        Objects.requireNonNull(toDate);
        assert Objects.equals(fromDate.getYear(), toDate.getYear());
    }

    public int year() {
        return fromDate().getYear();
    }
}
