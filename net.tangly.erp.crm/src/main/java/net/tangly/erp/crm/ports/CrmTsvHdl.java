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
import net.tangly.core.crm.GenderCode;
import net.tangly.core.crm.LegalEntity;
import net.tangly.core.crm.NaturalEntity;
import net.tangly.core.crm.VcardType;
import net.tangly.core.providers.Provider;
import net.tangly.core.tsv.TsvHdlCore;
import net.tangly.erp.crm.domain.*;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmRealm;
import net.tangly.erp.ports.TsvHdl;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.gleam.model.TsvRelation;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static net.tangly.core.crm.CrmTags.*;
import static net.tangly.core.tsv.TsvHdlCore.DATE;
import static net.tangly.core.tsv.TsvHdlCore.TEXT;
import static net.tangly.gleam.model.TsvEntity.get;

/**
 * Provide import and export functions for CRM entities persisted in tab separated files. These files are often defined for integration testing or integration of legacy systems not
 * providing programmatic API. The description of the TSV file structure and the mapping rules are of declarative nature. One2one relations are mapped through the oid of the
 * referenced entity if defined, otherwise an empty string. One2many relations are mapped through a tab separated list for the oid of the referenced entities if at least one is
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

    public void importComments(@NotNull Reader reader, String source) {
        var comments = TsvHdl.importRelations(CrmBoundedDomain.DOMAIN, reader, source, createTsvComment());
        TsvHdl.addComments(realm.naturalEntities(), comments);
        TsvHdl.addComments(realm.legalEntities(), comments);
        TsvHdl.addComments(realm.employees(), comments);
        TsvHdl.addComments(realm.contracts(), comments);
        TsvHdl.addComments(realm.subjects(), comments);
        TsvHdl.addComments(realm.interactions(), comments);
    }

    public void exportComments(@NotNull Path path) {
        List<TsvRelation<Comment>> comments = new ArrayList<>();
        realm.naturalEntities().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.legalEntities().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.employees().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.contracts().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.subjects().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        realm.interactions().items().forEach(o -> comments.addAll(updateAndCollectComments(o)));
        TsvHdl.exportRelations(CrmBoundedDomain.DOMAIN, path, createTsvComment(), comments);
    }

    public void importLeads(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvLead(), realm.leads());
    }

    public void exportLeads(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvLead(), realm.leads());
    }

    public void importNaturalEntities(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvNaturalEntity(), realm.naturalEntities());
    }

    public void exportNaturalEntities(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvNaturalEntity(), realm.naturalEntities());
    }

    public void importLegalEntities(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvLegalEntity(), realm.legalEntities());
    }

    public void exportLegalEntities(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvLegalEntity(), realm.legalEntities());
    }

    public void importEmployees(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvEmployee(), realm.employees());
    }

    public void exportEmployees(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvEmployee(), realm.employees());
    }

    public void importContracts(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvContract(), realm.contracts());
    }

    public void exportContracts(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvContract(), realm.contracts());
    }

    public void importSubjects(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvSubject(), realm.subjects());
    }

    public void exportSubjects(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvSubject(), realm.subjects());
    }

    public void importInteractions(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(CrmBoundedDomain.DOMAIN, reader, source, createTsvInteraction(), realm.interactions());
    }

    public void exportInteractions(@NotNull Path path) {
        TsvHdl.exportEntities(CrmBoundedDomain.DOMAIN, path, createTsvInteraction(), realm.interactions());
    }

    public void importActivities(@NotNull Reader reader, String source) {
        var activities = TsvHdl.importRelations(CrmBoundedDomain.DOMAIN, reader, source, createTsvActivity());
        realm.interactions().items().forEach(e -> addActivities(realm.interactions(), e, activities));
    }

    public void exportActivities(@NotNull Path path) {
        List<TsvRelation<Activity>> activities = new ArrayList<>();
        realm.interactions().items().forEach(o -> activities.addAll(updateAndCollectActivities(o)));
        TsvHdl.exportRelations(CrmBoundedDomain.DOMAIN, path, createTsvActivity(), activities);
    }

    private static <T extends HasComments & HasOid> List<TsvRelation<Comment>> updateAndCollectComments(T entity) {
        List<TsvRelation<Comment>> comments = new ArrayList<>();
        entity.comments().forEach(comment -> comments.add(new TsvRelation<>(entity.oid(), comment)));
        return comments;
    }

    private static List<TsvRelation<Activity>> updateAndCollectActivities(@NotNull Interaction entity) {
        List<TsvRelation<Activity>> activities = new ArrayList<>();
        entity.activities().forEach(activity -> activities.add(new TsvRelation<>(entity.oid(), activity)));
        return activities;
    }

    private static void addActivities(Provider<Interaction> provider, Interaction entity, List<TsvRelation<Activity>> activities) {
        var items = activities.stream().filter(o -> o.ownerId() == entity.oid()).map(TsvRelation::ownedEntity).toList();
        if (!items.isEmpty()) {
            entity.addAll(items);
            provider.update(entity);
        }
    }

    private static TsvEntity<Comment> createTsvComment() {
        Function<CSVRecord, Comment> imports = (CSVRecord item) -> Comment.of(LocalDateTime.parse(get(item, CREATED)), get(item, AUTHOR), get(item, TEXT));
        List<TsvProperty<Comment, ?>> fields =
            List.of(TsvProperty.ofEmpty(TsvEntity.OWNER_FOID), TsvProperty.of(CREATED, Comment::created, null, o -> (o != null) ? LocalDateTime.parse(o) : null),
                TsvProperty.ofString(AUTHOR, Comment::author, null), TsvProperty.ofString(TEXT, Comment::text, null),
                TsvProperty.ofString(TAGS, HasTags::rawTags, HasTags::rawTags));
        return TsvEntity.of(Comment.class, fields, imports);
    }

    private static TsvEntity<NaturalEntity> createTsvNaturalEntity() {
        List<TsvProperty<NaturalEntity, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.ofString(FIRSTNAME, NaturalEntity::firstname, NaturalEntity::firstname));
        fields.add(TsvProperty.ofEnum(GenderCode.class, GENDER, NaturalEntity::gender, NaturalEntity::gender));
        fields.add(TsvHdl.emailProperty(CRM_EMAIL_HOME, VcardType.home));
        fields.add(TsvHdl.phoneNrProperty(CRM_PHONE_MOBILE, VcardType.mobile));
        fields.add(TsvHdl.phoneNrProperty(CRM_PHONE_HOME, VcardType.home));
        fields.add(TsvHdl.tagProperty(CRM_IM_LINKEDIN));
        fields.add(TsvHdl.tagProperty(CRM_SITE_HOME));
        fields.add(TsvHdl.createAddressMapping(VcardType.home));
        return TsvHdl.of(NaturalEntity.class, fields, NaturalEntity::new);
    }

    private static TsvEntity<LegalEntity> createTsvLegalEntity() {
        List<TsvProperty<LegalEntity, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.ofString(CRM_VAT_NUMBER, LegalEntity::vatNr, LegalEntity::vatNr));
        fields.add(TsvHdl.tagProperty(CRM_IM_LINKEDIN));
        fields.add(TsvHdl.emailProperty(CRM_EMAIL_WORK, VcardType.work));
        fields.add(TsvHdl.tagProperty(CRM_SITE_WORK));
        fields.add(TsvHdl.phoneNrProperty(CRM_PHONE_WORK, VcardType.work));
        fields.add(TsvHdl.createAddressMapping(VcardType.work));
        fields.add(TsvProperty.ofString(CRM_SCHOOL, e -> (e.containsTag(CRM_SCHOOL) ? "Y" : "N"), (e, p) -> {
            if ("Y".equals(p)) {
                e.update(CRM_SCHOOL, null);
            }
        }));
        return TsvHdl.of(LegalEntity.class, fields, LegalEntity::new);
    }

    private static TsvEntity<Activity> createTsvActivity() {
        List<TsvProperty<Activity, ?>> fields = List.of(TsvProperty.ofEmpty(TsvEntity.OWNER_FOID),
            TsvProperty.of("code", Activity::code, Activity::code, e -> Enum.valueOf(ActivityCode.class, e.toLowerCase()), Enum::name),
            TsvProperty.ofDate("date", Activity::date, Activity::date), TsvProperty.ofInt("durationInMinutes", Activity::duration, Activity::duration),
            TsvProperty.ofString(AUTHOR, Activity::author, Activity::author), TsvProperty.ofString(TEXT, Activity::text, Activity::text));
        return TsvEntity.of(Activity.class, fields, Activity::new);
    }

    private static TsvEntity<Lead> createTsvLead() {
        Function<CSVRecord, Lead> imports =
            (CSVRecord csv) -> new Lead(LocalDate.parse(get(csv, DATE)), Enum.valueOf(LeadCode.class, get(csv, TsvHdl.CODE)), get(csv, FIRSTNAME), get(csv, LASTNAME),
                Enum.valueOf(GenderCode.class, get(csv, TsvHdl.GENDER)), get(csv, "company"), PhoneNr.of(get(csv, "phoneNr")), EmailAddress.of(get(csv, EMAIL)),
                get(csv, "linkedIn"), TsvProperty.valueOf(ActivityCode.class, get(csv, "activity")), get(csv, TEXT));

        List<TsvProperty<Lead, ?>> fields = List.of(TsvProperty.ofDate(DATE, Lead::date, null), TsvProperty.ofEnum(LeadCode.class, "code", Lead::code, null),
            TsvProperty.ofString(FIRSTNAME, Lead::firstname, null), TsvProperty.ofString(LASTNAME, Lead::firstname, null),
            TsvProperty.ofEnum(GenderCode.class, GENDER, Lead::gender, null), TsvProperty.ofString("company", Lead::company, null),
            TsvProperty.ofString("phoneNr", o -> Objects.nonNull(o.phoneNr()) ? o.phoneNr().number() : null, null),
            TsvProperty.ofString(EMAIL, o -> Objects.nonNull(o.email()) ? o.email().text() : null, null), TsvProperty.ofString("linkedIn", Lead::linkedIn, null),
            TsvProperty.ofEnum(ActivityCode.class, "activity", Lead::activity, null), TsvProperty.ofString(TEXT, Lead::text, null));
        return TsvEntity.of(Lead.class, fields, imports);

    }

    private TsvEntity<Employee> createTsvEmployee() {
        List<TsvProperty<Employee, ?>> fields =
            List.of(TsvProperty.of(TsvHdl.OID, Employee::oid, (entity, value) -> ReflectionUtilities.set(entity, TsvHdl.OID, value), Long::parseLong),
                TsvProperty.of(TsvHdlCore.createTsvDateRange(), Entity::range, Entity::range), TsvProperty.ofString(TEXT, Employee::text, Employee::text),
                TsvProperty.of("personOid", Employee::person, Employee::person, e -> findNaturalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()),
                TsvProperty.of("organizationOid", Employee::organization, Employee::organization, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()),
                TsvHdl.tagProperty(CRM_EMPLOYEE_TITLE), TsvHdl.tagProperty(CRM_EMAIL_WORK),
                TsvProperty.ofString(CRM_PHONE_WORK, e -> e.phoneNr(VcardType.work).map(PhoneNr::number).orElse(""), (e, p) -> e.phoneNr(VcardType.work, p)),
                TsvHdl.phoneNrProperty(CRM_PHONE_MOBILE, VcardType.mobile));
        return TsvHdl.of(Employee.class, fields, Employee::new);
    }

    private TsvEntity<Contract> createTsvContract() {
        List<TsvProperty<Contract, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.of("locale", Contract::locale, Contract::locale, TsvHdl::toLocale, Locale::getLanguage));
        fields.add(TsvProperty.of("currency", Contract::currency, Contract::currency, Currency::getInstance, Currency::getCurrencyCode));
        fields.add(TsvProperty.of(TsvHdlCore.createTsvBankConnection(), Contract::bankConnection, Contract::bankConnection));
        fields.add(TsvProperty.ofBigDecimal("amountWithoutVat", Contract::amountWithoutVat, Contract::amountWithoutVat));
        fields.add(TsvProperty.of("sellerOid", Contract::seller, Contract::seller, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        fields.add(TsvProperty.of("selleeOid", Contract::sellee, Contract::sellee, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        fields.add(TsvHdl.createAddressMapping(VcardType.work));
        return TsvHdl.of(Contract.class, fields, Contract::new);
    }

    private TsvEntity<Subject> createTsvSubject() {
        List<TsvProperty<Subject, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.of("userOid", Subject::user, Subject::user, e -> findNaturalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        fields.add(TsvProperty.ofString("gravatarEmail", Subject::gravatarEmail, Subject::gravatarEmail));
        fields.add(TsvProperty.ofString("passwordSalt", Subject::passwordSalt, Subject::passwordSalt));
        fields.add(TsvProperty.ofString("passwordHash", Subject::passwordHash, Subject::passwordHash));
        fields.add(TsvProperty.ofString("gmailUsername", Subject::gmailUsername, Subject::gmailUsername));
        fields.add(TsvProperty.ofString("gmailPassword", Subject::gmailPassword, Subject::gmailPassword));
        return TsvHdl.of(Subject.class, fields, Subject::new);
    }

    private TsvEntity<Interaction> createTsvInteraction() {
        List<TsvProperty<Interaction, ?>> fields = TsvHdl.createTsvEntityFields();
        fields.add(TsvProperty.of("state", Interaction::code, Interaction::code, e -> Enum.valueOf(InteractionCode.class, e.toLowerCase()), Enum::name));
        fields.add(TsvProperty.ofBigDecimal("potential", Interaction::potential, Interaction::potential));
        fields.add(TsvProperty.ofBigDecimal("probability", Interaction::probability, Interaction::probability));
        fields.add(TsvProperty.of("legalEntity", Interaction::entity, Interaction::entity, e -> findLegalEntityByOid(e).orElse(null), TsvHdl.convertFoidTo()));
        return TsvHdl.of(Interaction.class, fields, Interaction::new);
    }

    private Optional<NaturalEntity> findNaturalEntityByOid(String identifier) {
        return (identifier != null) ? Provider.findByOid(realm.naturalEntities(), Long.parseLong(identifier)) : Optional.empty();
    }

    private Optional<LegalEntity> findLegalEntityByOid(String identifier) {
        return (identifier != null) ? Provider.findByOid(realm.legalEntities(), Long.parseLong(identifier)) : Optional.empty();
    }
}
