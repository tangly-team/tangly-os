/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collabortors.domain;

import lombok.Builder;
import net.tangly.core.HasInterval;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

@Builder
public record SwissSocialInsurances(LocalDate from,
                                    LocalDate to,
                                    BigDecimal ahvPercentage,
                                    BigDecimal ahvAdministrationPercentage,
                                    BigDecimal ivPercentage,
                                    BigDecimal eoPercentage,
                                    BigDecimal alvPercentage,
                                    BigDecimal fakPercentage
) implements HasInterval {
    static final BigDecimal HALF = new BigDecimal("0.5");

    public static SwissSocialInsurances of(int year) {
        return switch (year) {
            case 2021, 2020, 2019 -> of2019_2021();
            case 2018, 2017, 2016 -> of2016_2018();
            default -> null;
        };
    }

    static SwissSocialInsurances of2019_2021() {
        return new SwissSocialInsurances(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.DECEMBER, 31), new BigDecimal("8.7"),
            new BigDecimal("5.0"), new BigDecimal("1.4"), new BigDecimal("0.5"), new BigDecimal("2.2"), new BigDecimal("1.7"));
    }


    static SwissSocialInsurances of2016_2018() {
        return new SwissSocialInsurances(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.DECEMBER, 31), new BigDecimal("8.4"),
            new BigDecimal("5.0"), new BigDecimal("1.4"), new BigDecimal("0.45"), new BigDecimal("2.2"), new BigDecimal("1.7"));
    }

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
        return (ahvPercentage().multiply(HALF)).add(ivPercentage().multiply(HALF)).add(eoPercentage().multiply(HALF)).add(alvPercentage().multiply(HALF))
            .add(fakPercentage()).add((ahvPercentage().add(ivPercentage()).add(eoPercentage())).multiply(ahvAdministrationPercentage()));
    }
}
