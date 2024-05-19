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

package net.tangly.erp.products.domain;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Defines the work view of a contract. The work budget is used to validate the work done by the team.
 *
 * @param id             unique identifier of the contract
 * @param mainContractId unique identifier of the main contract if the contract is a subcontract. The value is optional
 * @param from           start date of the contract
 * @param to             end date of the contract
 * @param locale         locale of the contract
 * @param budgetInHours  work budget in hours. If the value is zero, the contract is time and material without a budget
 */
public record WorkContract(@NotNull String id, String mainContractId, @NotNull LocalDate from, @NotNull LocalDate to, Locale locale, int budgetInHours) {
}
