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
import net.tangly.core.DateRange;
import net.tangly.core.HasDateRange;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Accident insurance contracts cover professional and non-professional accidents. Simply put, accident insurance is a form of insurance policy that offers a payout when people
 * experience injury or death due to an accident. The costs are a percentage of the salary for professional accidents and another percentage of the salary for non-professional
 * injuries. Often, at least a minimum amount shall be paid for each component of the insurance.
 *
 * @param range      date range when the accident insurance contract is active
 * @param nbuPercentage percentage of the salary for the non-professional accidents insurance
 * @param buPercentage  percentage of the salary for the professional accidents insurance
 * @param mininumAmount threshold amount you have to pay as a minimum amount per kind of coverage
 */
@Builder
public record SwissAccidentInsurance(@NotNull DateRange range, @NotNull BigDecimal nbuPercentage, @NotNull BigDecimal buPercentage, @NotNull BigDecimal mininumAmount,
                                     boolean nbuPaidByEmployer) implements HasDateRange {
}
