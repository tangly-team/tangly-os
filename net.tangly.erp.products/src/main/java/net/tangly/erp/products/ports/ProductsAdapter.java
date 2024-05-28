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

import com.amihaiemil.eoyaml.*;
import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.commons.utilities.ValidatorUtilities;
import net.tangly.core.Strings;
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
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.FormatProcessor.FMT;
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
    public void importEntities() {
        var handler = new ProductsTsvHdl(realm());
        Port.importEntities(dataFolder, PRODUCTS_TSV, handler::importProducts);
        Port.importEntities(dataFolder, WORK_CONTRACTS_TSV, handler::importWorkContracts);
        Port.importEntities(dataFolder, ASSIGNMENTS_TSV, handler::importAssignments);
        try (Stream<Path> stream = Files.walk(dataFolder)) {
            AtomicInteger nrOfImportedEffortFiles = new AtomicInteger();
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(YAML_EXT)).forEach(o -> {
                try (Reader reader = Files.newBufferedReader(dataFolder.resolve(o))) {
                    importEfforts(reader, o.toString(), true);
                    nrOfImportedEffortFiles.getAndIncrement();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (Exception e) {
                    EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.ERROR, "Error importing efforts.", Map.of("filename", o.toString()));
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
        handler.exportWorkContracts(dataFolder.resolve(WORK_CONTRACTS_TSV));
        handler.exportAssignments(dataFolder.resolve(ASSIGNMENTS_TSV));
        exportEfforts(dataFolder);
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
            if (ValidatorUtilities.isYamlValid(new StringReader(source), "assignment-schema.json")) {
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
                    String minutes = effort.asMapping().string("minutes");
                    Effort newEffort = new Effort(assignment, contractId, date, duration, text);
                    if (Objects.nonNull(minutes)) {
                        newEffort.minutes(minutes);
                    }
                    if (!assignment.range().isActive(date)) {
                        EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.ERROR, "effort date is out of assignment range.",
                            Map.of("filename", source, "assignement", assignment, "effort", newEffort));
                        return;
                    }
                    if ((assignment.closedPeriod() != null) && (!newEffort.date().isAfter(assignment.closedPeriod()))) {
                        EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.ERROR, "effort date is before of assignment closed period.",
                            Map.of("filename", source, "assignement", assignment, "effort", newEffort));
                        return;
                    }
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
            }

        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * Export all efforts in a hierarchy of folders. The files are grouped in folders by years.
     * The efforts are grouped by assignment, contract, collaborator, and year-month and written in a yaml file.
     * The name of the file is the year and month of the efforts, the name of the collaborator, and the identifier of the contract.
     *
     * @param path the path of the root folder where the efforts are exported
     */
    public void exportEfforts(@NotNull Path path) {
        var efforts = realm().efforts().items().stream().collect(
            groupingBy(o -> o.assignment().id(), groupingBy(o -> o.contractId(), groupingBy(o -> o.assignment().collaboratorId(),
                groupingBy(o -> YearMonth.from(o.date()))))));
        efforts.values().stream().flatMap(o -> o.values().stream().flatMap(o1 -> o1.values().stream().flatMap(o2 -> o2.values().stream())))
            .forEach(o -> exportEfforts(o, path));
    }

    public void exportEfforts(@NotNull List<Effort> efforts, @NotNull Path folder) {
        try {
            efforts.sort((o1, o2) -> o1.date().compareTo(o2.date()));
            YamlMappingBuilder builder = Yaml.createYamlMappingBuilder()
                .add("assignmentOid", efforts.getFirst().assignment().oid())
                .add("contractId", efforts.getFirst().contractId())
                .add(("collaborator"), efforts.getFirst().assignment().collaboratorId());
            var effortsBuilder = Yaml.createYamlSequenceBuilder();
            for (Effort effort : efforts) {
                var effortBuilder = Yaml.createYamlMappingBuilder();
                effortBuilder = effortBuilder.add("date", effort.date()).add("duration", effort.duration()).add("text", effort.text());
                if (effort.minutes() != null) {
                    effortBuilder = effortBuilder.add("minutes", effort.minutes());
                }
                effortsBuilder = effortsBuilder.add(effortBuilder.build());
            }
            builder = builder.add("efforts", effortsBuilder.build());
            folder = folder.resolve(Integer.toString(efforts.getFirst().date().getYear()), FMT."%02d\{efforts.getFirst().date().getMonthValue()}");
            AsciiDoctorHelper.createFolders(folder);
            var file = folder.resolve(filename(efforts.getFirst()));
            var printer = Yaml.createYamlPrinter(Files.newBufferedWriter(file));
            printer.print(builder.build());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void exportEffortsDocument(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull String filename, @NotNull ChronoUnit unit) {
        if (!logic.collect(assignment, from, to).isEmpty()) {
            var effortsDocumentFolder = reportFolder.resolve(Objects.nonNull(to) ? Integer.toString(to.getYear()) : Integer.toString(LocalDate.now().getYear()));
            AsciiDoctorHelper.createFolders(effortsDocumentFolder);
            var assignmentAsciiDocPath = effortsDocumentFolder.resolve(STR."\{filename}\{AsciiDoctorHelper.ASCIIDOC_EXT}");
            var assignmentPdfPath = effortsDocumentFolder.resolve(STR."\{filename}\{AsciiDoctorHelper.PDF_EXT}");
            var helper = new EffortReportEngine(logic);
            helper.createReport(assignment, from, to, assignmentAsciiDocPath, unit);
            AsciiDoctorHelper.createPdf(assignmentAsciiDocPath, assignmentPdfPath, true);
        }
    }

    @Override
    public void exportEffortsDocumentsSplittedPerMonth(@NotNull Assignment assignment, @NotNull YearMonth from, @NotNull YearMonth to, @NotNull ChronoUnit unit) {
        YearMonth current = from;
        var helper = new EffortReportEngine(logic);
        while (!current.isAfter(to)) {
            if (!logic.collect(assignment, current.atDay(1), current.atEndOfMonth()).isEmpty()) {
                var effortsDocumentFolder = reportFolder.resolve(Integer.toString(current.getYear()));
                AsciiDoctorHelper.createFolders(effortsDocumentFolder);
                var filename = filename(assignment, current);
                var assignmentAsciiDocPath = effortsDocumentFolder.resolve(STR."\{filename}\{AsciiDoctorHelper.ASCIIDOC_EXT}");
                var assignmentPdfPath = effortsDocumentFolder.resolve(STR."\{filename}\{AsciiDoctorHelper.PDF_EXT}");
                helper.createMonthlyReport(assignment, current, unit, assignmentAsciiDocPath);
                AsciiDoctorHelper.createPdf(assignmentAsciiDocPath, assignmentPdfPath, true);
                current = current.plusMonths(1);
            }
        }
    }

    private String filename(@NotNull Assignment assignment, @NotNull YearMonth month) {
        String generatedText = FMT."\{month.getYear()}-%02d\{month.getMonthValue()}";
        String dateText = Strings.firstOnlyUppercase(month.getMonth().toString());
        return STR."\{generatedText}-\{assignment.id()}-\{assignment.name()}-\{dateText}";
    }

    private String filename(@NotNull Effort effort) {
        String generatedText = FMT."\{effort.date().getYear()}-%02d\{effort.date().getMonthValue()}";
        return STR."\{generatedText}-\{effort.assignment().name()}-\{effort.contractId()}\{YAML_EXT}";
    }
}
