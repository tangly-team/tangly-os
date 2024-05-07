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

package net.tangly.erp.products.ports;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.core.domain.Port;
import net.tangly.core.providers.Provider;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.services.ProductsPort;
import net.tangly.erp.products.services.ProductsRealm;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ProductsAdapter implements ProductsPort {
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String ASSIGNMENTS_TSV = "assignments.tsv";
    public static final String EFFORTS_TSV = "efforts.tsv";
    public static final String YAML_EXT = ".yaml";

    private final ProductsRealm realm;

    private final ProductsBusinessLogic logic;

    private final Path dataFolder;
    private final Path reportFolder;

    public ProductsAdapter(@NotNull ProductsRealm realm, @NotNull ProductsBusinessLogic logic, @NotNull Path dataFolder, Path reportFolder) {
        this.realm = realm;
        this.logic = logic;
        this.dataFolder = dataFolder;
        this.reportFolder = reportFolder;
    }

    @Override
    public ProductsRealm realm() {
        return realm;
    }

    @Override
    public ProductsBusinessLogic logic() {
        return logic;
    }

    @Override
    public void importEntities() {
        var handler = new ProductsTsvHdl(realm());
        Port.importEntities(dataFolder, PRODUCTS_TSV, handler::importProducts);
        Port.importEntities(dataFolder, ASSIGNMENTS_TSV, handler::importAssignments);
        Port.importEntities(dataFolder, EFFORTS_TSV, handler::importEfforts);
        try (Stream<Path> stream = Files.walk(dataFolder)) {
            AtomicInteger nrOfImportedEffortFiles = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(YAML_EXT)).forEach(o -> {
                try (Reader reader = Files.newBufferedReader(dataFolder.resolve(o))) {
                    importEfforts(reader, o.toString(), true);
                    nrOfImportedEffortFiles.getAndIncrement();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.INFO, "Efforts were imported out of", Map.of("nrOfImportedEffortFiles", Integer.toString(nrOfImportedEffortFiles.get())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities() {
        var handler = new ProductsTsvHdl(realm());
        handler.exportProducts(dataFolder.resolve(PRODUCTS_TSV));
        handler.exportAssignments(dataFolder.resolve(ASSIGNMENTS_TSV));
        handler.exportEfforts(dataFolder.resolve(EFFORTS_TSV));
    }

    @Override
    public void clearEntities() {
        realm().efforts().deleteAll();
        realm().assignments().deleteAll();
        realm().products().deleteAll();
    }

    @Override
    public void importEfforts(@NotNull Reader stream, @NotNull String source, boolean replace) throws IORuntimeException {
        try {
            YamlMapping data = Yaml.createYamlInput(stream).readYamlMapping();
            String contractId = data.string("contractId");
            String collaborator = data.string("collaborator");
            long assignmentOid = data.longNumber("assignmentOid");
            Assignment assignment = Provider.findByOid(realm().assignments(), assignmentOid).orElse(null);

            if (Objects.isNull(assignment)) {
                EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.ERROR, "assignment could not be found.",
                    Map.of("filename", source, "assignmentOid", Long.toString(assignmentOid)));
                return;
            }
            YamlSequence efforts = data.yamlSequence("efforts");
            efforts.children().forEach((YamlNode effort) -> {
                LocalDate date = effort.asMapping().date("date");
                int duration = effort.asMapping().integer("duration");
                String text = effort.asMapping().string("text");
                Effort newEffort = new Effort(assignment, contractId, date, duration, text);
                Optional<Effort> foundEffort = logic.findEffortFor(assignmentOid, collaborator, date);
                if (foundEffort.isPresent()) {
                    if (replace) {
                        logic.realm().efforts().delete(foundEffort.get());
                        logic.realm().efforts().update(newEffort);
                        EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.INFO, " effort replaced already exists.", Map.of("filename", source, "entity", newEffort));

                    } else {
                        EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.WARNING, " effort could not be imported because it already exists.", Map.of("filename", source, "entity", newEffort));
                    }
                } else {
                    logic.realm().efforts().update(newEffort);
                    EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.INFO, " effort added.", Map.of("filename", source, "entity", newEffort));
                }
            });
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void exportEffortsDocument(@NotNull Assignment assignment, LocalDate from, LocalDate to, String filename, @NotNull ChronoUnit unit) {
        String collaborator = assignment.name().replace(",", "_").replace(" ", "");
        var assignmentDocumentPath = reportFolder.resolve(STR."\{assignment.id()}-\{collaborator}-\{from.toString()}_\{to.toString()}\{AsciiDoctorHelper.ASCIIDOC_EXT}");
        var helper = new EffortReportEngine(logic);
        helper.createAsciiDocReport(assignment, from, to, assignmentDocumentPath);
    }
}
