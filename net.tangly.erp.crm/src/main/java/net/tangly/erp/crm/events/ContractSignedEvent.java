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

package net.tangly.erp.crm.events;

import net.tangly.core.DateRange;
import org.jetbrains.annotations.NotNull;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Locale;

/**
 * Defines the signing of a contract event. Upon signing of a contract from the customer, the event is created and sent.
 *
 * @param id             unique identifier of the contract in the context of a tenant.
 * @param mainContractId unique identifier of the main contract if the contract is a subcontract. The value is optional
 * @param range          date range of the contract validity
 * @param locale         locale of the contract
 * @param budget         monetary budget of the contract. The currency is part of the monetary amount
 * @param budgetInHours  work budget measured with hours.
 */
public record ContractSignedEvent(@NotNull String id, String mainContractId, @NotNull DateRange range, @NotNull Locale locale,
                                  MonetaryAmount budget, BigDecimal budgetInHours) {
}
