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
import java.util.Map;

import net.tangly.bus.core.Entity;
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

public final class TsvHdl {
    public static final String OID = "oid";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String TEXT = "text";
    public static final String MODULE = "net.tangly.ports";
    private static final CSVFormat FORMAT = CSVFormat.TDF.withFirstRecordAsHeader().withIgnoreHeaderCase(true).withRecordSeparator('\n');

    private TsvHdl() {
    }

    public static <T extends Entity> List<TsvProperty<T, ?>> createTsvEntityFields() {
        List<TsvProperty<T, ?>> fields = new ArrayList<>();
        fields.add(TsvProperty.of(OID, Entity::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong));
        fields.add(TsvProperty.ofString(ID, Entity::id, Entity::id));
        fields.add(TsvProperty.ofString(NAME, Entity::name, Entity::name));
        fields.add(TsvProperty.of(FROM_DATE, Entity::fromDate, Entity::fromDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.of(TO_DATE, Entity::toDate, Entity::toDate, TsvProperty.CONVERT_DATE_FROM));
        fields.add(TsvProperty.ofString(TEXT, Entity::text, Entity::text));
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

    public static String get(@NotNull CSVRecord record, @NotNull String column) {
        return Strings.emptyToNull(record.get(column));
    }

}
