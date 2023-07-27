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
import net.tangly.core.HasId;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record Contract(@NotNull String id,
                       @NotNull Organization organization,
                       @NotNull Collaborator collaborator, @NotNull DateRange range,
                       @NotNull BigDecimal yearlySalary,
                       @NotNull BigDecimal workPercentage,
                       int nrOfPayments, @NotNull List<SwissPensionFund> pensionFunds) implements HasId, HasDateRange {
}
