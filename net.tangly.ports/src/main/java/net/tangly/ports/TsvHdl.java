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

package net.tangly.ports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.BankConnection;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.HasComments;
import net.tangly.bus.core.HasOid;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.core.QualifiedEntity;
import net.tangly.bus.crm.CrmEntity;
import net.tangly.bus.crm.CrmTags;
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

public final class TsvHdl {
    public static final String OID = "oid";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String TEXT = "text";
    private static final String STREET = "street";
    private static final String POSTCODE = "postcode";
    private static final String LOCALITY = "locality";
    private static final String REGION = "region";
    private static final String COUNTRY = "country";
    private static final String IBAN = "iban";
    private static final String BIC = "bic";
    private static final String INSTITUTE = "institute";
    public static final String MODULE = "net.tangly.ports";
    private static final CSVFormat FORMAT = CSVFormat.TDF.withFirstRecordAsHeader().withIgnoreHeaderCase(true).withRecordSeparator('\n');
    private static final Logger logger = LoggerFactory.getLogger(TsvHdl.class);


    private TsvHdl() {
    }

    public static <T extends Entity> List<TsvProperty<T, ?>> createTsvEntityFields() {
        List<TsvProperty<T, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.of(OID, Entity::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong));
        fields.add(TsvProperty.of(FROM_DATE, Entity::fromDate, Entity::fromDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.of(TO_DATE, Entity::toDate, Entity::toDate, TsvProperty.CONVERT_DATE_FROM));
        return fields;
    }


    public static <T extends QualifiedEntity> List<TsvProperty<T, ?>> createTsvQualifiedEntityFields() {
        List<TsvProperty<T, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString(ID, QualifiedEntity::id, QualifiedEntity::id));
        fields.add(TsvProperty.ofString(NAME, QualifiedEntity::name, QualifiedEntity::name));
        fields.add(TsvProperty.ofString(TEXT, QualifiedEntity::text, QualifiedEntity::text));
        return fields;
    }

    public static <T> void importEntities(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, Provider<T> provider) {
        try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            int counter = 0;
            for (CSVRecord record : FORMAT.parse(in)) {
                T object = tsvEntity.imports(record);
                if (object instanceof Entity entity) {
                    if (entity.isValid()) {
                        provider.update(object);
                        ++counter;
                        EventData.log(EventData.IMPORT, MODULE, EventData.Status.SUCCESS, tsvEntity.clazz().getSimpleName() + " imported",
                                Map.of("filename", path, "object", object));
                    } else {
                        EventData.log(EventData.IMPORT, MODULE, EventData.Status.WARNING, tsvEntity.clazz().getSimpleName() + " invalid entity",
                                Map.of("filename", path, "object", object));

                    }
                } else {
                    provider.update(object);
                    ++counter;
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.INFO, tsvEntity.clazz().getSimpleName() + " imported",
                            Map.of("filename", path, "object", object));
                }
            }
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.INFO, tsvEntity.clazz().getSimpleName() + " imported objects",
                    Map.of("filename", path, "count", counter));
        } catch (IOException e) {
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.FAILURE, "Entities imported from TSV file", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    public static <T> void exportEntities(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull Provider<T> provider) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), FORMAT)) {
            int counter = 0;
            tsvEntity.headers().forEach(e -> TsvProperty.print(out, e));
            out.println();
            for (T entity : provider.items()) {
                tsvEntity.exports(entity, out);
                out.println();
                ++counter;
                EventData.log(EventData.EXPORT, MODULE, EventData.Status.SUCCESS, tsvEntity.clazz().getSimpleName() + " exported to TSV file",
                        Map.of("filename", path, "entity", entity));
            }
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.INFO, "exported to TSV file", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.FAILURE, "Entities exported to TSV file", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    public static TsvEntity<BankConnection> createTsvBankConnection() {
        Function<CSVRecord, BankConnection> imports = (CSVRecord record) -> {
            BankConnection connection = new BankConnection(get(record, IBAN), get(record, BIC), get(record, INSTITUTE));
            if (!connection.isValid()) {
                logger.atWarn().log("Invalid bank connection {}", connection);
            }
            return connection;
        };
        List<TsvProperty<BankConnection, ?>> fields =
                List.of(TsvProperty.ofString("iban", BankConnection::iban, null), TsvProperty.ofString("bic", BankConnection::bic, null),
                        TsvProperty.ofString("institute", BankConnection::institute, null));
        return TsvEntity.of(BankConnection.class, fields, imports);
    }

    public static TsvEntity<Address> createTsvAddress() {
        Function<CSVRecord, Address> imports =
                (CSVRecord record) -> Address.builder().street(get(record, STREET)).postcode(get(record, POSTCODE)).locality(get(record, LOCALITY))
                        .region(get(record, REGION)).country(get(record, COUNTRY)).build();

        List<TsvProperty<Address, ?>> fields =
                List.of(TsvProperty.ofString("street", Address::street, null), TsvProperty.ofString("extended", Address::extended, null),
                        TsvProperty.ofString("postcode", Address::postcode, null), TsvProperty.ofString("locality", Address::locality, null),
                        TsvProperty.ofString("region", Address::region, null), TsvProperty.ofString("country", Address::country, null));
        return TsvEntity.of(Address.class, fields, imports);
    }


    public static String get(@NotNull CSVRecord record, @NotNull String column) {
        return Strings.emptyToNull(record.get(column));
    }

    public static <T extends HasComments & HasOid> void addComments(T entity, Provider<Comment> comments) {
        entity.addAll(comments.items().stream().filter(o -> o.ownerFoid() == entity.oid()).collect(Collectors.toList()));
    }

    public static <T extends HasTags> TsvProperty<T, String> tagProperty(String tagName) {
        return TsvProperty.ofString(tagName, e -> e.tag(tagName).orElse(null), (e, p) -> {
            if (p != null) {
                e.tag(tagName, p);
            }
        });
    }

    public static <T extends CrmEntity> TsvProperty<T, String> phoneNrProperty(String tagName, CrmTags.Type type) {
        return TsvProperty.ofString(tagName, e -> e.phoneNr(type).map(PhoneNr::number).orElse(null), (e, p) -> e.phoneNr(type, p));
    }

    public static <T extends CrmEntity> TsvProperty<T, String> emailProperty(String tagName, CrmTags.Type type) {
        return TsvProperty.ofString(tagName, e -> e.email(type).map(EmailAddress::text).orElse(null), (e, p) -> e.email(type, p));
    }

    public static <T extends CrmEntity> TsvProperty<T, Address> createAddressMapping(@NotNull CrmTags.Type type) {
        return TsvProperty.of(createTsvAddress(), (T e) -> e.address(type).orElse(null), (e, p) -> e.address(type, p));
    }

    public static <T extends HasOid> Function<T, Object> convertFoidTo() {
        return u -> (u != null) ? Long.toString(u.oid()) : "";
    }

    public static Locale toLocale(String language) {
        return switch (language.toLowerCase()) {
            case "en" -> Locale.ENGLISH;
            case "de" -> Locale.GERMAN;
            case "fr" -> Locale.FRENCH;
            default -> Locale.ENGLISH;
        };
    }
}
