/*
 * Copyright 2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.crm.domain;

import net.tangly.core.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * A contract extension extends the budget for an existing contract. The conditions of the contract stay the same.
 *
 * @param amountWithoutVat additional amount for the contract
 * @param range            date range of the contract extension
 */
public record ContractExtension(@NotNull String id, @NotNull String name, @NotNull DateRange range, String text, @NotNull String contractId,
                                @NotNull BigDecimal amountWithoutVat, BigDecimal budgetInHours) implements HasId, HasName, HasDateRange, HasText {
}
