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

import net.tangly.erp.collabortors.domain.Contract;
import net.tangly.erp.collabortors.domain.SwissInsurances;
import net.tangly.erp.collabortors.domain.SwissPensionFunds;
import net.tangly.erp.collabortors.domain.SwissSocialInsurances;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.function.Function;

public class CollaboratorsTsvHdl {
    static TsvEntity<Contract> createTsvContract() {
        Function<CSVRecord, Contract> of = null;
        List<TsvProperty<Contract, ?>> fields =
            List.of(
                TsvProperty.ofDate("fromDate", Contract::from, null),
                TsvProperty.ofDate("toDate", Contract::to, null),
                TsvProperty.ofLong("collaboratorOid", Contract::naturalEntityOid, null),
                TsvProperty.ofLong("toDate", Contract::naturalEntityOid, null));
        return TsvEntity.of(Contract.class, fields, of);
    }

    static TsvEntity<SwissInsurances> createTsvSwissInsurances() {
        Function<CSVRecord, SwissInsurances> of = null;
        List<TsvProperty<SwissInsurances, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissInsurances::from, null),
                TsvProperty.ofDate("toDate", SwissInsurances::to, null));
        return TsvEntity.of(SwissInsurances.class, fields, of);
    }

    static TsvEntity<SwissPensionFunds> createTsvSwissPensionFunds() {
        Function<CSVRecord, SwissPensionFunds> of = null;
        List<TsvProperty<SwissPensionFunds, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissPensionFunds::from, null),
                TsvProperty.ofDate("toDate", SwissPensionFunds::to, null));
        return TsvEntity.of(SwissPensionFunds.class, fields, of);
    }

    static TsvEntity<SwissSocialInsurances> createTsvSwissSocialInsurances() {
        Function<CSVRecord, SwissSocialInsurances> of = null;
        List<TsvProperty<SwissSocialInsurances, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissSocialInsurances::from, null),
                TsvProperty.ofDate("toDate", SwissSocialInsurances::to, null));
        return TsvEntity.of(SwissSocialInsurances.class, fields, of);
    }
}
