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

import net.tangly.core.HasId;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class Organization implements HasId {
    private final String legalEntityId;
    private final List<SwissSocialInsurance> socialInsurances;
    private final List<SwissAccidentInsurance> insurances;

    public Organization(@NotNull String legalEntityId) {
        this.legalEntityId = legalEntityId;
        contracts = new ArrayList<>();
        socialInsurances = new ArrayList<>();
        insurances = new ArrayList<>();
    }

    private final List<Contract> contracts;

    @Override
    public String id() {
        return legalEntityId;
    }

    public void addContract(@NotNull Contract contract) {
        this.contracts.add(contract);
    }

    public void addSocialInsurances(@NotNull SwissSocialInsurance socialInsurances) {
        this.socialInsurances.add(socialInsurances);
    }

    public void addInsurances(@NotNull SwissAccidentInsurance insurances) {
        this.insurances.add(insurances);
    }

    public PensionFundDeductions computePensionFundDeductions(@NotNull Contract contract, @NotNull Year year) {
        return null;
    }

    public AccidentInsuranceDeductions computeAccidentInsuranceDeductions(@NotNull Contract contract, @NotNull Year year) {
        return null;
    }

    public SocialInsuranceDeductions computeSocialInsuranceDeductions(@NotNull Contract contract, @NotNull Year year) {
        return null;
    }

    public SwissWageCard computeYearlyWageCard(@NotNull Contract contract, int year) {
        return null;
    }

    public SwissWageCard computeMonthlyWageCard(@NotNull Contract contract, int year, Month month) {
        return null;
    }

    record PensionFundDeductions(Year year, BigDecimal employeeDeductions, BigDecimal employerDeductions) {
    }

    record AccidentInsuranceDeductions(Year year, BigDecimal professionalAccidentDeductions, BigDecimal nonProfessionalAccidentDeductions) {
    }

    record SocialInsuranceDeductions(Year year, BigDecimal employeeDeductions, BigDecimal employerDeductions) {
    }
}
