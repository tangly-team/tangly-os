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

package net.tangly.erp.collaborators.ports;

import net.tangly.core.providers.Provider;
import net.tangly.erp.collaborators.domain.*;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.erp.collabortors.services.CollaboratorsRealm;
import net.tangly.erp.ports.TsvHdl;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CollaboratorsTsvHdl {
    private final CollaboratorsRealm realm;

    public CollaboratorsTsvHdl(CollaboratorsRealm realm) {
        this.realm = realm;
    }

    public void importCollaboratators(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CollaboratorsBoundedDomain.DOMAIN, reader, source, createTsvCollaborator(), realm.collaborators());
    }

    public void exportCollaborators(@NotNull Path path) {
        TsvHdl.exportEntities(CollaboratorsBoundedDomain.DOMAIN, path, createTsvCollaborator(), realm.collaborators());
    }


    static TsvEntity<Collaborator> createTsvCollaborator() {
        Function<CSVRecord, Collaborator> of = (csv) -> new Collaborator(TsvEntity.get(csv, "id"), TsvEntity.get(csv, "oldSocialSecurityNumber"),
            null, TsvEntity.get(csv, "fullname"), TsvEntity.get(csv, "internalId"), null);
        List<TsvProperty<Collaborator, ?>> fields =
            List.of(TsvProperty.ofString("id", Collaborator::id, null),
                TsvProperty.ofString("oldSocialSecurityNumber", Collaborator::oldSocialSecurityNumber, null),
                TsvProperty.ofDate("birthday", Collaborator::birthday, null),
                TsvProperty.ofString("fullname", Collaborator::fullname, null),
                TsvProperty.ofString("internalId", Collaborator::internalId, null));
        return TsvEntity.of(Collaborator.class, fields, of);
    }

    static TsvEntity<Contract> createTsvContract() {
        Function<CSVRecord, Contract> of = null;
        List<TsvProperty<Contract, ?>> fields = null;
//            List.of(TsvProperty.ofDate("fromDate", Contract::from, null),
//                TsvProperty.ofDate("toDate", Contract::to, null),
//                TsvProperty.ofLong("collaboratorOid", Contract::naturalEntityOid, null));
        return TsvEntity.of(Contract.class, fields, of);
    }

    static TsvEntity<SwissAccidentInsurance> createTsvSwissInsurances() {
        Function<CSVRecord, SwissAccidentInsurance> of = null;
        List<TsvProperty<SwissAccidentInsurance, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissAccidentInsurance::from, null),
                TsvProperty.ofDate("toDate", SwissAccidentInsurance::to, null));
        return TsvEntity.of(SwissAccidentInsurance.class, fields, of);
    }

    static TsvEntity<SwissPensionFund> createTsvSwissPensionFunds() {
        Function<CSVRecord, SwissPensionFund> of = null;
        List<TsvProperty<SwissPensionFund, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissPensionFund::from, null),
                TsvProperty.ofDate("toDate", SwissPensionFund::to, null));
        return TsvEntity.of(SwissPensionFund.class, fields, of);
    }

    static TsvEntity<SwissSocialInsurance> createTsvSwissSocialInsurances() {
        Function<CSVRecord, SwissSocialInsurance> of = null;
        List<TsvProperty<SwissSocialInsurance, ?>> fields =
            List.of(TsvProperty.ofDate("fromDate", SwissSocialInsurance::from, null),
                TsvProperty.ofDate("toDate", SwissSocialInsurance::to, null));
        return TsvEntity.of(SwissSocialInsurance.class, fields, of);
    }

    private Optional<Organization> findOrganizationById(String identifier) {
        return (identifier != null) ? Provider.findById(realm.organizations(), identifier) : Optional.empty();
    }
}