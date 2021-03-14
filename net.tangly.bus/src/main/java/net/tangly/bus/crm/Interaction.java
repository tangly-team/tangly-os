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

import net.tangly.core.QualifiedEntityImp;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Defines an interaction between your company and a legal entity and a group of natural entities. The legal entity is the organization you want a contract
 * with. The natural entities are the persons in these organizations you are communicated with. The interaction has a set of activities moving your negotiation
 * through stages. The final result is a contract or a lost opportunity.
 * <p>An interaction can have quite a long duration. Activities are the events when you interact with your potential customer.</p>
 */
public class Interaction extends QualifiedEntityImp {
    private final List<Activity> activities;
    private LegalEntity legalEntity;
    private InteractionCode code;
    private BigDecimal potential;
    private BigDecimal probability;

    public Interaction() {
        activities = new ArrayList<>();
        fromDate(LocalDate.now());
        this.code = InteractionCode.prospect;
        this.potential = BigDecimal.ZERO;
        this.probability = BigDecimal.ZERO;
    }

    public LegalEntity legalEntity() {
        return legalEntity;
    }

    public void legalEntity(LegalEntity legalEntity) {
        this.legalEntity = legalEntity;
    }

    public InteractionCode code() {
        return code;
    }

    public void code(@NotNull InteractionCode code) {
        this.code = code;
    }

    public BigDecimal potential() {
        return potential;
    }

    public void potential(@NotNull BigDecimal potential) {
        this.potential = potential;
    }

    public BigDecimal probability() {
        return probability;
    }

    public void probability(@NotNull BigDecimal probability) {
        this.probability = probability;
    }

    public BigDecimal weightedPotential() {
        return potential.multiply(probability);
    }

    public List<Activity> activities() {
        return Collections.unmodifiableList(activities);
    }

    public void addAll(Collection<Activity> activities) {
        this.activities.addAll(activities);
    }

    public void add(Activity activity) {
        activities.add(activity);
    }

    public void remove(Activity activity) {
        activities.remove(activity);
    }

    @Override
    public boolean check() {
        return Objects.nonNull(legalEntity()) && (Objects.requireNonNull(probability).compareTo(BigDecimal.ZERO) > 0) &&
            (Objects.requireNonNull(probability).compareTo(BigDecimal.ONE) <= 0);
    }

    @Override
    public String toString() {
        return """
            Interaction[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, state=%s, potential=%s, probability=%s, tags=%s]
            """.formatted(oid(), id(), name(), fromDate(), toDate(), text(), code(), potential(), probability(), tags());
    }
}
