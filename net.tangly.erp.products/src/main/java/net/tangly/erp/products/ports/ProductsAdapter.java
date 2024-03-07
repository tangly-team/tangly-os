/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
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
import net.tangly.commons.utilities.AsciiDoctorHelper;
import net.tangly.core.providers.Provider;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.services.ProductsPort;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Products adapter is an adapter for the products port defined as a secondary port. The port has access to
 * <ul>
 *     <li>realm of the products bounded domain and associated entities</li>
 *     <li>folder to the root directory containing all assignment reports and documents</li>
 * </ul>
 */
public class ProductsAdapter implements ProductsPort {
    private final ProductsBusinessLogic logic;
    private final Path folder;

    public ProductsAdapter(ProductsBusinessLogic logic, Path folder) {
        this.logic = logic;
        this.folder = folder;
    }

    public void importEfforts(@NotNull Path path, boolean replace) throws IOException {

        InputStream stream = Files.newInputStream(path);
        YamlMapping data = Yaml.createYamlInput(stream).readYamlMapping();
        long assignmentId = data.longNumber("assignmentId");
        String contractId = data.string("contractId");
        String collaborator = data.string("collaborator");
        YamlSequence efforts = data.yamlSequence("efforts");

        Assignment assignment = Provider.findByOid(logic.realm().assignments(), assignmentId).orElse(null);
        efforts.children().forEach((YamlNode effort) -> {
            LocalDate date = effort.asMapping().date("date");
            int duration = effort.asMapping().integer("duration");
            String text = effort.asMapping().string("text");
            Optional<Effort> foundEffort = logic.findEffortFor(assignmentId, collaborator, date);
            if (foundEffort.isPresent()) {
                logic.realm().efforts().delete(foundEffort.get());
                logic.realm().efforts().update(foundEffort.get());
                if (replace) {
                    logic.realm().efforts().delete(foundEffort.get());
                    logic.realm().efforts().update(foundEffort.get());
                } else {
                    // TODO log warning effort duplicate cannot be added.
                }
            } else {
                logic.realm().efforts().update(foundEffort.get());
            }
        });
    }

    public void exportEffortsDocument(Assignment assignment, LocalDate from, LocalDate to) {
        String collaborator = assignment.name().replace(",", "_").replace(" ", "");
        var assignmentDocumentPath = folder.resolve(assignment.id() + "-" + collaborator + "-" + from.toString() + "_" + to.toString() + AsciiDoctorHelper.ASCIIDOC_EXT);
        var helper = new EffortReportEngine(logic);
        helper.createAsciiDocReport(assignment, from, to, assignmentDocumentPath);
    }
}
