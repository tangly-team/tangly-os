/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.erp.crm.ports;

import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.core.*;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.TsvHdl;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.tsv.TsvHdlCore;
import net.tangly.erp.crm.domain.*;
import net.tangly.erp.crm.services.CrmRealm;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.gleam.model.TsvRelation;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static net.tangly.core.tsv.TsvHdlCore.*;
import static net.tangly.erp.crm.domain.CrmTags.*;
import static net.tangly.gleam.model.TsvEntity.get;

/**
 * Provide import and export functions for CRM entities persisted in tab-separated files. These files are often defined for integration testing or integration of legacy systems, not
 * providing programmatic API. The description of the TSV file structure and the mapping rules are of a declarative nature. One2one relations are mapped
 * through the oid of the
 * referenced entity if defined, otherwise an empty string. One2many relations are mapped through a tab-separated list for the oid of the referenced entities if at least one is
 * defined, otherwise an empty string.
 */
public class CrmTsvHdl {
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String CREATED = "created";
    private static final String GENDER = "gender";
    private static final String EMAIL = "email";
    private static final String AUTHOR = "author";
    private static final String TAGS = "tags";
    private final CrmRealm realm;

    public CrmTsvHdl(@NotNull CrmRealm realm) {
        this.realm = realm;
    }

    public void importComments(@NotNull DomainAudit audit, @NotNull Path path) {
        var comments = TsvHdl.importRelations(audit, path, createTsvComment());
        TsvHdl.addComments(realm.naturalEntities(), comments);
        TsvHdl.addComments(realm.legalEntities(), comments);
        TsvHdl.addComments(realm.employees(), comments);
        TsvHdl.addComments(realm.contracts(), comments);
        TsvHdl.addComments(realm.opportunities(), comments);
    }

    public void exportComments(@NotNull DomainAudit audit, @NotNull Path path) {
        List<TsvRelation<Comment>> comments = new ArrayList<>();
        realm.naturalEntities().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.legalEntities().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.employees().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.contracts().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.opportunities().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        TsvHdl.exportRelations(audit, path, createTsvComment(), comments);
    }

    public void importLeads(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvLead(), realm.leads());
    }

    public void exportLeads(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvLead(), realm.leads());
    }

    public void importNaturalEntities(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvNaturalEntity(), realm.naturalEntities());
    }

    public void exportNaturalEntities(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvNaturalEntity(), realm.naturalEntities());
    }

    public void importLegalEntities(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvLegalEntity(), realm.legalEntities());
    }

    public void exportLegalEntities(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvLegalEntity(), realm.legalEntities());
    }

    public void importEmployees(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvEmployee(), realm.employees());
    }

    public void exportEmployees(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvEmployee(), realm.employees());
    }

    public void importContracts(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvContract(), realm.contracts());
    }

    public void importContractExtensions(@NotNull DomainAudit audit, @NotNull Path path) {
        Provider<ContractExtension> extensions = new ProviderInMemory<>();
        TsvHdl.importEntities(audit, path, createTsvContractExtension(), extensions);
        extensions.items().forEach(e -> Provider.findById(realm.contracts(), e.contractId()).ifPresent(c -> {
            c.add(e);
            realm.contracts().update(c);
        }));
    }

    public void exportContracts(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvContract(), realm.contracts());
    }

    public void exportContractExtensions(@NotNull DomainAudit audit, @NotNull Path path) {
        Provider<ContractExtension> extensions = new ProviderInMemory<>();
        realm.contracts().items().forEach(e -> extensions.updateAll(e.contractExtensions()));
        TsvHdl.exportEntities(audit, path, createTsvContractExtension(), extensions);
    }

    public void importOpportunities(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.importEntities(audit, path, createTsvOpportunity(), realm.opportunities());
    }

    public void exportOpportunities(@NotNull DomainAudit audit, @NotNull Path path) {
        TsvHdl.exportEntities(audit, path, createTsvOpportunity(), realm.opportunities());
    }

    public void importActivities(@NotNull DomainAudit audit, @NotNull Path path) {
        var activities = TsvHdl.importRelations(audit, path, createTsvActivity());
        realm.opportunities().items().forEach(e -> addActivities(realm.opportunities(), e, activities));
    }

    public void exportActivities(@NotNull DomainAudit audit, @NotNull Path path) {
        List<TsvRelation<Activity>> activities = new ArrayList<>();
        realm.opportunities().items().forEach(o -> activities.addAll(updateAndCollectActivities(o)));
        TsvHdl.exportRelations(audit, path, createTsvActivity(), activities);
    }

    private static <T extends HasMutableComments & HasOid> List<TsvRelation<Comment>> updateAndCollectComments(T entity) {
        List<TsvRelation<Comment>> comments = new ArrayList<>();
        entity.comments().forEach(comment -> comments.add(new TsvRelation<>(entity.oid(), comment)));
        return comments;
    }

    private static List<TsvRelation<Activity>> updateAndCollectActivities(@NotNull Opportunity entity) {
        List<TsvRelation<Activity>> activities = new ArrayList<>();
        entity.activities().forEach(activity -> activities.add(new TsvRelation<>(entity.oid(), activity)));
        return activities;
    }

    private static void addActivities(Provider<Opportunity> provider, Opportunity entity, List<TsvRelation<Activity>> activities) {
        var items = activities.stream().filter(o -> o.ownerId() == entity.oid()).map(TsvRelation::ownedEntity).toList();
        if (!items.isEmpty()) {
            entity.activities(items);
            provider.update(entity);
        }
    }

    private static TsvEntity<Comment> createTsvComment() {
        Function<CSVRecord, Comment> imports = (CSVRecord item) -> new Comment(LocalDateTime.parse(get(item, CREATED)), get(item, AUTHOR), get(item, TEXT),
            Tag.toTags(get(item, TAGS)));
        List<TsvProperty<Comment, ?>> fields = List.of(TsvProperty.ofEmpty(TsvEntity.OWNER_FOID),
            TsvProperty.of(CREATED, Comment::created, null, o -> (o != null) ? LocalDateTime.parse(o) : null),
            TsvProperty.ofString(AUTHOR, Comment::author),
            TsvProperty.ofString(TEXT, Comment::text), TsvProperty.ofString(TAGS, HasTags::rawTags));
        return TsvEntity.of(Comment.class, fields, imports);
    }

    private static TsvEntity<NaturalEntity> createTsvNaturalEntity() {
        List<TsvProperty<NaturalEntity, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.ofString(FIRSTNAME, NaturalEntity::firstname, NaturalEntity::firstname));
        fields.add(TsvProperty.ofEnum(GenderCode.class, GENDER, NaturalEntity::gender, NaturalEntity::gender));
        fields.add(emailProperty(CRM_EMAIL_HOME, VcardType.home));
        fields.add(phoneNrProperty(CRM_PHONE_MOBILE, VcardType.mobile));
        fields.add(phoneNrProperty(CRM_PHONE_HOME, VcardType.home));
        fields.add(TsvHdl.tagProperty(CRM_IM_LINKEDIN));
        fields.add(TsvHdl.tagProperty(CRM_SITE_HOME));
        fields.add(createAddressMapping(VcardType.home));
        fields.add(TsvProperty.ofString(TAGS, HasMutableTags::rawTags, HasMutableTags::rawTags));
        return TsvHdl.of(NaturalEntity.class, fields, NaturalEntity::new);
    }

    private static TsvEntity<LegalEntity> createTsvLegalEntity() {
        List<TsvProperty<LegalEntity, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.ofString(CRM_VAT_NUMBER, LegalEntity::vatNr, LegalEntity::vatNr));
        fields.add(TsvHdl.tagProperty(CRM_IM_LINKEDIN));
        fields.add(emailProperty(CRM_EMAIL_WORK, VcardType.work));
        fields.add(TsvHdl.tagProperty(CRM_SITE_WORK));
        fields.add(phoneNrProperty(CRM_PHONE_WORK, VcardType.work));
        fields.add(TsvHdl.tagProperty(CRM_RESPONSIBLE));
        fields.add(createAddressMapping(VcardType.work));
        fields.add(TsvProperty.ofString(CRM_SCHOOL, e -> (e.containsTag(CRM_SCHOOL) ? "Y" : "N"), (e, p) -> {
            if ("Y".equals(p)) {
                e.update(CRM_SCHOOL, null);
            }
        }));
        fields.add(TsvHdl.tagProperty(GEO_PLUSCODE));
        fields.add(TsvHdl.tagProperty(GEO_LATITUDE));
        fields.add(TsvHdl.tagProperty(GEO_LONGITUDE));
        fields.add(TsvProperty.ofString(TAGS, HasMutableTags::rawTags, HasMutableTags::rawTags));
        return TsvHdl.of(LegalEntity.class, fields, LegalEntity::new);
    }

    private static TsvEntity<Activity> createTsvActivity() {
        List<TsvProperty<Activity, ?>> fields = List.of(TsvProperty.ofEmpty(TsvEntity.OWNER_FOID),
            TsvProperty.of("code", Activity::code, Activity::code, e -> TsvProperty.valueOf(ActivityCode.class, e), Enum::name),
            TsvProperty.ofDate("date", Activity::date, Activity::date), TsvProperty.ofInt("durationInMinutes", Activity::duration, Activity::duration),
            TsvProperty.ofString(AUTHOR, Activity::author, Activity::author), TsvProperty.ofString(TEXT, Activity::text, Activity::text));
        return TsvEntity.of(Activity.class, fields, Activity::new);
    }

    private static TsvEntity<Lead> createTsvLead() {
        Function<CSVRecord, Lead> imports =
            (CSVRecord csv) -> new Lead(LocalDate.parse(get(csv, DATE)), Enum.valueOf(LeadCode.class, get(csv, TsvHdl.CODE)), get(csv, FIRSTNAME),
                get(csv, LASTNAME), Enum.valueOf(GenderCode.class, get(csv, GENDER)), get(csv, "company"), PhoneNr.of(get(csv, "phoneNr")),
                EmailAddress.of(get(csv, EMAIL)), get(csv, "linkedIn"), get(csv, TEXT));

        List<TsvProperty<Lead, ?>> fields = List.of(TsvProperty.ofDate(DATE, Lead::date, null), TsvProperty.ofEnum(LeadCode.class, "code", Lead::code, null),
            TsvProperty.ofString(FIRSTNAME, Lead::firstname), TsvProperty.ofString(LASTNAME, Lead::firstname),
            TsvProperty.ofEnum(GenderCode.class, GENDER, Lead::gender, null), TsvProperty.ofString("company", Lead::company),
            TsvProperty.ofString("phoneNr", o -> Objects.nonNull(o.phoneNr()) ? o.phoneNr().number() : null),
            TsvProperty.ofString(EMAIL, o -> Objects.nonNull(o.email()) ? o.email().text() : null), TsvProperty.ofString("linkedIn", Lead::linkedIn, null),
            TsvProperty.ofString(TEXT, Lead::text));
        return TsvEntity.of(Lead.class, fields, imports);
    }

    private TsvEntity<Employee> createTsvEmployee() {
        List<TsvProperty<Employee, ?>> fields =
            List.of(TsvProperty.of(HasOid.OID, Employee::oid, (entity, value) -> ReflectionUtilities.set(entity, HasOid.OID, value), Long::parseLong),
                TsvProperty.of(TsvHdlCore.createTsvDateRange(), MutableEntity::range, MutableEntity::range),
                TsvProperty.ofString(TEXT, Employee::text, Employee::text),
                TsvProperty.of("personOid", Employee::person, Employee::person, e -> findNaturalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()),
                TsvProperty.of("organizationOid", Employee::organization, Employee::organization, e -> findLegalEntityByOid(e).orElse(null),
                    TsvHdl.convertFoidTo()), TsvHdl.tagProperty(CRM_EMPLOYEE_TITLE), TsvHdl.tagProperty(CRM_EMAIL_WORK),
                TsvProperty.ofString(CRM_PHONE_WORK, e -> e.phoneNr(VcardType.work).map(PhoneNr::number).orElse(""), (e, p) -> e.phoneNr(VcardType.work, p)),
                phoneNrProperty(CRM_PHONE_MOBILE, VcardType.mobile));
        return TsvHdl.of(Employee.class, fields, Employee::new);
    }

    private TsvEntity<Contract> createTsvContract() {
        List<TsvProperty<Contract, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.of(LOCALE, Contract::locale, Contract::locale, TsvHdl::toLocale, Locale::getLanguage));
        fields.add(TsvProperty.of("currency", Contract::currency, Contract::currency, Currency::getInstance, Currency::getCurrencyCode));
        fields.add(TsvProperty.of(TsvHdlCore.createTsvBankConnection(), Contract::bankConnection, Contract::bankConnection));
        fields.add(TsvProperty.ofBigDecimal("amountWithoutVat", Contract::amountWithoutVat, Contract::amountWithoutVat));
        fields.add(TsvProperty.ofBigDecimal("budgetInHours", Contract::budgetInHours, Contract::budgetInHours));
        fields.add(TsvProperty.of("sellerOid", Contract::seller, Contract::seller, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        fields.add(TsvProperty.of("selleeOid", Contract::sellee, Contract::sellee, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        fields.add(createAddressMapping(VcardType.work));
        return TsvHdl.of(Contract.class, fields, Contract::new);
    }

    private TsvEntity<ContractExtension> createTsvContractExtension() {
        Function<CSVRecord, ContractExtension> imports = (CSVRecord csv) -> new ContractExtension(get(csv, HasId.ID), get(csv, NAME),
            DateRange.of(LocalDate.parse(get(csv, FROM_DATE)), LocalDate.parse(get(csv, TO_DATE))), get(csv, TEXT), get(csv, "contractId"),
            TsvHdl.parseBigDecimal(csv, "amountWithoutVat"), TsvHdl.parseBigDecimal(csv, "budgetInHours"));

        List<TsvProperty<ContractExtension, ?>> fields =
            List.of(TsvProperty.ofString(HasId.ID, ContractExtension::id), TsvProperty.ofString(NAME, ContractExtension::name),
                TsvProperty.of(TsvHdlCore.createTsvDateRange(), HasDateRange::range, null), TsvProperty.ofString(TEXT, ContractExtension::text),
                TsvProperty.ofString("contractId", ContractExtension::contractId),
                TsvProperty.ofBigDecimal("amountWithoutVat", ContractExtension::amountWithoutVat, null),
                TsvProperty.ofBigDecimal("budgetInHours", ContractExtension::amountWithoutVat, null));
        return TsvEntity.of(ContractExtension.class, fields, imports);
    }

    private TsvEntity<Opportunity> createTsvOpportunity() {
        List<TsvProperty<Opportunity, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.of("state", Opportunity::code, Opportunity::code, e -> TsvProperty.valueOf(OpportunityCode.class, e), Enum::name));
        fields.add(TsvProperty.ofBigDecimal("potential", Opportunity::potential, Opportunity::potential));
        fields.add(TsvProperty.ofBigDecimal("probability", Opportunity::probability, Opportunity::probability));
        fields.add(TsvProperty.of("legalEntity", Opportunity::entity, Opportunity::entity, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        fields.add(TsvProperty.of("contact", Opportunity::contact, Opportunity::contact, e -> findEmployeeByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        return TsvHdl.of(Opportunity.class, fields, Opportunity::new);
    }

    private Optional<Employee> findEmployeeByOid(String oid) {
        return (oid != null) ? Provider.findByOid(realm.employees(), Long.parseLong(oid)) : Optional.empty();
    }

    private Optional<NaturalEntity> findNaturalEntityByOid(String oid) {
        return (oid != null) ? Provider.findByOid(realm.naturalEntities(), Long.parseLong(oid)) : Optional.empty();
    }

    private Optional<LegalEntity> findLegalEntityByOid(String oid) {
        return (oid != null) ? Provider.findByOid(realm.legalEntities(), Long.parseLong(oid)) : Optional.empty();
    }

    public static <T extends CrmEntity> TsvProperty<T, String> phoneNrProperty(String tagName, VcardType type) {
        return TsvProperty.ofString(tagName, e -> e.phoneNr(type).map(PhoneNr::number).orElse(null), (e, p) -> e.phoneNr(type, p));
    }

    public static <T extends CrmEntity> TsvProperty<T, String> emailProperty(String tagName, VcardType type) {
        return TsvProperty.ofString(tagName, e -> e.email(type).map(EmailAddress::text).orElse(null), (e, p) -> e.email(type, p));
    }

    public static <T extends CrmEntity> TsvProperty<T, Address> createAddressMapping(@NotNull VcardType type) {
        return TsvProperty.of(TsvHdlCore.createTsvAddress(), (T e) -> e.address(type).orElse(null), (e, p) -> e.address(type, p));
    }
}
