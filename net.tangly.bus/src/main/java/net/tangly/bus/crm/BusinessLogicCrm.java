/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.bus.crm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import javax.inject.Inject;

import net.tangly.bus.core.HasInterval;
import org.jetbrains.annotations.NotNull;

/**
 * The business logic and rules of the bounded domain of CRM entities.
 */
public class BusinessLogicCrm {
    private final RealmCrm realm;

    @Inject
    public BusinessLogicCrm(@NotNull RealmCrm realm) {
        this.realm = realm;
    }

    public RealmCrm realm() {
        return realm;
    }

    /**
     * Set the end date property of interaction to the end date of the last contract associated with the interaction in the case of customer and completed
     * state. Set the end date property of interaction to the end date of the last activity associated with the interaction in the case of lost state.
     */
    public void updateInteractions() {
        realm().interactions().items().forEach(interaction -> {
            interaction.toDate(realm().contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.legalEntity().oid())
                    .map(Contract::toDate).max(Comparator.comparing(LocalDate::toEpochDay)).get());
        });
        realm().interactions().items().stream().filter(o -> o.code() == InteractionCode.lost).forEach(interaction -> interaction
                .toDate(interaction.activities().stream().map(Activity::date).max(Comparator.comparing(LocalDate::toEpochDay)).get()));
    }

    public BigDecimal funnel(@NotNull InteractionCode state, LocalDate from, LocalDate to) {
        HasInterval.IntervalFilter<Interaction> filterInteractions = new HasInterval.IntervalFilter(from, to);
        HasInterval.IntervalFilter<Contract> filterContracts = new HasInterval.IntervalFilter(from, to);
        return switch (state) {
            case lead, prospect, lost -> realm.interactions().items().stream().filter(o -> o.code() == state).filter(filterInteractions)
                    .map(Interaction::weightedPotential).reduce(BigDecimal.ZERO, BigDecimal::add);
            case customer, completed -> realm.interactions().items().stream().filter(o -> o.code() == state)
                    .flatMap(interaction -> realm.contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.legalEntity().oid()))
                    .filter(filterContracts).map(Contract::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
        };
    }
}
