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

package net.tangly.erp.products.artifacts;

import net.tangly.commons.lang.Strings;
import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static net.tangly.erp.products.domain.Assignment.convert;

/**
 * engine to create effort reports for collaborators.
 */
public class EffortReportEngine {
    private static final Logger logger = LogManager.getLogger();

    private final ProductsBusinessLogic logic;
    private final ChronoUnit unit;

    public EffortReportEngine(@NotNull ProductsBusinessLogic logic, @NotNull ChronoUnit unit) {
        this.logic = logic;
        this.unit = unit;
    }


    public void createMonthlyReport(@NotNull Assignment assignment, YearMonth month, @NotNull Path reportPath) {
        try (var writer = new PrintWriter(Files.newOutputStream(reportPath), true, StandardCharsets.UTF_8)) {
            logger.atInfo().log("Create assignment report {}", reportPath);
            createMonthlyReport(assignment, month, writer);
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Error during reporting generation of {}", reportPath);
        }
    }

    public void createReport(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull Path reportPath) {
        try (var writer = new PrintWriter(Files.newOutputStream(reportPath), true, StandardCharsets.UTF_8)) {
            logger.atInfo().log("Create assignment report {}", reportPath);
            createReport(assignment, from, to, writer);
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Error during reporting generation of {}", reportPath);
        }
    }

    private void createMonthlyReport(@NotNull Assignment assignment, @NotNull YearMonth month, @NotNull PrintWriter writer) {
        final AsciiDocHelper helper = new AsciiDocHelper(writer);
        helper.header("Work Report %s %d".formatted(Strings.firstOnlyUppercase(month.getMonth().toString()), month.getYear()), 2);
        String folder = "/var/tangly-erp/tenant-tangly/docs";
        writer.println(":organization: " + "tangly llc");
        writer.println(":copyright: " + "");
        writer.println(":pdf-themesdir: " + folder);
        writer.println(":pdf-theme: " + "tenant");
        writer.println();

        int workedDuration = logic.collect(assignment, null, month.atEndOfMonth()).stream().map(Effort::duration).reduce(0, Integer::sum);
        helper.paragraph("The amount of performed activities is %s %s until end of %s %d.".formatted(convert(workedDuration, unit), text(unit),
            Strings.firstOnlyUppercase(month.getMonth().toString()), month.getYear()));
        helper.paragraph("The daily reports are:");
        helper.tableHeader(null, "cols=\"1,6a,>1\", options=\"header\"");

        helper.writer().println("^|Date ^|Description ^|Duration (%s)".formatted(text(unit)));
        helper.writer().println();
        createTableBody(assignment, month.atDay(1), month.atEndOfMonth(), helper);
        helper.tableEnd();
        createMinutes(assignment, month.atDay(1), month.atEndOfMonth(), helper);
    }

    private void createReport(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull PrintWriter writer) {
        final AsciiDocHelper helper = new AsciiDocHelper(writer);
        String folder = "/var/tangly-erp/tenant-tangly/docs";
        writer.println(":organization: " + "tangly llc");
        writer.println(":copyright: " + "");
        writer.println(":pdf-themesdir: " + folder);
        writer.println(":pdf-theme: " + "tenant");
        writer.println();

        helper.header("Work Report", 2);

        helper.paragraph("The described activities were performed between %s and %s."
            .formatted(from.format(DateTimeFormatter.ISO_LOCAL_DATE), to.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        helper.paragraph("The daily reports are:");

        helper.tableHeader(null, "cols=\"1,5a,>1\", options=\"header\"");
        helper.writer().println("^|Date ^|Description ^|Duration (%s)".formatted(text(unit)));
        helper.writer().println();
        createTableBody(assignment, from, to, helper);
        helper.tableEnd();
    }

    private void createTableBody(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull AsciiDocHelper helper) {
        var activities = logic.collect(assignment, from, to);
        Map<String, List<Effort>> groups = activities.stream().collect(groupingBy(Effort::contractId));
        if (groups.keySet().size() > 1) {
            groups.keySet().forEach(o -> generateEffortsForContract(groups.get(o), helper));
            groups.keySet().forEach(o -> generateEffortsTotalForContract(groups.get(o), o, helper));
        } else {
            generateEffortsForContract(logic.collect(assignment, from, to), helper);
        }
        int totalDuration = activities.stream().map(Effort::duration).reduce(0, Integer::sum);
        helper.tableRow("", "Total Time (time in %s)".formatted(text(unit)), convert(totalDuration, unit).toString());
    }

    private void createMinutes(@NotNull Assignment assignment, @NotNull LocalDate from, @NotNull LocalDate to, @NotNull AsciiDocHelper helper) {
        List<Effort> efforts = logic.collect(assignment, from, to);
        boolean containsMinutes = efforts.stream().anyMatch(o -> !Strings.isNullOrBlank(o.minutes()));
        if (containsMinutes) {
            helper.header("Minutes", 2);
        }
        efforts.stream().filter(o -> !Strings.isNullOrBlank(o.minutes())).forEach(o -> {
            helper.header("Minutes for %s".formatted(o.date().toString()), 3);
            helper.paragraph(o.minutes());
        });
    }

    private void generateEffortsForContract(@NotNull List<Effort> efforts, @NotNull AsciiDocHelper helper) {
        var sortedEfforts = new ArrayList<>(efforts);
        sortedEfforts.sort(Comparator.comparing(Effort::date));
        sortedEfforts.forEach(o -> helper.tableRow(o.date().toString(), o.text(), convert(o.duration(), unit).toString()));
        helper.tableRow("", "", "");
    }

    private void generateEffortsTotalForContract(@NotNull List<Effort> efforts, @NotNull String contractId, @NotNull AsciiDocHelper helper) {
        int totalDuration = efforts.stream().map(Effort::duration).reduce(0, Integer::sum);
        helper.tableRow("Total Time for Contract %s".formatted(contractId), "(Time in %s".formatted(text(unit)), convert(totalDuration, unit).toString());
        helper.tableRow("", "", "");
    }

    private static String text(@NotNull ChronoUnit unit) {
        return unit.name().toLowerCase();
    }
}
