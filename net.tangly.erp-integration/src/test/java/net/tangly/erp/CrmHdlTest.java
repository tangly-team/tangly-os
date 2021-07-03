/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.time.LocalDate;
import java.time.Month;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import net.tangly.core.Comment;
import net.tangly.core.PhoneNr;
import net.tangly.core.Tag;
import net.tangly.core.crm.CrmTags;
import net.tangly.core.crm.VcardType;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.GenderCode;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.domain.NaturalEntity;
import net.tangly.erp.crm.services.CrmRealm;
import net.tangly.erpr.crm.ports.CrmEntities;
import net.tangly.erpr.crm.ports.CrmHdl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static net.tangly.core.providers.Provider.findById;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the import and export of CRM entities to TSV files. All files are either defined as resources or written to an in-memory file system. The export
 * function should verify the correct creation and writing of an additional instance for each bounded domain entity.
 */
class CrmHdlTest {
    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            var handler = new CrmHdl(new CrmEntities(), store.crmRoot());
            handler.importEntities();

            verifyNaturalEntities(handler.realm(), 0, null);
            verifyLegalEntities(handler.realm(), 0, null);
            verifyEmployees(handler.realm());
            verifyContracts(handler.realm(), 0, null);
            verifyInteractions(handler.realm());
            verifyActivities(handler.realm());
            verifySubjects(handler.realm());
            verifyComments(handler.realm());

            NaturalEntity naturalEntity = createNaturalEntity();
            handler.realm().naturalEntities().update(naturalEntity);
            int nrOfNaturalEntities = handler.realm().naturalEntities().items().size();

            LegalEntity legalEntity = createLegalEntity();
            handler.realm().legalEntities().update(legalEntity);
            int nrOfLegalEntities = handler.realm().legalEntities().items().size();

            Contract contract = createContract(handler.realm());
            handler.realm().contracts().update(contract);
            int nrOfContracts = handler.realm().contracts().items().size();

            handler.exportEntities();

            handler = new CrmHdl(new CrmEntities(), store.crmRoot());
            handler.importEntities();
            verifyNaturalEntities(handler.realm(), nrOfNaturalEntities, naturalEntity);
            verifyLegalEntities(handler.realm(), nrOfLegalEntities, legalEntity);
            verifyEmployees(handler.realm());
            verifyContracts(handler.realm(), nrOfContracts, contract);
            verifyInteractions(handler.realm());
            verifyActivities(handler.realm());
            verifySubjects(handler.realm());
            verifyComments(handler.realm());
        }
    }

    private void verifyNaturalEntities(@NotNull CrmRealm realm, int nrOfEntities, NaturalEntity entity) {
        assertThat(realm.naturalEntities().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.naturalEntities(), 1)).isPresent();
        Realm.checkEntities(realm.naturalEntities());
        if (entity != null) {
            assertThat(realm.naturalEntities().items()).hasSize(nrOfEntities);
            Optional<NaturalEntity> copy = realm.naturalEntities().findBy(NaturalEntity::socialNr, entity.socialNr());
            assertThat(copy).isNotEmpty();
            assertThat(copy.get()).isEqualTo(entity);
        }
    }

    private void verifyLegalEntities(@NotNull CrmRealm realm, int nrOfEntities, LegalEntity entity) {
        assertThat(realm.legalEntities().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.legalEntities(), 100)).isPresent();
        assertThat(realm.legalEntities().findBy(LegalEntity::id, "UNKNOWN-100")).isPresent();
        assertThat(realm.legalEntities().findBy(LegalEntity::name, "Vaadin GbmH")).isPresent();
        Realm.checkEntities(realm.legalEntities());
        if (entity != null) {
            assertThat(realm.legalEntities().items()).hasSize(nrOfEntities);
            Optional<LegalEntity> copy = findById(realm.legalEntities(), entity.id());
            assertThat(copy).isNotEmpty();
            assertThat(copy.get()).isEqualTo(entity);
        }
    }

    private void verifyEmployees(@NotNull CrmRealm realm) {
        assertThat(realm.employees().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.employees(), 200)).isPresent();
        Realm.checkEntities(realm.employees());
    }

    private void verifyContracts(@NotNull CrmRealm realm, int nrOfEntities, Contract entity) {
        assertThat(realm.contracts().items().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.contracts(), 500)).isPresent();
        Realm.checkEntities(realm.contracts());
        if (entity != null) {
            assertThat(realm.contracts().items()).hasSize(nrOfEntities);
            Optional<Contract> copy = findById(realm.contracts(), entity.id());
            assertThat(copy).isNotEmpty();
            assertThat(copy.get()).isEqualTo(entity);
        }
    }

    private void verifyInteractions(@NotNull CrmRealm realm) {
        assertThat(realm.interactions().items()).isNotEmpty();
        Realm.checkEntities(realm.interactions());
    }

    private void verifyActivities(@NotNull CrmRealm realm) {
        assertThat(realm.collectActivities(o -> true)).isNotEmpty();
        realm.collectActivities(o -> true).forEach(activity -> assertThat(activity.check()).isTrue());
    }

    private void verifySubjects(@NotNull CrmRealm realm) {
        assertThat(realm.subjects().items().isEmpty()).isTrue();
        Realm.checkEntities(realm.subjects());
    }

    private void verifyComments(@NotNull CrmRealm realm) {
        assertThat(Provider.findByOid(realm.naturalEntities(), 1).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.naturalEntities(), 6).orElseThrow().comments().isEmpty()).isFalse();
        assertThat(Provider.findByOid(realm.legalEntities(), 102).orElseThrow().comments().isEmpty()).isFalse();
    }

    private NaturalEntity createNaturalEntity() {
        NaturalEntity entity = new NaturalEntity();
        entity.fromDate(LocalDate.of(1900, Month.JANUARY, 1));
        entity.toDate(LocalDate.of(2000, Month.DECEMBER, 31));
        entity.text("*This is a markdown text*");
        entity.add(Comment.of("administrator", "*This is a markdown comment for natural entity*"));
        entity.socialNr("social-security-number");
        entity.firstname("John");
        entity.lastname("Doe");
        entity.gender(GenderCode.unspecified);
        entity.address(VcardType.home, Address.builder().country("CH").region("ZH").locality("Zürich").build());
        entity.phoneNr(VcardType.home, "+41 79 778 8689");
        entity.phoneNr(VcardType.mobile, "+41 79 778 8689");
        return entity;
    }

    private LegalEntity createLegalEntity() {
        LegalEntity entity = new LegalEntity();
        entity.id("CHE-487.951.218");
        entity.name("bbv Group AG");
        entity.fromDate(LocalDate.of(1995, Month.NOVEMBER, 1));
        entity.text("*This is a markdown text*");
        entity.add(Comment.of("administrator", "*This is a markdown comment for legal entity*"));
        entity.add(Tag.of(CrmTags.CRM_SITE_WORK, "www.bbv.ch"));
        entity.add(Tag.of(CrmTags.CRM_PHONE_WORK, PhoneNr.of("+41 41 429 01 11").number()));
        entity.vatNr("CHE-487.951.218");
        entity.email(VcardType.work, "info@bbv.net");
        entity.address(VcardType.work, Address.builder().country("CH").region("LU").locality("Lucerne").postcode("6002").street("Blumenrain 10").build());
        return entity;
    }

    private Contract createContract(@NotNull CrmRealm realm) {
        Contract entity = new Contract();
        entity.id("Contract-009");
        entity.name("Contract 009");
        entity.fromDate(LocalDate.of(2010, Month.MARCH, 1));
        entity.toDate(LocalDate.of(2010, Month.DECEMBER, 31));
        entity.text("*This is a markdown text for a contract*");
        entity.address(Address.builder().country("CH").region("LU").locality("Lucerne").postcode("6002").street("Blumenrain 10").build());
        entity.bankConnection(BankConnection.of("CH88 0900 0000 3064 1768 2", "POFICHBEXXX", "Postfinanz Schweiz"));
        entity.add(Comment.of("administrator", "*This is a markdown comment for contract*"));
        entity.seller(findById(realm.legalEntities(), "CHE-357.875.339").orElseThrow());
        entity.sellee(findById(realm.legalEntities(), "FR9201.998269211").orElseThrow());
        entity.amountWithoutVat(BigDecimal.ONE);
        entity.locale(Locale.ENGLISH);
        entity.currency(Currency.getInstance("EUR"));
        return entity;
    }
}
