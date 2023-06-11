/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.ports;

import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.commons.logger.EventData;
import net.tangly.core.Address;
import net.tangly.core.Comment;
import net.tangly.core.EmailAddress;
import net.tangly.core.Entity;
import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.PhoneNr;
import net.tangly.core.crm.CrmEntity;
import net.tangly.core.crm.VcardType;
import net.tangly.core.providers.Provider;
import net.tangly.core.tsv.TsvHdlCore;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.gleam.model.TsvRelation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.tangly.core.tsv.TsvHdlCore.FROM_DATE;
import static net.tangly.core.tsv.TsvHdlCore.ID;
import static net.tangly.core.tsv.TsvHdlCore.NAME;
import static net.tangly.core.tsv.TsvHdlCore.TEXT;
import static net.tangly.core.tsv.TsvHdlCore.TO_DATE;

public final class TsvHdl {
    public static final CSVFormat FORMAT =
        CSVFormat.Builder.create().setDelimiter('\t').setQuote('"').setRecordSeparator('\n').setIgnoreSurroundingSpaces(true).setHeader().setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true).setIgnoreEmptyLines(true).build();

    public static final String OID = HasOid.OID;
    public static final String CODE = "code";
    public static final String GENDER = "gender";
    public static final String MODULE = "net.tangly.ports";

    private TsvHdl() {
    }

    public static <T extends Entity> List<TsvProperty<T, ?>> createTsvEntityFields() {
        List<TsvProperty<T, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.of(OID, Entity::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong));
        fields.add(TsvProperty.ofString(TEXT, Entity::text, Entity::text));
        fields.add(TsvProperty.ofDate(FROM_DATE, Entity::from, Entity::from));
        fields.add(TsvProperty.ofDate(TO_DATE, Entity::to, Entity::to));
        return fields;
    }

    public static <T extends HasId & Entity & HasName> List<TsvProperty<T, ?>> createTsvQualifiedEntityFields() {
        List<TsvProperty<T, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString(ID, Entity::id, Entity::id));
        fields.add(TsvProperty.ofString(NAME, Entity::name, Entity::name));
        return fields;
    }

    public static <T> void importEntities(@NotNull Reader in, String source, @NotNull TsvEntity<T> tsvEntity, @NotNull Provider<T> provider) {
        BiFunction<TsvEntity<T>, CSVRecord, T> lambda = (tsv, record) -> {
            T entity = tsvEntity.imports(record);
            if (!(entity instanceof Entity instance) || (instance.validate())) {
                provider.update(entity);
            }
            return entity;
        };
        imports(in, source, tsvEntity, lambda);
    }

    public static <T> List<TsvRelation<T>> importRelations(@NotNull Reader in, String source, @NotNull TsvEntity<T> tsvEntity) {
        List<TsvRelation<T>> relations = new ArrayList<>();
        BiFunction<TsvEntity<T>, CSVRecord, TsvRelation<T>> lambda = (tsv, record) -> {
            TsvRelation<T> relation = tsvEntity.importRelation(record);
            relations.add(relation);
            return relation;
        };
        imports(in, source, tsvEntity, lambda);
        return relations;
    }

    public static <T> void exportEntities(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull Provider<T> provider) {
        BiConsumer<T, CSVPrinter> lambda = (o, printer) -> tsvEntity.exports(o, printer);
        exports(path, tsvEntity, provider.items(), lambda);
    }

    public static <T> void exportRelations(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull List<TsvRelation<T>> relations) {
        BiConsumer<TsvRelation<T>, CSVPrinter> lambda = (o, printer) -> tsvEntity.exportRelation(o, printer);
        exports(path, tsvEntity, relations, lambda);
    }

    public static <T extends HasComments & HasOid> void addComments(Provider<T> provider, List<TsvRelation<Comment>> comments) {
        provider.items().forEach(e -> TsvHdl.addComments(provider, e, comments));
    }

    public static <T extends HasComments & HasOid> void addComments(Provider<T> provider, T entity, List<TsvRelation<Comment>> comments) {
        var items = comments.stream().filter(o -> o.ownerId() == entity.oid()).map(o -> o.ownedEntity()).toList();
        if (!items.isEmpty()) {
            entity.addCommnents(items);
            provider.update(entity);
        }
    }

    public static <T extends HasTags> TsvProperty<T, String> tagProperty(String tagName) {
        return TsvProperty.ofString(tagName, e -> e.value(tagName).orElse(null), (e, p) -> {
            if (p != null) {
                e.update(tagName, p);
            }
        });
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

    private static <T, U> void imports(@NotNull Reader in, String source, @NotNull TsvEntity<T> tsvEntity, BiFunction<TsvEntity<T>, CSVRecord, U> function) {
        try (in) {
            int counter = 0;
            for (CSVRecord csv : FORMAT.parse(in)) {
                Object imported = function.apply(tsvEntity, csv);
                if (!(imported instanceof Entity entity) || (entity.validate())) {
                    ++counter;
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.SUCCESS, tsvEntity.clazz().getSimpleName() + " imported",
                        Map.of("filename", source, "object", imported));
                } else {
                    EventData.log(EventData.IMPORT, MODULE, EventData.Status.WARNING, tsvEntity.clazz().getSimpleName() + " invalid entity",
                        Map.of("filename", source, "object", imported));
                }
            }
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.INFO, tsvEntity.clazz().getSimpleName() + " imported objects", Map.of("filename", source, "count", counter));
        } catch (Exception e) {
            EventData.log(EventData.IMPORT, MODULE, EventData.Status.FAILURE, "Entities not imported from TSV file", Map.of("filename", source), e);
            throw new RuntimeException(e);
        }
    }

    public static <T, U> void exports(@NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull List<U> items, @NotNull BiConsumer<U, CSVPrinter> lambda) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), FORMAT)) {
            int counter = 0;
            tsvEntity.headers().forEach(e -> TsvProperty.print(out, e));
            out.println();
            for (U entity : items) {
                lambda.accept(entity, out);
                out.println();
                ++counter;
                EventData.log(EventData.EXPORT, MODULE, EventData.Status.SUCCESS, tsvEntity.clazz().getSimpleName() + " exported to TSV file",
                    Map.of("filename", path, "entity", entity));
            }
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.INFO, "exported to TSV file", Map.of("filename", path, "counter", counter));
        } catch (Exception e) {
            EventData.log(EventData.EXPORT, MODULE, EventData.Status.FAILURE, "Entities exported to TSV file", Map.of("filename", path), e);
            throw new RuntimeException(e);
        }
    }
}
