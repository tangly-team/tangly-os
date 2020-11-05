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

import java.nio.file.Path;
import java.time.LocalDate;

import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.bus.products.ProductsPort;
import net.tangly.commons.utilities.AsciiDoctorHelper;

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

    public void exportEffortsDocument(Assignment assignment, LocalDate from, LocalDate to) {
        String collaborator = assignment.name().replace(",", "_").replace(" ", "");
        Path assignmentDocumentPath =
            folder.resolve(assignment.id() + "-" + collaborator + "-" + from.toString() + "_" + to.toString() + AsciiDoctorHelper.ASCIIDOC_EXT);
        EffortReportAsciiDoc helper = new EffortReportAsciiDoc(logic);
        helper.create(assignment, from, to, assignmentDocumentPath);
    }
}
