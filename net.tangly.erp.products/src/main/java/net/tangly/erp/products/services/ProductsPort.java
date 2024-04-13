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

package net.tangly.erp.products.services;

import net.tangly.core.domain.Port;
import net.tangly.erp.products.domain.Assignment;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Defines the import and export port for the products-bounded domain. It is the primary port in the DDD terminology.
 */
public interface ProductsPort extends Port<ProductsRealm> {
    ProductsBusinessLogic logic();

    /**
     * Imports all efforts defined in the YAML file located at the given path.
     * An effort is identified by the combination of the assignment identifier and the date of the effort. The assigement identifies uniquely the collaborator working on the effort.
     *
     * @param path    path to the YAML defining the efforts to be imported
     * @param replace if true, the existing efforts are replaced by the imported ones, otherwise the imported efforts are only imported if not preesent
     */
    void importEfforts(@NotNull Path path, boolean replace) throws IORuntimeException;

    /**
     * Exports all efforts of the assignment in the given period of time. The export shall be a asciidoc document.
     * The document name should contain the contract identifier, the collaborator identifier, and the period of time.
     * The prefix should be the year and month of the period end. The prefix is used to sort the documents.
     *
     * @param assignment assignment which efforts are of interest
     * @param from       start of the considered time interval
     * @param to         end of the considered time interval
     */
    void exportEffortsDocument(@NotNull Assignment assignment, LocalDate from, LocalDate to);
}
