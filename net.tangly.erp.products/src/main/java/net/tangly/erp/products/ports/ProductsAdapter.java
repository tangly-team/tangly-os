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
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class ProductsAdapter implements ProductsPort {
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String ASSIGNMENTS_TSV = "assignments.tsv";
    public static final String EFFORTS_TSV = "efforts.tsv";

    private final ProductsRealm realm;

    private final ProductsBusinessLogic logic;

    private final Path folder;

    public ProductsAdapter(@NotNull ProductsRealm realm, @NotNull ProductsBusinessLogic logic, @NotNull Path folder) {
        this.realm = realm;
        this.logic = logic;
        this.folder = folder;
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
        Port.importEntities(folder, PRODUCTS_TSV, handler::importProducts);
        Port.importEntities(folder, ASSIGNMENTS_TSV, handler::importAssignments);
        Port.importEntities(folder, EFFORTS_TSV, handler::importEfforts);
    }

    @Override
    public void exportEntities() {
        var handler = new ProductsTsvHdl(realm());
        handler.exportProducts(folder.resolve(PRODUCTS_TSV));
        handler.exportAssignments(folder.resolve(ASSIGNMENTS_TSV));
        handler.exportEfforts(folder.resolve(EFFORTS_TSV));
    }

    @Override
    public void clearEntities() {
        realm().efforts().deleteAll();
        realm().assignments().deleteAll();
        realm().products().deleteAll();
    }

    @Override
    public void importEfforts(@NotNull InputStream stream, @NotNull String source, boolean replace) throws IORuntimeException {
        try {
            YamlMapping data = Yaml.createYamlInput(stream).readYamlMapping();
            String contractId = data.string("contractId");
            String collaborator = data.string("collaborator");
            long assignmentOid = data.longNumber("assignmentOid");
            Assignment assignment = Provider.findByOid(logic.realm().assignments(), assignmentOid).orElse(null);

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
                        EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.INFO, " effort replaced already exists.",
                            Map.of("filename", source, "entity", newEffort));

                    } else {
                        EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.WARNING, " effort could not be imported because it already exists.",
                            Map.of("filename", source, "entity", newEffort));
                    }
                } else {
                    logic.realm().efforts().update(newEffort);
                    EventData.log(EventData.IMPORT, ProductsBoundedDomain.DOMAIN, EventData.Status.INFO, " effort added.",
                        Map.of("filename", source, "entity", newEffort));
                }
            });
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public void exportEffortsDocument(@NotNull Assignment assignment, LocalDate from, LocalDate to) {
        String collaborator = assignment.name().replace(",", "_").replace(" ", "");
        var assignmentDocumentPath = folder.resolve(STR."\{assignment.id()}-\{collaborator}-\{from.toString()}_\{to.toString()}\{AsciiDoctorHelper.ASCIIDOC_EXT}");
        var helper = new EffortReportEngine(logic);
        helper.createAsciiDocReport(assignment, from, to, assignmentDocumentPath);
    }
}
