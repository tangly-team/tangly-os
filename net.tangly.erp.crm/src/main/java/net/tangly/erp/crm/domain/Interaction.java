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

package net.tangly.erp.crm.domain;

import net.tangly.core.EntityExtendedImp;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Defines an interaction between your company and a legal entity and a group of natural entities. The legal entity is the organization you want a contract with. The interaction
 * has a set of activities moving your negotiation through stages. The final result is a contract or a lost opportunity.
 * <p>An interaction can have quite a long duration. Activities are the events when you interact with your potential customer.</p>
 */
public class Interaction extends EntityExtendedImp {
    private final List<Activity> activities;
    private LegalEntity entity;
    private InteractionCode code;
    private BigDecimal potential;
    private BigDecimal probability;

    public Interaction(long oid) {
        super(oid);
        activities = new ArrayList<>();
        from(LocalDate.now());
        this.code = InteractionCode.prospect;
        this.potential = BigDecimal.ZERO;
        this.probability = BigDecimal.ZERO;
    }

    public LegalEntity entity() {
        return entity;
    }

    public void entity(LegalEntity legalEntity) {
        this.entity = legalEntity;
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
    public boolean validate() {
        return Objects.nonNull(entity()) && (Objects.requireNonNull(potential).compareTo(BigDecimal.ZERO) >= 0) &&
            (Objects.requireNonNull(probability).compareTo(BigDecimal.ZERO) >= 0) && (Objects.requireNonNull(probability).compareTo(BigDecimal.ONE) <= 0);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Interaction o) && super.equals(o) && Objects.equals(activities().size(), o.activities.size()) && Objects.equals(entity(), o.entity()) &&
            Objects.equals(code(), o.code()) && Objects.equals(potential(), o.potential()) && Objects.equals(probability(), o.probability());
    }

    @Override
    public String toString() {
        return """
            Interaction[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, state=%s, potential=%s, probability=%s, tags=%s]
            """.formatted(oid(), id(), name(), from(), to(), text(), code(), potential(), probability(), tags());
    }
}
