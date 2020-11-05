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

package net.tangly.products.ports;

import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.commons.utilities.AsciiDocHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffortReportAsciiDoc {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ProductsBusinessLogic logic;

    public EffortReportAsciiDoc(ProductsBusinessLogic logic) {
        this.logic = logic;
    }

    public void create(Assignment assignment, LocalDate from, LocalDate to, Path reportPath) {
        try (Writer writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
            final AsciiDocHelper helper = new AsciiDocHelper(writer);
            helper.header("Work Report", 1);
            helper.tableHeader("work-report", "cols=\"1,5a,>1\", options=\"header\"");
            helper.writer().println("^|Date ^|Description ^|Duration");
            helper.writer().println();

            logic.collect(assignment, from, to).forEach(o -> helper.tableRow(o.date().toString(), o.text(), Integer.toString(o.duration())));

            int totalDuration = logic.collect(assignment, from, to).stream().map(Effort::duration).reduce(0, Integer::sum);
            BigDecimal totalHours = new BigDecimal(totalDuration).divide(new BigDecimal(60));
            helper.tableRow("Total Time", "(time in minutes " + Integer.toString(totalDuration) + ")", totalHours.toString());
            helper.tableEnd();
        } catch (IOException e) {
            logger.error("Error during reporting", e);
        }
    }
}