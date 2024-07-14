/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.core.domain;


import net.tangly.commons.lang.Strings;
import net.tangly.commons.logger.EventData;
import net.tangly.core.*;
import net.tangly.core.providers.Provider;
import net.tangly.core.tsv.TsvHdlCore;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.gleam.model.TsvRelation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class to handle TSV files.
 */
public final class TsvHdl {
    public static final CSVFormat FORMAT =
        CSVFormat.Builder.create().setDelimiter('\t').setQuote('"').setRecordSeparator('\n').setIgnoreSurroundingSpaces(true).setHeader()
            .setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setIgnoreEmptyLines(true).build();

    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String TEXT = "text";
    public static final String DOMAIN = "domain";

    private TsvHdl() {
    }

    public static boolean isEmpty(@NotNull CSVRecord record) {
        return Objects.isNull(record) || record.stream().anyMatch(o -> !Strings.isNullOrBlank(o));
    }

    public static CSVRecord nextNonEmptyRecord(Iterator<CSVRecord> records) {
        CSVRecord csv = records.hasNext() ? records.next() : null;
        while (Objects.nonNull(csv) && !TsvHdl.isEmpty(csv)) {
            csv = records.hasNext() ? records.next() : null;
        }
        return csv;
    }

    public static <T> TsvEntity<T> of(@NotNull Class<T> clazz, @NotNull List<TsvProperty<T, ?>> properties, @NotNull Function<Long, T> supplier) {
        return new TsvEntity<>(clazz, properties, o -> supplier.apply(Long.parseLong(o.get(HasOid.OID))), null);
    }

    public static <T extends MutableEntity> List<TsvProperty<T, ?>> createTsvEntityFields() {
        List<TsvProperty<T, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.ofLong(HasOid.OID, MutableEntity::oid, null));
        fields.add(TsvProperty.ofString(HasId.ID, MutableEntity::id, MutableEntity::id));
        fields.add(TsvProperty.ofString(NAME, MutableEntity::name, MutableEntity::name));
        fields.add(TsvProperty.of(TsvHdlCore.createTsvDateRange(), MutableEntity::range, MutableEntity::range));
        fields.add(TsvProperty.ofString(TEXT, MutableEntity::text, MutableEntity::text));
        return fields;
    }

    public static String parseString(@NotNull CSVRecord record, @NotNull String fieldName) {
        return Strings.blankToNull(record.get(fieldName));
    }

    public static LocalDate parseDate(@NotNull CSVRecord record, @NotNull String fieldName) {
        var value = record.get(fieldName);
        return Strings.isNullOrBlank(value) ? null : LocalDate.parse(value);
    }

    public static int parseInt(@NotNull CSVRecord record, @NotNull String fieldName) {
        var value = record.get(fieldName);
        return Strings.isNullOrBlank(value) ? 0 : Integer.parseInt(value);
    }

    public static BigDecimal parseBigDecimal(@NotNull CSVRecord record, @NotNull String fieldName) {
        var value = record.get(fieldName);
        return Strings.isNullOrBlank(value) ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public static <T extends Enum<T>> T parseEnum(@NotNull CSVRecord record, @NotNull String fieldName, @NotNull Class<T> enumType) {
        var value = record.get(fieldName);
        return Strings.isNullOrBlank(value) ? null : Enum.valueOf(enumType, value);
    }

    public static <T> void importEntities(@NotNull DomainAudit audit, @NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull Provider<T> provider) {
        BiFunction<TsvEntity<T>, CSVRecord, T> lambda = (tsv, record) -> {
            T entity = tsvEntity.imports(record);
            if (!(entity instanceof MutableEntityExtended instance) || (instance.validate())) {
                provider.update(entity);
            }
            return entity;
        };
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            imports(audit, in, path.toString(), tsvEntity, lambda);
        } catch (IOException e) {
            audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Entities not imported from TSV file", Map.of("filename", path), e);
        }
    }

    public static <T> List<TsvRelation<T>> importRelations(@NotNull DomainAudit audit, @NotNull Path path, @NotNull TsvEntity<T> tsvEntity) {
        List<TsvRelation<T>> relations = new ArrayList<>();
        BiFunction<TsvEntity<T>, CSVRecord, TsvRelation<T>> lambda = (tsv, record) -> {
            TsvRelation<T> relation = tsvEntity.importRelation(record);
            relations.add(relation);
            return relation;
        };
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            imports(audit, in, path.toString(), tsvEntity, lambda);
        } catch (IOException e) {
            audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Relations not imported from TSV file", Map.of("filename", path), e);
        }
        return relations;
    }

    public static <T> void exportEntities(@NotNull DomainAudit audit, @NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull Provider<T> provider) {
        BiConsumer<T, CSVPrinter> lambda = tsvEntity::exports;
        exports(audit, path, tsvEntity, provider.items(), lambda);
    }

    public static <T> void exportRelations(@NotNull DomainAudit audit, @NotNull Path path, @NotNull TsvEntity<T> tsvEntity,
                                           @NotNull List<TsvRelation<T>> relations) {
        BiConsumer<TsvRelation<T>, CSVPrinter> lambda = tsvEntity::exportRelation;
        exports(audit, path, tsvEntity, relations, lambda);
    }

    public static <T extends HasMutableComments & HasOid> void addComments(Provider<T> provider, List<TsvRelation<Comment>> comments) {
        provider.items().forEach(e -> TsvHdl.addComments(provider, e, comments));
    }

    public static <T extends HasMutableComments & HasOid> void addComments(Provider<T> provider, T entity, List<TsvRelation<Comment>> comments) {
        var items = comments.stream().filter(o -> o.ownerId() == entity.oid()).map(TsvRelation::ownedEntity).toList();
        if (!items.isEmpty()) {
            entity.addComments(items);
            provider.update(entity);
        }
    }

    public static <T extends HasMutableTags> TsvProperty<T, String> tagProperty(String tagName) {
        return TsvProperty.ofString(tagName, e -> e.value(tagName).orElse(null), (e, p) -> {
            if (p != null) {
                e.update(tagName, p);
            }
        });
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

    private static <T, U> void imports(@NotNull DomainAudit audit, @NotNull Reader in, String source, @NotNull TsvEntity<T> tsvEntity,
                                       BiFunction<TsvEntity<T>, CSVRecord, U> function) {
        CSVRecord loggedRecord = null;
        try (in) {
            int counter = 0;
            for (CSVRecord csv : FORMAT.parse(in)) {
                loggedRecord = csv;
                U imported = function.apply(tsvEntity, csv);
                if (!(imported instanceof MutableEntityExtended entity) || (entity.validate())) {
                    ++counter;
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, "%s imported".formatted(tsvEntity.clazz().getSimpleName()),
                        Map.of("filename", source, "object", imported));
                } else {
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.WARNING, "%s invalid entity".formatted(tsvEntity.clazz().getSimpleName()),
                        Map.of("filename", source, "object", imported));
                }
            }
            audit.log(EventData.IMPORT_EVENT, EventData.Status.SUCCESS, "%s imported objects".formatted(tsvEntity.clazz().getSimpleName()),
                Map.of("filename", source, "count", counter));
        } catch (IOException e) {
            audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Entities not imported from TSV file",
                Map.of("filename", source, "csv-record", loggedRecord), e);
        }
    }

    public static <T, U> void exports(@NotNull DomainAudit audit, @NotNull Path path, @NotNull TsvEntity<T> tsvEntity, @NotNull List<U> items,
                                      @NotNull BiConsumer<U, CSVPrinter> lambda) {
        U loggedEntity = null;
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), FORMAT)) {
            int counter = 0;
            tsvEntity.headers().forEach(e -> TsvProperty.print(out, e));
            out.println();
            for (U entity : items) {
                loggedEntity = entity;
                lambda.accept(entity, out);
                out.println();
                ++counter;
                audit.log(EventData.EXPORT_EVENT, EventData.Status.INFO, "%s exported to TSV file".formatted(tsvEntity.clazz().getSimpleName()),
                    Map.of("filename", path, "entity", entity));
            }
            audit.log(EventData.EXPORT_EVENT, EventData.Status.SUCCESS, "exported to TSV file", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            audit.log(EventData.EXPORT_EVENT, EventData.Status.FAILURE, "Entities exported to TSV file",
                Map.of("filename", path, "entity", Objects.nonNull(loggedEntity) ? loggedEntity : "null"), e);
        }
    }
}
