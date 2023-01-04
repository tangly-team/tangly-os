/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collaborators.services;

import net.tangly.erp.collabortors.domain.Collaborator;
import net.tangly.erp.collabortors.domain.Contract;
import net.tangly.erp.collabortors.domain.Organization;
import net.tangly.erp.collabortors.domain.SwissAccidentInsurance;
import net.tangly.erp.collabortors.domain.SwissPensionFund;
import net.tangly.erp.collabortors.domain.SwissSocialInsurance;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

class CollaboratorTest {
    static SwissSocialInsurance of2022_2022() {
        return new SwissSocialInsurance(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.DECEMBER, 31), new BigDecimal("0.087"),
            new BigDecimal("0.03"), new BigDecimal("0.014"), new BigDecimal("0.005"), new BigDecimal("0.022"), new BigDecimal("0.017"));
    }

    static SwissSocialInsurance of2019_2021() {
        return new SwissSocialInsurance(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.DECEMBER, 31), new BigDecimal("0.087"),
            new BigDecimal("0.05"), new BigDecimal("0.014"), new BigDecimal("0.005"), new BigDecimal("0.022"), new BigDecimal("0.017"));
    }


    static SwissSocialInsurance of2016_2018() {
        return new SwissSocialInsurance(LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.DECEMBER, 31), new BigDecimal("8.4"),
            new BigDecimal("0.05"), new BigDecimal("0.014"), new BigDecimal("0.0045"), new BigDecimal("0.022"), new BigDecimal("0.017"));
    }

    static SwissAccidentInsurance of2016_2022() {
        return new SwissAccidentInsurance(LocalDate.of(2016, Month.JANUARY, 1), null, new BigDecimal("0.0092"), new BigDecimal("0.01199"), new BigDecimal("100"), false);
    }

    static SwissPensionFund ofSwissPensionFunds2017() {
        return new SwissPensionFund(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31),
            new BigDecimal("0.18456"), new BigDecimal("0.5"));
    }

    static SwissPensionFund ofSwissPensionFunds2018() {
        return new SwissPensionFund(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31),
            new BigDecimal("0.18736"), new BigDecimal("0.5"));
    }

    static SwissPensionFund ofSwissPensionFunds2019() {
        return new SwissPensionFund(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31),
            new BigDecimal("0.21562"), new BigDecimal("0.5"));
    }

    static SwissPensionFund ofSwissPensionFunds2020() {
        return new SwissPensionFund(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31),
            new BigDecimal("0.22020"), new BigDecimal("0.5"));
    }

    static SwissPensionFund ofSwissPensionFunds2021() {
        return new SwissPensionFund(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31),
            new BigDecimal("0.22164"), new BigDecimal("0.5"));
    }

    static SwissPensionFund ofSwissPensionFunds2022() {
        return new SwissPensionFund(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31),
            new BigDecimal("0.21724"), new BigDecimal("0.5"));
    }

    @Test
    void computeSwissSocialnsurances() {}

    @Test
    void computeSwissPensionFunds() {}

    @Test
    void computeSwissInsurances() {}

    private Organization createOrganizationAndContract() {
        Collaborator collaborator = Collaborator.builder()
            .birthday(LocalDate.of(1964, Month.DECEMBER, 12))
            .id("756.5149.8825.64")
            .oldSocialSecurityNumber("144.64.474.111").build();
        Contract contract = Contract.builder()
            .collaborator(collaborator)
            .from(LocalDate.of(2016, Month.JANUARY, 1))
            .yearlySalary(new BigDecimal("30000"))
            .nrOfPayments(12)
            .workPercentage(new BigDecimal("1"))
            .pensionFunds(List.of(ofSwissPensionFunds2017(),
                ofSwissPensionFunds2017(),
                ofSwissPensionFunds2018(),
                ofSwissPensionFunds2019(),
                ofSwissPensionFunds2020(),
                ofSwissPensionFunds2021(),
                ofSwissPensionFunds2022())).build();
        Organization organization = new Organization("CHE-357.875.339");
        organization.addContract(contract);

        organization.addSocialInsurances(of2016_2018());
        organization.addSocialInsurances(of2019_2021());
        organization.addSocialInsurances(of2022_2022());

        organization.addInsurances(of2016_2022());

        return organization;
    }
}
