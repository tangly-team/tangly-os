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

import net.tangly.commons.lang.Strings;
import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static java.util.FormatProcessor.FMT;
import static java.util.stream.Collectors.groupingBy;
import static net.tangly.erp.products.domain.Assignment.convert;

/**
 * engine to create effort reports for collaborators.
 */
public class EffortReportEngine {
    private static final Logger logger = LogManager.getLogger();

    private final ProductsBusinessLogic logic;

    public EffortReportEngine(@NotNull ProductsBusinessLogic logic) {
        this.logic = logic;
    }

    public void createMonthlyReport(@NotNull Assignment assignment, YearMonth month, @NotNull ChronoUnit unit, @NotNull Path reportPath) {
        try (var writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
            logger.atInfo().log("Create assignment report {}", reportPath);
            createMonthlyReport(assignment, month, unit, writer);
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Error during reporting generation of {}", reportPath);
        }
    }

    public void createReport(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull Path reportPath, @NotNull ChronoUnit unit) {
        try (var writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
            logger.atInfo().log("Create assignment report {}", reportPath);
            createReport(assignment, from, to, unit, writer);
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Error during reporting generation of {}", reportPath);
        }
    }

    private void createMonthlyReport(@NotNull Assignment assignment, @NotNull YearMonth month, @NotNull ChronoUnit unit, @NotNull Writer writer) {
        final AsciiDocHelper helper = new AsciiDocHelper(writer);
        helper.header(FMT."Work Report \{Strings.firstOnlyUppercase(month.getMonth().toString())} \{month.getYear()}", 2);
        int workedDuration = logic.collect(assignment, null, month.atEndOfMonth()).stream().map(Effort::duration).reduce(0, Integer::sum);
        helper.paragraph(FMT."The amount of performed activities is \{convert(workedDuration, unit)} \{text(unit)} until end of \{Strings.firstOnlyUppercase(
            month.getMonth().toString())} \{month.getYear()}.");
        helper.paragraph("The daily reports are:");
        helper.tableHeader(null, "cols=\"1,6a,>1\", options=\"header\"");

        helper.writer().println(STR."^|Date ^|Description ^|Duration (\{text(unit)})");
        helper.writer().println();
        createTableBody(assignment, month.atDay(1), month.atEndOfMonth(), unit, helper);
        helper.tableEnd();
        createMinutes(assignment, month.atDay(1), month.atEndOfMonth(), helper);
    }

    private void createReport(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull ChronoUnit unit, @NotNull Writer writer) {
        final AsciiDocHelper helper = new AsciiDocHelper(writer);
        helper.header("Work Report", 2);
        helper.tableHeader(null, "cols=\"1,5a,>1\", options=\"header\"");
        helper.writer().println(STR."^|Date ^|Description ^|Duration (\{text(unit)})");
        helper.writer().println();
        createTableBody(assignment, from, to, unit, helper);
        helper.tableEnd();
    }

    private void createTableBody(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull ChronoUnit unit, @NotNull AsciiDocHelper helper) {
        Map<String, List<Effort>> groups = logic.collect(assignment, from, to).stream().collect(groupingBy(Effort::contractId));
        if (groups.keySet().size() > 1) {
            groups.keySet().forEach(o -> generateEffortsForContract(groups.get(o), helper));
            groups.keySet().forEach(o -> generateEffortsTotalForContract(groups.get(o), o, helper, unit));
        } else {
            logic.collect(assignment, from, to).forEach(o -> helper.tableRow(o.date().toString(), o.text(), convert(o.duration(), unit).toString()));
        }
        int totalDuration = logic.collect(assignment, from, to).stream().map(Effort::duration).reduce(0, Integer::sum);
        helper.tableRow("Total Time", STR."(time in \{text(unit)})", convert(totalDuration, unit).toString());
    }

    private void createMinutes(@NotNull Assignment assignment, @NotNull LocalDate from, @NotNull LocalDate to, @NotNull AsciiDocHelper helper) {
        List<Effort> efforts = logic.collect(assignment, from, to);
        boolean containsMinutes = efforts.stream().anyMatch(o -> !Strings.isNullOrBlank(o.minutes()));
        if (containsMinutes) {
            helper.header("Minutes", 2);
        }
        efforts.stream().filter(o -> !Strings.isNullOrBlank(o.minutes())).forEach(o -> {
            helper.header(STR."Minutes for \{o.date().toString()}", 3);
            helper.paragraph(o.minutes());
        });
    }

    private void generateEffortsForContract(@NotNull List<Effort> efforts, @NotNull AsciiDocHelper helper) {
        efforts.forEach(o -> helper.tableRow(o.date().toString(), o.text(), Integer.toString(o.duration())));
        helper.tableRow("", "", "");
    }

    private void generateEffortsTotalForContract(@NotNull List<Effort> efforts, @NotNull String contractId, @NotNull AsciiDocHelper helper,
                                                 @NotNull ChronoUnit unit) {
        int totalDuration = efforts.stream().map(Effort::duration).reduce(0, Integer::sum);
        helper.tableRow(STR."Total Time for Contract \{contractId}", STR."(Time in \{text(unit)}", convert(totalDuration, unit).toString());
        helper.tableRow("", "", "");
    }

    private static String text(@NotNull ChronoUnit unit) {
        return unit.name().toLowerCase();
    }
}
