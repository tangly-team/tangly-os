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
import net.tangly.commons.models.Tag;
import net.tangly.erp.crm.CrmTags;
import net.tangly.erp.crm.Employee;
import net.tangly.erp.crm.LegalEntity;
import net.tangly.erp.crm.NaturalEntity;
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
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final String LINKEDIN = "linkedIn";
    private static final String VAT_NR = "vatNr";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CrmCsvHdl.class);

    private final List<NaturalEntity> naturalEntities;
    private final List<LegalEntity> legalEntities;
    private final List<Employee> employees;

    public CrmCsvHdl() {
        naturalEntities = new ArrayList<>();
        legalEntities = new ArrayList<>();
        employees = new ArrayList<>();
    }

    public List<NaturalEntity> importNaturalEntities(@NotNull Path path) throws IOException {
        List<NaturalEntity> entities = new ArrayList<>();
        Reader in = new BufferedReader(new FileReader(path.toFile()));
        Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (record != null) {
            String oid = get(record, OID);
            String id = get(record, ID);
            String lastname = get(record, "lastname");
            String firstname = get(record, "firstname");
            String fromDate = get(record, FROM_DATE);
            String toDate = get(record, TO_DATE);
            String emailHome = get(record, "email-home");
            String phoneMobile = get(record, "phone-mobile");
            String linkedIn = get(record, LINKEDIN);
            NaturalEntity entity = NaturalEntity.of(Long.valueOf(oid), id, lastname, firstname);
            entity.fromDate((fromDate != null) ? LocalDate.parse(fromDate) : null);
            entity.toDate((toDate != null) ? LocalDate.parse(toDate) : null);
            entity.setAddress(CrmTags.HOME, importAddress(record));
            entity.setEmail(CrmTags.HOME, emailHome);
            entity.setPhoneNr(CrmTags.MOBILE, phoneMobile);
            entity.setIm("linkedin", linkedIn);
            entities.add(entity);
            record = records.hasNext() ? records.next() : null;
        }
        naturalEntities.addAll(entities);
        return entities;
    }

    public List<LegalEntity> importLegalEntities(@NotNull Path path) throws IOException {
        List<LegalEntity> entities = new ArrayList<>();
        Reader in = new BufferedReader(new FileReader(path.toFile()));
        Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (record != null) {
            String oid = get(record, OID);
            String id = get(record, ID);
            String name = get(record, "name");
            String fromDate = get(record, FROM_DATE);
            String toDate = get(record, TO_DATE);
            String emailWork = get(record, "email-work");
            String phoneMain = get(record, "phone-main");
            String siteWork = get(record, "site-work");
            String linkedIn = get(record, LINKEDIN);
            String vatNr = get(record, VAT_NR);
            LegalEntity entity = LegalEntity.of(Long.valueOf(oid), id, name);
            entity.fromDate((fromDate != null) ? LocalDate.parse(fromDate) : null);
            entity.toDate((toDate != null) ? LocalDate.parse(toDate) : null);
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
        legalEntities.addAll(entities);
        return entities;
    }

    public List<Employee> importEmployees(@NotNull Path path) throws IOException {
        List<Employee> entities = new ArrayList<>();
        Reader in = new BufferedReader(new FileReader(path.toFile()));
        Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (record != null) {
            String oid = get(record, OID);
            String id = get(record, ID);
            String personOid = get(record, "personOid");
            String organizationOid = get(record, "organizationOid");
            String fromDate = get(record, FROM_DATE);
            String toDate = get(record, TO_DATE);
            String title = get(record, "title");
            String emailWork = get(record, "email-work");
            String phoneWork = get(record, "phone-work");
            Employee entity = new Employee(Long.valueOf(oid), id);

            findNaturalEntityByOid(personOid).ifPresent(entity::person);
            findLegalEntityByOid(organizationOid).ifPresent(entity::organization);

            entity.fromDate((fromDate != null) ? LocalDate.parse(fromDate) : null);
            entity.toDate((toDate != null) ? LocalDate.parse(toDate) : null);
            entity.setEmail(CrmTags.WORK, emailWork);
            entity.setPhoneNr(CrmTags.WORK, phoneWork);
            entity.setTag("title", title);
            entities.add(entity);
            record = records.hasNext() ? records.next() : null;
        }
        employees.addAll(entities);
        return entities;
    }

    public Address importAddress(@NotNull CSVRecord record) {
        String street = Strings.nullToEmpty(record.get("street"));
        String postcode = Strings.nullToEmpty(record.get("postcode"));
        String locality = Strings.nullToEmpty(record.get("locality"));
        String region = Strings.nullToEmpty(record.get("region"));
        String country = Strings.nullToEmpty(record.get("country"));
        return new Address(street, postcode, locality, region, country);
    }

    public String get(CSVRecord record, String column) {
        return Strings.emptyToNull(record.get(column));
    }

    private Optional<NaturalEntity> findNaturalEntityByOid(String identifier) {
        if (identifier != null) {
            long oid = Long.parseLong(identifier);
            return naturalEntities.stream().filter(o -> o.oid() == oid).findAny();

        } else {
            return Optional.empty();
        }
    }

    private Optional<LegalEntity> findLegalEntityByOid(String identifier) {
        if (identifier != null) {
            long oid = Long.parseLong(identifier);
            return legalEntities.stream().filter(o -> o.oid() == oid).findAny();

        } else {
            return Optional.empty();
        }
    }
}
