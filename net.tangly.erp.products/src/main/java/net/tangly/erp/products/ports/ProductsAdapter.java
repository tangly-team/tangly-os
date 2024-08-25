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
import com.amihaiemil.eoyaml.exceptions.YamlReadingException;
import net.tangly.commons.lang.Strings;
import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.commons.utilities.ValidatorUtilities;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.Port;
import net.tangly.core.providers.Provider;
import net.tangly.erp.products.artifacts.EffortReportEngine;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.services.ProductsPort;
import net.tangly.erp.products.services.ProductsRealm;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class ProductsAdapter implements ProductsPort {
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String WORK_CONTRACTS_TSV = "work-contracts.tsv";
    public static final String ASSIGNMENTS_TSV = "assignments.tsv";
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
    public void importEntities(@NotNull DomainAudit audit) {
        var handler = new ProductsTsvHdl(realm());
        handler.importProducts(audit, dataFolder.resolve(PRODUCTS_TSV));
        handler.importWorkContracts(audit, dataFolder.resolve(WORK_CONTRACTS_TSV));
        handler.importAssignments(audit, dataFolder.resolve(ASSIGNMENTS_TSV));
        try (Stream<Path> stream = Files.walk(dataFolder)) {
            AtomicInteger nrOfImportedEffortFiles = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(YAML_EXT)).forEach(o -> {
                try (Reader reader = Files.newBufferedReader(dataFolder.resolve(o))) {
                    importEfforts(audit, reader, o.toString(), true);
                    nrOfImportedEffortFiles.getAndIncrement();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (Exception e) {
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.ERROR, "Error importing efforts.", Map.of("filename", o.toString(), "exception", e));
                }
            });
            audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, "Efforts were imported out of",
                Map.of("nrOfImportedEffortFiles", Integer.toString(nrOfImportedEffortFiles.get())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities(@NotNull DomainAudit audit) {
        var handler = new ProductsTsvHdl(realm());
        handler.exportProducts(audit, dataFolder.resolve(PRODUCTS_TSV));
        handler.exportWorkContracts(audit, dataFolder.resolve(WORK_CONTRACTS_TSV));
        handler.exportAssignments(audit, dataFolder.resolve(ASSIGNMENTS_TSV));
        exportEfforts(audit, dataFolder);
    }

    @Override
    public void clearEntities(@NotNull DomainAudit audit) {
        realm().efforts().deleteAll();
        Port.entitiesCleared(audit, "efforts");
        realm().assignments().deleteAll();
        Port.entitiesCleared(audit, "assignments");
        realm().products().deleteAll();
        Port.entitiesCleared(audit, "products");
    }

    @Override
    public void importEfforts(@NotNull DomainAudit audit, @NotNull Reader stream, @NotNull String source, boolean replace) throws IORuntimeException {
        try {
            if (ValidatorUtilities.isYamlValid(new StringReader(source), "assignment-efforts-schema.json")) {
                YamlMapping data = Yaml.createYamlInput(stream).readYamlMapping();
                String contractId = data.string("contractId");
                String collaborator = data.string("collaborator");
                long assignmentOid = data.longNumber("assignmentOid");
                Assignment assignment = Provider.findByOid(realm().assignments(), assignmentOid).orElse(null);

                if (Objects.isNull(assignment)) {
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.ERROR, "assignment could not be found.",
                        Map.of("filename", source, "assignmentOid", Long.toString(assignmentOid)));
                    return;
                }
                YamlSequence efforts = data.yamlSequence("efforts");
                efforts.children().forEach((YamlNode effort) -> {
                    LocalDate date = effort.asMapping().date("date");
                    int duration = effort.asMapping().integer("duration");
                    Collection<String> text = effort.asMapping().literalBlockScalar("text");
                    Collection<String> minutes = effort.asMapping().literalBlockScalar("minutes");
                    Effort newEffort = new Effort(assignment, contractId, date, duration, multilines(text));
                    if (Objects.nonNull(minutes) && !minutes.isEmpty()) {
                        newEffort.minutes(multilines(minutes));
                    }
                    if (!assignment.range().isActive(date)) {
                        audit.log(EventData.IMPORT_EVENT, EventData.Status.ERROR, "effort date is out of assignment range.",
                            Map.of("filename", source, "assignment", assignment, "effort", newEffort));
                        return;
                    }
                    if ((assignment.closedPeriod() != null) && (!newEffort.date().isAfter(assignment.closedPeriod()))) {
                        audit.log(EventData.IMPORT_EVENT, EventData.Status.ERROR, "effort date is before of assignment closed period.",
                            Map.of("filename", source, "assignment", assignment, "effort", newEffort));
                        return;
                    }
                    Optional<Effort> foundEffort = logic.findEffortFor(assignmentOid, collaborator, date);
                    if (foundEffort.isPresent()) {
                        if (replace) {
                            logic.realm().efforts().delete(foundEffort.get());
                            logic.realm().efforts().update(newEffort);
                            audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, " effort replaced already exists.",
                                Map.of("filename", source, "entity", newEffort));

                        } else {
                            audit.log(EventData.IMPORT_EVENT, EventData.Status.WARNING, " effort could not be imported because it " + "already exists.",
                                Map.of("filename", source, "entity", newEffort));
                        }
                    } else {
                        logic.realm().efforts().update(newEffort);
                        audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, " effort added.", Map.of("filename", source, "entity", newEffort));
                    }
                });
            }

        } catch (IOException e) {
            throw new IORuntimeException(e);
        } catch (YamlReadingException e) {
            e.printStackTrace();
        }
    }

    private String multilines(Collection<String> texts) {
        StringBuilder builder = new StringBuilder();
        texts.forEach(o -> builder.append(o.trim().equals("_") ? "" : o.trim()).append(System.lineSeparator()));
        return builder.toString();
    }

    /**
     * Exports all efforts in a hierarchy of folders. The files are grouped in folders by years.
     * The efforts are grouped by assignment, contract, collaborator, and year-month and written in a yaml file.
     * The name of the file is the year and month of the efforts, the name of the collaborator, and the identifier of the contract.
     *
     * @param path the uri of the root folder where the efforts are exported
     */
    public void exportEfforts(@NotNull DomainAudit audit, @NotNull Path path) {
        var efforts = realm().efforts().items().stream().collect(groupingBy(o -> o.assignment().id(),
            groupingBy(Effort::contractId, groupingBy(o -> o.assignment().collaboratorId(), groupingBy(o -> YearMonth.from(o.date()))))));
        efforts.values().stream().flatMap(o -> o.values().stream().flatMap(o1 -> o1.values().stream().flatMap(o2 -> o2.values().stream())))
            .forEach(o -> exportEfforts(o, path));
    }

    public void exportEfforts(@NotNull List<Effort> efforts, @NotNull Path folder) {
        var assignment = efforts.getFirst().assignment();
        efforts.sort(Comparator.comparing(Effort::date));
        var file = Port.resolvePath(folder, efforts.getFirst().date().getYear(), efforts.getFirst().date().getMonth(), filename(efforts.getFirst()));
        try (var writer = new PrintWriter(Files.newOutputStream(file), true, StandardCharsets.UTF_8)) {
            writer.print(yamlScalar("assignmentOid", Long.toString(assignment.oid()), 0));
            writer.print(yamlScalar("contractId", efforts.getFirst().contractId(), 0));
            writer.print(yamlScalar("collaborator", assignment.name(), 0));
            writer.println("efforts:");
            for (Effort effort : efforts) {
                writer.print("-".indent(2));
                writer.print(yamlScalar("date", effort.date().toString(), 4));
                writer.print(yamlScalar("duration", Integer.toString(effort.duration()), 4));
                writer.print(("text: |".indent(4)));
                yamlLiteralBlockScalar(effort.text(), 8).forEach(writer::write);
                if (effort.minutes() != null) {
                    writer.print(("minutes: |".indent(4)));
                    yamlLiteralBlockScalar(effort.minutes(), 8).forEach(writer::write);
                }
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    public String yamlScalar(@NotNull String key, @NotNull String value, int indent) {
        return "%s: %s".formatted(key, value).indent(indent);
    }

    public List<String> yamlLiteralBlockScalar(@NotNull String text, int indent) {
        return text.lines().map(o -> o.trim().isEmpty() ? "_".indent(indent) : o.indent(indent)).toList();
    }

    @Override
    public void exportEffortsDocument(@NotNull DomainAudit audit, @NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull String filename,
                                      @NotNull ChronoUnit unit) {
        if (!logic.collect(assignment, from, to).isEmpty()) {
            var assignmentAsciiDocPath = Port.resolvePath(reportFolder, to.getYear(), to.getMonth(), "%s%s".formatted(filename, AsciiDoctorHelper.ASCIIDOC_EXT));
            var assignmentPdfPath = Port.resolvePath(reportFolder, to.getYear(), to.getMonth(), "%s%s".formatted(filename, AsciiDoctorHelper.PDF_EXT));
            var helper = new EffortReportEngine(logic, unit);
            helper.createReport(assignment, from, to, assignmentAsciiDocPath);
            AsciiDoctorHelper.createPdf(assignmentAsciiDocPath, assignmentPdfPath, true);
        }
    }

    @Override
    public void exportEffortsDocumentsSplitPerMonth(@NotNull DomainAudit audit, @NotNull Assignment assignment, @NotNull YearMonth from, @NotNull YearMonth to,
                                                    @NotNull ChronoUnit unit) {
        YearMonth current = from;
        var helper = new EffortReportEngine(logic, unit);
        while (!current.isAfter(to)) {
            if (!logic.collect(assignment, current.atDay(1), current.atEndOfMonth()).isEmpty()) {
                var filename = filename(assignment, current);
                var assignmentAsciiDocPath = Port.resolvePath(reportFolder, to.getYear(), to.getMonth(), "%s%s".formatted(filename, AsciiDoctorHelper.ASCIIDOC_EXT));
                var assignmentPdfPath = Port.resolvePath(reportFolder, to.getYear(), to.getMonth(), "%s%s".formatted(filename, AsciiDoctorHelper.PDF_EXT));
                helper.createMonthlyReport(assignment, current, assignmentAsciiDocPath);
                AsciiDoctorHelper.createPdf(assignmentAsciiDocPath, assignmentPdfPath, true);
                current = current.plusMonths(1);
            }
        }
    }

    private String filename(@NotNull Assignment assignment, @NotNull YearMonth month) {
        String generatedText = "%d-%%02d%d".formatted(month.getYear(), month.getMonthValue());
        String dateText = Strings.firstOnlyUppercase(month.getMonth().toString());
        return "%s-%s-%s-%s".formatted(generatedText, assignment.id(), assignment.name(), dateText);
    }

    private String filename(@NotNull Effort effort) {
        String generatedText = "%04d-%02d".formatted(effort.date().getYear(), effort.date().getMonthValue());
        return "%s-%s-%s%s".formatted(generatedText, effort.assignment().name(), effort.contractId(), YAML_EXT);
    }
}
