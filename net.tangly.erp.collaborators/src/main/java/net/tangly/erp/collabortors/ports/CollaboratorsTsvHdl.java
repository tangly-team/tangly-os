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

package net.tangly.erp.collabortors.ports;

import java.util.List;

import net.tangly.erp.collabortors.domain.Contract;
import net.tangly.erp.collabortors.domain.SwissPensionFunds;
import net.tangly.erp.collabortors.domain.SwissSocialInsurances;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;

public class CollaboratorsTsvHdl {
    static TsvEntity<Contract> createTsvContract() {
        List<TsvProperty<Contract, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", Contract::fromDate, Contract::fromDate), TsvProperty.ofDate("toDate", Contract::toDate, Contract::toDate));
        return TsvEntity.of(Contract.class, fields, Contract::new);
    }

    static TsvEntity<SwissSocialInsurances> createTsvSwissSocialInsurances() {

        List<TsvProperty<SwissSocialInsurances, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissSocialInsurances::fromDate, null), TsvProperty.ofDate("toDate", SwissSocialInsurances::toDate, null));
        return TsvEntity.of(SwissSocialInsurances.class, fields, SwissSocialInsurances::new);
    }

    static TsvEntity<SwissPensionFunds> createTsvSwissPensionFunds() {
        List<TsvProperty<SwissPensionFunds, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissPensionFunds::fromDate, null), TsvProperty.ofDate("toDate", SwissPensionFunds::toDate, null));
        return TsvEntity.of(SwissPensionFunds.class, fields, SwissPensionFunds::new);
    }

}
