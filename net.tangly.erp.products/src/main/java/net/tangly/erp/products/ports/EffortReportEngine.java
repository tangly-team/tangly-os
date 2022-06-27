/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.products.ports;

import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * engine to create effort reports for collaborators.
 */
public class EffortReportEngine {
    private static final Logger logger = LogManager.getLogger();

    private final ProductsBusinessLogic logic;

    public EffortReportEngine(@NotNull ProductsBusinessLogic logic) {
        this.logic = logic;
    }

    public void createPdfStream(@NotNull Assignment assignment, LocalDate from, LocalDate to) {
        // TODO write pdf generation
    }

    public void createAsciiDocReport(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull Path reportPath) {
        try (var writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
            logger.atInfo().log("Create assignement report {}", reportPath);
            createAsciiDocReport(assignment, from, to, writer);
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Error during reporting generation of {}", reportPath);
        }
    }

    private void createAsciiDocReport(@NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull Writer writer) {
        final AsciiDocHelper helper = new AsciiDocHelper(writer);
        helper.header("Work Report", 1);
        helper.tableHeader("work-report", "cols=\"1,5a,>1\", options=\"header\"");
        helper.writer().println("^|Date ^|Description ^|Duration");
        helper.writer().println();

        Map<String, List<Effort>> groups = logic.collect(assignment, from, to).stream().collect(groupingBy(Effort::contractId));
        if (groups.keySet().size() > 1) {
            groups.keySet().forEach(o -> generateEffortsForContract(groups.get(o), helper));
            groups.keySet().forEach(o -> generateEffortsTotalForContract(groups.get(o), o, helper));
        } else {
            logic.collect(assignment, from, to).forEach(o -> helper.tableRow(o.date().toString(), o.text(), Integer.toString(o.duration())));
        }
        int totalDuration = logic.collect(assignment, from, to).stream().map(Effort::duration).reduce(0, Integer::sum);
        BigDecimal totalHours = new BigDecimal(totalDuration).divide(new BigDecimal(60));
        helper.tableRow("Total Time", "(time in minutes " + totalDuration + ")", totalHours.toString());
        helper.tableEnd();
    }

    private void generateEffortsForContract(@NotNull List<Effort> efforts, @NotNull AsciiDocHelper helper) {
        efforts.forEach(o -> helper.tableRow(o.date().toString(), o.text(), Integer.toString(o.duration())));
        helper.tableRow("", "", "");
    }

    private void generateEffortsTotalForContract(@NotNull List<Effort> efforts, @NotNull String contractId, @NotNull AsciiDocHelper helper) {
        int totalDuration = efforts.stream().map(Effort::duration).reduce(0, Integer::sum);
        BigDecimal totalHours = new BigDecimal(totalDuration).divide(new BigDecimal(60));
        helper.tableRow("Total Time for Contract " + contractId, "(time in minutes " + totalDuration + ")", totalHours.toString());
        helper.tableRow("", "", "");
    }
}
