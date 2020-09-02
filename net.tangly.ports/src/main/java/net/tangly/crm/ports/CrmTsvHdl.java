/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.crm.ports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.HasComments;
import net.tangly.bus.core.HasOid;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.crm.Activity;
import net.tangly.bus.crm.ActivityCode;
import net.tangly.bus.crm.BankConnection;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmEntity;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.GenderCode;
import net.tangly.bus.crm.Interaction;
import net.tangly.bus.crm.InteractionCode;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.Subject;
import net.tangly.bus.invoices.Product;
import net.tangly.bus.providers.InstanceProviderInMemory;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.commons.lang.Strings;
import net.tangly.commons.logger.EventData;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.tangly.bus.crm.CrmTags.CRM_EMAIL_HOME;
import static net.tangly.bus.crm.CrmTags.CRM_EMAIL_WORK;
import static net.tangly.bus.crm.CrmTags.CRM_EMPLOYEE_TITLE;
import static net.tangly.bus.crm.CrmTags.CRM_IM_LINKEDIN;
import static net.tangly.bus.crm.CrmTags.CRM_PHONE_MOBILE;
import static net.tangly.bus.crm.CrmTags.CRM_PHONE_WORK;
import static net.tangly.bus.crm.CrmTags.CRM_SCHOOL;
import static net.tangly.bus.crm.CrmTags.CRM_SITE_HOME;
import static net.tangly.bus.crm.CrmTags.CRM_SITE_WORK;
import static net.tangly.bus.crm.CrmTags.CRM_VAT_NUMBER;
import static net.tangly.bus.crm.CrmTags.HOME;
import static net.tangly.bus.crm.CrmTags.MOBILE;
import static net.tangly.bus.crm.CrmTags.WORK;

/**
 * Provides import and export functions for CRM entities persisted in comma separated tabs files. These files are often defined for integration testing or
 * integration of legacy systems not providing programmatic API. The description of the TSV file structure and mapping rules are of declarative nature. One 2
 * one relations are mapped through the oid of the referenced entity if defined, otherwise an empty string. One 2 many relations are mapped through an comma
 * separated list of the oid of the referenced entities if at least one is defined, otherwise an empty string.
 */
public class CrmTsvHdl {
    public static final String OID = "oid";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String TEXT = "text";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    private static final String STREET = "street";
    private static final String POSTCODE = "postcode";
    private static final String LOCALITY = "locality";
    private static final String REGION = "region";
    private static final String COUNTRY = "country";
    private static final String IBAN = "iban";
    private static final String BIC = "bic";
    private static final String INSTITUTE = "institute";
    private static final String OWNED_BY = "ownedBy";
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";
    private static final String TAGS = "tags";
    private static final CSVFormat FORMAT = CSVFormat.TDF.withFirstRecordAsHeader().withIgnoreHeaderCase(true).withRecordSeparator('\n');

    private static final Logger logger = LoggerFactory.getLogger(CrmTsvHdl.class);
    private final Crm crm;


    public CrmTsvHdl(@NotNull Crm crm) {
        this.crm = crm;
    }

    public void importComments(@NotNull Path path) {
        Provider<Comment> comments = new InstanceProviderInMemory<>();
        importEntities(path, createTsvComment(), comments);
        crm.naturalEntities().items().forEach(e -> addComments(e, comments));
        crm.legalEntities().items().forEach(e -> addComments(e, comments));
        crm.employees().items().forEach(e -> addComments(e, comments));
        crm.contracts().items().forEach(e -> addComments(e, comments));
        crm.subjects().items().forEach(e -> addComments(e, comments));
        crm.interactions().items().forEach(e -> addComments(e, comments));
        crm.activities().items().forEach(e -> addComments(e, comments));
    }

    public void exportComments(@NotNull Path path) {
        Provider<Comment> comments = new InstanceProviderInMemory<>();
        crm.naturalEntities().items().forEach(o -> comments.updateAll(o.comments()));
        crm.legalEntities().items().forEach(o -> comments.updateAll(o.comments()));
        crm.employees().items().forEach(o -> comments.updateAll(o.comments()));
        crm.contracts().items().forEach(o -> comments.updateAll(o.comments()));
        crm.subjects().items().forEach(o -> comments.updateAll(o.comments()));
        crm.interactions().items().forEach(o -> comments.updateAll(o.comments()));
        crm.activities().items().forEach(o -> comments.updateAll(o.comments()));
        exportEntities(path, createTsvComment(), comments);
    }

    public void importNaturalEntities(@NotNull Path path) {
        importEntities(path, createTsvNaturalEntity(), crm.naturalEntities());
    }

    public void exportNaturalEntities(@NotNull Path path) {
        exportEntities(path, createTsvNaturalEntity(), crm.naturalEntities());
    }

    public void importLegalEntities(@NotNull Path path) {
        importEntities(path, createTsvLegalEntity(), crm.legalEntities());
    }

    public void exportLegalEntities(@NotNull Path path) {
        exportEntities(path, createTsvLegalEntity(), crm.legalEntities());
    }

    public void importEmployees(@NotNull Path path) {
        importEntities(path, createTsvEmployee(), crm.employees());
    }

    public void exportEmployees(@NotNull Path path) {
        exportEntities(path, createTsvEmployee(), crm.employees());
    }

    public void importContracts(@NotNull Path path) {
        importEntities(path, createTsvContract(), crm.contracts());
    }

    public void exportContracts(@NotNull Path path) {
        exportEntities(path, createTsvContract(), crm.contracts());
    }

    public void importProducts(@NotNull Path path) {
        importEntities(path, createTsvProduct(), crm.products());
    }

    public void exportProducts(@NotNull Path path) {
        exportEntities(path, createTsvProduct(), crm.products());
    }

    public void importSubjects(@NotNull Path path) {
        importEntities(path, createTsvSubject(), crm.subjects());
    }

    public void exportSubjects(@NotNull Path path) {
        exportEntities(path, createTsvSubject(), crm.subjects());
    }

    public void importInteractions(@NotNull Path path) {
        importEntities(path, createTsvInteraction(), crm.interactions());
    }

    public void exportInteractions(@NotNull Path path) {
        exportEntities(path, createTsvInteraction(), crm.interactions());
    }

    public void importActivities(@NotNull Path path) {
        importEntities(path, createTsvActivity(), crm.activities());
    }

    public void exportActivities(@NotNull Path path) {
        exportEntities(path, createTsvActivity(), crm.activities());
    }

    <T> void importEntities(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, Provider<T> provider) {
        try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            int counter = 0;
            for (CSVRecord record : FORMAT.parse(in)) {
                T entity = tsvEntity.imports(record);
                provider.update(entity);
                ++counter;
                EventData.log("import", "net.tangly.ports", EventData.Status.SUCCESS, tsvEntity.clazz().getSimpleName() + " imported",
                        Map.of("filename", path, "entity", entity));
            }
            EventData.log("import", "net.tangly.ports", EventData.Status.SUCCESS, "imported from TSV file", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log("import", "net.tangly.ports", EventData.Status.FAILURE, "Entities imported from TSV file", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    <T> void exportEntities(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull Provider<T> provider) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), FORMAT)) {
            int counter = 0;
            tsvEntity.headers().forEach(e -> TsvProperty.print(out, e));
            out.println();
            for (T entity : provider.items()) {
                tsvEntity.exports(entity, out);
                out.println();
                ++counter;
                EventData.log("export", "net.tangly.ports", EventData.Status.SUCCESS, tsvEntity.clazz().getSimpleName() + " exported to TSV file",
                        Map.of("filename", path, "entity", entity));
            }
            EventData.log("export", "net.tangly.ports", EventData.Status.SUCCESS, "exported to TSV file", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log("export", "net.tangly.ports", EventData.Status.FAILURE, "Entities exported to TSV file", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    static <T extends Entity> List<TsvProperty<T, ?>> createTsvEntityFields() {
        List<TsvProperty<T, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.of(OID, Entity::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong));
        fields.add(TsvProperty.ofString(ID, Entity::id, Entity::id));
        fields.add(TsvProperty.ofString(NAME, Entity::name, Entity::name));
        fields.add(TsvProperty.of(FROM_DATE, Entity::fromDate, Entity::fromDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.of(TO_DATE, Entity::toDate, Entity::toDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.ofString(TEXT, Entity::text, Entity::text));
        return fields;
    }

    static TsvEntity<Comment> createTsvComment() {
        Function<CSVRecord, Comment> imports = (CSVRecord record) -> Comment
                .of(LocalDateTime.parse(get(record, CREATED)), Long.parseLong(get(record, OWNED_BY)), get(record, AUTHOR), get(record, TEXT));

        List<TsvProperty<Comment, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.of(OID, Comment::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong));
        fields.add(TsvProperty.ofLong(OWNED_BY, Comment::ownedBy, null));
        fields.add(TsvProperty.of(CREATED, Comment::created, null, o -> (o != null) ? LocalDateTime.parse(o) : null));
        fields.add(TsvProperty.ofString(AUTHOR, Comment::author, null));
        fields.add(TsvProperty.ofString(TEXT, Comment::text, null));
        fields.add(TsvProperty.ofString(TAGS, HasTags::rawTags, HasTags::rawTags));
        return TsvEntity.of(Comment.class, fields, imports);
    }

    static TsvEntity<NaturalEntity> createTsvNaturalEntity() {
        List<TsvProperty<NaturalEntity, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString(LASTNAME, NaturalEntity::lastname, NaturalEntity::lastname));
        fields.add(TsvProperty.ofString(FIRSTNAME, NaturalEntity::firstname, NaturalEntity::firstname));
        fields.add(TsvProperty.of("gender", NaturalEntity::gender, NaturalEntity::gender, e -> Enum.valueOf(GenderCode.class, e), Enum::name));
        fields.add(createAddressMapping(HOME));
        fields.add(TsvProperty.ofString(CRM_EMAIL_HOME, e -> e.email(HOME).orElse(""), (e, p) -> e.email(HOME, p)));
        fields.add(TsvProperty.ofString(CRM_PHONE_MOBILE, e -> e.phoneNr(MOBILE).map(PhoneNr::number).orElse(""), (e, p) -> e.phoneNr(MOBILE, p)));
        fields.add(tagProperty(CRM_IM_LINKEDIN));
        fields.add(tagProperty(CRM_SITE_HOME));
        return TsvEntity.of(NaturalEntity.class, fields, NaturalEntity::new);
    }

    static TsvEntity<LegalEntity> createTsvLegalEntity() {
        List<TsvProperty<LegalEntity, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString(CRM_VAT_NUMBER, LegalEntity::vatNr, LegalEntity::vatNr));
        fields.add(tagProperty(CRM_IM_LINKEDIN));
        fields.add(tagProperty(CRM_EMAIL_WORK));
        fields.add(tagProperty(CRM_SITE_WORK));
        fields.add(TsvProperty.ofString(CRM_PHONE_WORK, e -> e.phoneNr(WORK).map(PhoneNr::number).orElse(""), (e, p) -> e.phoneNr(WORK, p)));
        fields.add(createAddressMapping(WORK));
        fields.add(TsvProperty.ofString(CRM_SCHOOL, e -> (e.containsTag(CRM_SCHOOL) ? "Y" : "N"), (e, p) -> {
            if ("Y".equals(p)) {
                e.tag(CRM_SCHOOL, null);
            }
        }));
        return TsvEntity.of(LegalEntity.class, fields, LegalEntity::new);
    }

    TsvEntity<Employee> createTsvEmployee() {
        List<TsvProperty<Employee, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.of(OID, Employee::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong));
        fields.add(TsvProperty.ofString(ID, Employee::id, Entity::id));
        fields.add(TsvProperty.of(FROM_DATE, Employee::fromDate, Entity::fromDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.of(TO_DATE, Employee::toDate, Employee::toDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.ofString(TEXT, Employee::text, Employee::text));
        fields.add(TsvProperty.of("personOid", Employee::person, Employee::person, e -> this.findNaturalEntityByOid(e).orElse(null), convertFoidTo()));
        fields.add(TsvProperty
                .of("organizationOid", Employee::organization, Employee::organization, e -> this.findLegalEntityByOid(e).orElse(null), convertFoidTo()));
        fields.add(tagProperty(CRM_EMPLOYEE_TITLE));
        fields.add(tagProperty(CRM_EMAIL_WORK));
        fields.add(TsvProperty.ofString(CRM_PHONE_WORK, e -> e.phoneNr(WORK).map(PhoneNr::number).orElse(""), (e, p) -> e.phoneNr(WORK, p)));
        return TsvEntity.of(Employee.class, fields, Employee::new);
    }

    TsvEntity<Contract> createTsvContract() {
        List<TsvProperty<Contract, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.of(createTsvBankConnection(), Contract::bankConnection, Contract::bankConnection));
        fields.add(TsvProperty.ofBigDecimal("amountWithoutVat", Contract::amountWithoutVat, Contract::amountWithoutVat));
        fields.add(TsvProperty.of("sellerOid", Contract::seller, Contract::seller, e -> this.findLegalEntityByOid(e).orElse(null), convertFoidTo()));
        fields.add(TsvProperty.of("selleeOid", Contract::sellee, Contract::sellee, e -> this.findLegalEntityByOid(e).orElse(null), convertFoidTo()));
        return TsvEntity.of(Contract.class, fields, Contract::new);
    }

    static TsvEntity<Product> createTsvProduct() {
        Function<CSVRecord, Product> imports = (CSVRecord record) -> new Product(get(record, "id"), get(record, "name"), get(record, "text"),
                TsvProperty.CONVERT_BIGDECIMAL_FROM.apply(get(record, "unitPrice")), get(record, "unit"),
                TsvProperty.CONVERT_BIGDECIMAL_FROM.apply(get(record, "vatRate")));

        List<TsvProperty<Product, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.ofString("id", Product::id, null));
        fields.add(TsvProperty.ofString("name", Product::name, null));
        fields.add(TsvProperty.ofString("text", Product::text, null));
        fields.add(TsvProperty.ofBigDecimal("unitPrice", Product::unitPrice, null));
        fields.add(TsvProperty.ofString("unit", Product::unit, null));
        fields.add(TsvProperty.ofBigDecimal("vatRate", Product::vatRate, null));
        return TsvEntity.of(Product.class, fields, imports);
    }

    TsvEntity<Subject> createTsvSubject() {
        List<TsvProperty<Subject, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.of("userOid", Subject::user, Subject::user, e -> this.findNaturalEntityByOid(e).orElse(null), convertFoidTo()));
        fields.add(TsvProperty.ofString("gravatarEmail", Subject::gravatarEmail, Subject::gravatarEmail));
        fields.add(TsvProperty.ofString("passwordSalt", Subject::passwordSalt, Subject::passwordSalt));
        fields.add(TsvProperty.ofString("passwordHash", Subject::passwordHash, Subject::passwordHash));
        fields.add(TsvProperty.ofString("gmailUsername", Subject::gmailUsername, Subject::gmailUsername));
        fields.add(TsvProperty.ofString("gmailPassword", Subject::gmailPassword, Subject::gmailPassword));
        return TsvEntity.of(Subject.class, fields, Subject::new);
    }

    static TsvEntity<Interaction> createTsvInteraction() {
        List<TsvProperty<Interaction, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.of("state", Interaction::state, Interaction::state, e -> Enum.valueOf(InteractionCode.class, e), Enum::name));
        fields.add(TsvProperty.ofBigDecimal("potential", Interaction::potential, Interaction::potential));
        fields.add(TsvProperty.ofBigDecimal("probability", Interaction::probability, Interaction::probability));
        return TsvEntity.of(Interaction.class, fields, Interaction::new);
    }

    static TsvEntity<Activity> createTsvActivity() {
        List<TsvProperty<Activity, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.of("code", Activity::code, Activity::code, e -> Enum.valueOf(ActivityCode.class, e), Enum::name));
        fields.add(TsvProperty.ofInt("durationInMinutes", Activity::durationInMinutes, Activity::durationInMinutes));
        fields.add((TsvProperty.ofString("details", Activity::details, Activity::details)));
        return TsvEntity.of(Activity.class, fields, Activity::new);
    }

    static TsvEntity<BankConnection> createTsvBankConnection() {
        Function<CSVRecord, BankConnection> imports = (CSVRecord record) -> {
            BankConnection connection = new BankConnection(get(record, IBAN), get(record, BIC), get(record, INSTITUTE));
            if (!connection.isValid()) {
                logger.atWarn().log("Invalid bank connection {}", connection);
            }
            return connection;
        };

        List<TsvProperty<BankConnection, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.ofString("iban", BankConnection::iban, null));
        fields.add(TsvProperty.ofString("bic", BankConnection::bic, null));
        fields.add(TsvProperty.ofString("institute", BankConnection::institute, null));
        return TsvEntity.of(BankConnection.class, fields, imports);
    }

    static TsvEntity<Address> createTsvAddress() {
        Function<CSVRecord, Address> imports =
                (CSVRecord record) -> Address.builder().street(get(record, STREET)).postcode(get(record, POSTCODE)).locality(get(record, LOCALITY))
                        .region(get(record, REGION)).country(get(record, COUNTRY)).build();

        List<TsvProperty<Address, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.ofString("street", Address::street, null));
        fields.add(TsvProperty.ofString("extended", Address::extended, null));
        fields.add(TsvProperty.ofString("postcode", Address::postcode, null));
        fields.add(TsvProperty.ofString("locality", Address::locality, null));
        fields.add(TsvProperty.ofString("region", Address::region, null));
        fields.add(TsvProperty.ofString("country", Address::country, null));
        return TsvEntity.of(Address.class, fields, imports);
    }

    <T extends HasComments & HasOid> void addComments(T entity, Provider<Comment> comments) {
        entity.addAll(comments.items().stream().filter(o -> o.ownedBy() == entity.oid()).collect(Collectors.toList()));
    }

    static <T extends HasTags> TsvProperty<T, String> tagProperty(String tagName) {
        return TsvProperty.ofString(tagName, e -> e.tag(tagName).orElse(null), (e, p) -> {
            if (p != null) {
                e.tag(tagName, p);
            }
        });
    }

    static <T extends CrmEntity> TsvProperty<T, Address> createAddressMapping(@NotNull String tag) {
        return TsvProperty.of(createTsvAddress(), (T e) -> e.address(tag).orElse(null), (e, p) -> e.address(tag, p));
    }

    static <T extends HasOid> Function<T, Object> convertFoidTo() {
        return u -> (u != null) ? Long.toString(u.oid()) : "";
    }

    private static String get(@NotNull CSVRecord record, @NotNull String column) {
        return Strings.emptyToNull(record.get(column));
    }

    private Optional<NaturalEntity> findNaturalEntityByOid(String identifier) {
        return (identifier != null) ? crm.naturalEntities().find(Long.parseLong(identifier)) : Optional.empty();
    }

    private Optional<LegalEntity> findLegalEntityByOid(String identifier) {
        return (identifier != null) ? crm.legalEntities().find(Long.parseLong(identifier)) : Optional.empty();
    }
}
