/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.erp.crm.services;

import net.tangly.core.HasMutableDateRange;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.InteractionCode;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * The business logic and rules of the bounded domain of CRM entities.
 */
public class CrmBusinessLogic {
    private final CrmRealm realm;

    public CrmBusinessLogic(@NotNull CrmRealm realm) {
        this.realm = realm;
    }

    public CrmRealm realm() {
        return realm;
    }


    /**
     * Set the end date property of interaction to the end date of the last contract associated with the interaction in the case of customer and completed state. Set the end date
     * property of interaction to the end date of the last activity associated with the interaction in the case of lost state.
     */
    public void updateInteractions() {
        realm().interactions().items().forEach(interaction -> interaction.to(
            realm().contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.entity().oid()).map(Contract::to)
                .max(Comparator.comparing(LocalDate::toEpochDay)).orElseThrow()));
        realm().interactions().items().stream().filter(o -> o.code() == InteractionCode.lost)
            .forEach(interaction -> interaction.to(interaction.activities().stream().map(Activity::date).max(Comparator.comparing(LocalDate::toEpochDay)).orElseThrow()));
    }

    /**
     * Return the potential number of all interactions in the selected time slot and tate.
     *
     * @param code defines the state of the expected interactions
     * @param from interactions should have been started after this date
     * @param to   interactions should have been started after this date
     * @return the aggregated potential amount
     */
    public BigDecimal funnel(@NotNull InteractionCode code, LocalDate from, LocalDate to) {
        return switch (code) {
            case lead, prospect, lost -> realm.interactions().items().stream().filter(o -> o.code() == code)
                .filter(new HasMutableDateRange.RangeFilter<>(from, to)).map(Interaction::weightedPotential)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            case ordered, completed -> realm.interactions().items().stream().filter(o -> o.code() == code)
                .flatMap(interaction -> realm.contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.entity().oid()))
                .filter(new HasMutableDateRange.RangeFilter<>(from, to)).map(Contract::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
        };
    }
}
