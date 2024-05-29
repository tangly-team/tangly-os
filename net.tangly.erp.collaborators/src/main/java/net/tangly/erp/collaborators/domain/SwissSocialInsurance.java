/*
 * Copyright 2021-2024 Marcel Baumann
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

package net.tangly.erp.collaborators.domain;

import lombok.Builder;
import net.tangly.core.DateRange;
import net.tangly.core.HasDateRange;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Builder
public record SwissSocialInsurance(@NotNull DateRange range, @NotNull BigDecimal ahvPercentage, @NotNull BigDecimal ahvAdministrationPercentage,
                                   @NotNull BigDecimal ivPercentage,
                                   @NotNull BigDecimal eoPercentage, @NotNull BigDecimal alvPercentage,
                                   @NotNull BigDecimal fakPercentage) implements HasDateRange {
    static final BigDecimal HALF = new BigDecimal("0.5");

    public BigDecimal computeEmployeeSocialInsurances(BigDecimal amount) {
        return amount.multiply(employeeSocialInsurancesPercentage());
    }

    public BigDecimal computeEmployerSocialInsurances(BigDecimal amount) {
        return amount.multiply(employerSocialInsurancesPercentage());
    }

    public BigDecimal employeeSocialInsurancesPercentage() {
        return (ahvPercentage().multiply(HALF)).add(ivPercentage().multiply(HALF)).add(eoPercentage().multiply(HALF)).add(alvPercentage().multiply(HALF));
    }

    public BigDecimal employerSocialInsurancesPercentage() {
        return (ahvPercentage().multiply(HALF)).add(ivPercentage().multiply(HALF)).add(eoPercentage().multiply(HALF)).add(alvPercentage().multiply(HALF)).add(
                fakPercentage())
            .add(employerAdministrativeCostsPercentage());
    }

    public BigDecimal employerAdministrativeCostsPercentage() {
        return (ahvPercentage().add(ivPercentage()).add(eoPercentage())).multiply(ahvAdministrationPercentage());
    }
}
