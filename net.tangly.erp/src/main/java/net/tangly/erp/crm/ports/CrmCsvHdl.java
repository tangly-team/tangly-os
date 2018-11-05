/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.crm.ports;

import com.google.common.base.Strings;
import net.tangly.commons.models.Address;
import net.tangly.commons.models.EntityImp;
import net.tangly.commons.models.Tag;
import net.tangly.erp.crm.*;
import net.tangly.erp.crm.apps.Crm;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CrmCsvHdl {
    private static final String OID = "oid";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final String TEXT = "text";
    private static final String LINKEDIN = "linkedIn";
    private static final String VAT_NR = "vatNr";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CrmCsvHdl.class);

    private final Crm crm;

    public CrmCsvHdl(Crm crm) {
        this.crm = crm;
    }

    public List<NaturalEntity> importNaturalEntities(@NotNull Path path) throws IOException {
        List<NaturalEntity> entities = new ArrayList<>();
        try (Reader in = new BufferedReader(new FileReader(path.toFile()))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                NaturalEntity entity = NaturalEntity
                        .of(Long.valueOf(get(record, OID)), get(record, ID), get(record, "lastname"), get(record, "firstname"));
                updateEntity(record, entity);
                entity.setAddress(CrmTags.HOME, importAddress(record));
                entity.setEmail(CrmTags.HOME, get(record, "email-home"));
                entity.setPhoneNr(CrmTags.MOBILE, get(record, "phone-mobile"));
                entity.setIm("linkedin", get(record, LINKEDIN));
                entities.add(entity);
                record = records.hasNext() ? records.next() : null;
            }
            crm.addNaturalEntities(entities);
        }
        return entities;
    }

    public List<LegalEntity> importLegalEntities(@NotNull Path path) throws IOException {
        List<LegalEntity> entities = new ArrayList<>();
        try (Reader in = new BufferedReader(new FileReader(path.toFile()))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                String emailWork = get(record, "email-work");
                String phoneMain = get(record, "phone-main");
                String siteWork = get(record, "site-work");
                String linkedIn = get(record, LINKEDIN);
                String vatNr = get(record, VAT_NR);
                LegalEntity entity = LegalEntity.of(Long.valueOf(get(record, OID)), get(record, ID));
                updateEntity(record, entity);
                entity.setAddress(CrmTags.HOME, importAddress(record));
                entity.setEmail(CrmTags.WORK, emailWork);
                entity.setPhoneNr(CrmTags.WORK, phoneMain);
                entity.vatNr(vatNr);
                try {
                    entity.setSite(CrmTags.WORK, (siteWork != null) ? new URI(siteWork) : null);
                } catch (URISyntaxException e) {
                    log.error("Erroneous URI syntax {}", siteWork, e);
                }
                entity.setIm("linkedin", linkedIn);
                if (siteWork != null) {
                    entity.replace(Tag.of(CrmTags.CRM_SITE_WORK, siteWork));
                }
                entities.add(entity);
                record = records.hasNext() ? records.next() : null;
            }
            crm.addLegalEntities(entities);
        }
        return entities;
    }

    public List<Employee> importEmployees(@NotNull Path path) throws IOException {
        List<Employee> entities = new ArrayList<>();
        try (Reader in = new BufferedReader(new FileReader(path.toFile()))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                Employee entity = new Employee(Long.valueOf(get(record, OID)), get(record, ID));
                updateEntity(record, entity);
                findNaturalEntityByOid(get(record, "personOid")).ifPresent(entity::person);
                findLegalEntityByOid(get(record, "organizationOid")).ifPresent(entity::organization);
                entity.setEmail(CrmTags.WORK, get(record, "email-work"));
                entity.setPhoneNr(CrmTags.WORK, get(record, "phone-work"));
                entity.setTag("title", get(record, "title"));
                entities.add(entity);
                record = records.hasNext() ? records.next() : null;
            }
            crm.addEmployees(entities);
        }
        return entities;
    }

    public List<Contract> importContracts(@NotNull Path path) throws IOException {
        List<Contract> entities = new ArrayList<>();
        try (Reader in = new BufferedReader(new FileReader(path.toFile()))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                record = records.hasNext() ? records.next() : null;
                Contract entity = new Contract(Long.valueOf(get(record, OID)), get(record, ID));
                updateEntity(record, entity);
                findLegalEntityByOid(get(record, "sellerOid")).ifPresent(entity::seller);
                findLegalEntityByOid(get(record, "selleeOid")).ifPresent(entity::sellee);
            }
            crm.addContracts(entities);
        }
        return entities;
    }

    public BankConnection importBankConnection(@NotNull CSVRecord record) {
        String iban = get(record, "iban");
        String bic = get(record, "bic");
        String institute = get(record, "institute");
        return new BankConnection(iban, bic, institute);
    }

    public Address importAddress(@NotNull CSVRecord record) {
        String street = get(record, "street");
        String postcode = get(record, "postcode");
        String locality = get(record, "locality");
        String region = get(record, "region");
        String country = get(record, "country");
        return Address.builder().street(street).postcode(postcode).locality(locality).region(region).country(country).build();
    }

    private void updateEntity(@NotNull CSVRecord record, EntityImp entity) {
        entity.name(get(record, NAME));
        String fromDate = get(record, FROM_DATE);
        entity.fromDate((fromDate != null) ? LocalDate.parse(fromDate) : null);
        String toDate = get(record, TO_DATE);
        entity.toDate((toDate != null) ? LocalDate.parse(toDate) : null);
        entity.text(get(record, TEXT));
    }

    private String get(CSVRecord record, String column) {
        return Strings.emptyToNull(record.get(column));
    }

    private Optional<NaturalEntity> findNaturalEntityByOid(String identifier) {
        if (identifier != null) {
            long oid = Long.parseLong(identifier);
            return crm.naturalEntities().stream().filter(o -> o.oid() == oid).findAny();

        } else {
            return Optional.empty();
        }
    }

    private Optional<LegalEntity> findLegalEntityByOid(String identifier) {
        if (identifier != null) {
            long oid = Long.parseLong(identifier);
            return crm.legalEntities().stream().filter(o -> o.oid() == oid).findAny();

        } else {
            return Optional.empty();
        }
    }
}
