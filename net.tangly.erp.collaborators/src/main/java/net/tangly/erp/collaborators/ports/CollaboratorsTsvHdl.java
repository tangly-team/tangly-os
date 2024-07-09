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

import net.tangly.core.HasId;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.TsvHdl;
import net.tangly.core.providers.Provider;
import net.tangly.core.tsv.TsvHdlCore;
import net.tangly.erp.collaborators.domain.*;
import net.tangly.erp.collabortors.services.CollaboratorsRealm;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.tangly.core.tsv.TsvHdlCore.FROM_DATE;
import static net.tangly.core.tsv.TsvHdlCore.TO_DATE;

public class CollaboratorsTsvHdl {
    private final CollaboratorsRealm realm;

    public CollaboratorsTsvHdl(CollaboratorsRealm realm) {
        this.realm = realm;
    }

    public void importCollaboratators(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvCollaborator(), realm.collaborators());
    }

    public void exportCollaborators(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvCollaborator(), realm.collaborators());
    }

    static TsvEntity<Collaborator> createTsvCollaborator() {
        Function<CSVRecord, Collaborator> of = (csv) ->
            new Collaborator(TsvEntity.get(csv, HasId.ID), TsvEntity.get(csv, "oldSocialSecurityNumber"),
                TsvHdlCore.ofDate(csv, "birthday"), TsvEntity.get(csv, "fullname"), TsvEntity.get(csv, "internalId"), TsvHdlCore.ofAddress(csv));
        List<TsvProperty<Collaborator, ?>> fields =
            List.of(TsvProperty.ofString(HasId.ID, Collaborator::id),
                TsvProperty.ofString("oldSocialSecurityNumber", Collaborator::oldSocialSecurityNumber),
                TsvProperty.ofDate("birthday", Collaborator::birthday),
                TsvProperty.ofString("fullname", Collaborator::fullname),
                TsvProperty.ofString("internalId", Collaborator::internalId),
                TsvProperty.of(TsvHdlCore.createTsvAddress(), Collaborator::address, null));
        return TsvEntity.of(Collaborator.class, fields, of);
    }

    static TsvEntity<Contract> createTsvContract() {
        Function<CSVRecord, Contract> of = null;
        List<TsvProperty<Contract, ?>> fields = null;
        List.of(
            TsvProperty.ofString(HasId.ID, Contract::id),
            TsvProperty.ofString("organizationId", (Contract o) -> o.organization().id()),
            TsvProperty.ofString("collaboratorId", (Contract o) -> o.collaborator().id()),
            TsvProperty.ofDate(FROM_DATE, Contract::from),
            TsvProperty.ofDate(TO_DATE, Contract::to),
            TsvProperty.ofBigDecimal("yearlySalary", Contract::yearlySalary),
            TsvProperty.ofBigDecimal("workPercentage", Contract::workPercentage),
            TsvProperty.ofInt("nrOfPayments", Contract::nrOfPayments, null));
        return TsvEntity.of(Contract.class, fields, of);
    }

    static TsvEntity<SwissAccidentInsurance> createTsvSwissInsurances() {
        Function<CSVRecord, SwissAccidentInsurance> of = null;
        List<TsvProperty<SwissAccidentInsurance, ?>> fields =
            List.of(TsvProperty.ofDate(FROM_DATE, SwissAccidentInsurance::from),
                TsvProperty.ofDate(TO_DATE, SwissAccidentInsurance::to));
        return TsvEntity.of(SwissAccidentInsurance.class, fields, of);
    }

    static TsvEntity<SwissPensionFund> createTsvSwissPensionFunds() {
        Function<CSVRecord, SwissPensionFund> of = null;
        List<TsvProperty<SwissPensionFund, ?>> fields =
            List.of(TsvProperty.ofDate(FROM_DATE, SwissPensionFund::from),
                TsvProperty.ofDate(TO_DATE, SwissPensionFund::to));
        return TsvEntity.of(SwissPensionFund.class, fields, of);
    }

    static TsvEntity<SwissSocialInsurance> createTsvSwissSocialInsurances() {
        Function<CSVRecord, SwissSocialInsurance> of = null;
        List<TsvProperty<SwissSocialInsurance, ?>> fields =
            List.of(TsvProperty.ofDate(FROM_DATE, SwissSocialInsurance::from),
                TsvProperty.ofDate(TO_DATE, SwissSocialInsurance::to));
        return TsvEntity.of(SwissSocialInsurance.class, fields, of);
    }

    private Optional<Organization> findOrganizationById(String identifier) {
        return (identifier != null) ? Provider.findById(realm.organizations(), identifier) : Optional.empty();
    }
}
