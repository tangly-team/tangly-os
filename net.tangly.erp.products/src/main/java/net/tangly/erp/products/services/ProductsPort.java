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

import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.Port;
import net.tangly.erp.products.domain.Assignment;
import org.eclipse.serializer.exceptions.IORuntimeException;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Defines the import and export port for the products-bounded domain. It is the primary port in the DDD terminology.
 */
public interface ProductsPort extends Port<ProductsRealm> {
    ProductsBusinessLogic logic();

    /**
     * Imports all efforts defined in the YAML file located at the given uri.
     * An effort is identified by the combination of the assignment identifier and the date of the effort. The assignment identifies uniquely the collaborator working on the effort.
     *
     * @param reader  reader containing the efforts to import
     * @param source  source of the efforts used to audit the import operation
     * @param replace if true, the imported ones replace the existing efforts, otherwise the imported efforts are only imported if not present
     */
    void importEfforts(@NotNull DomainAudit audit, @NotNull Reader reader, @NotNull String source, boolean replace) throws IORuntimeException;

    /**
     * Exports all efforts of the assignment in the given period of time. The export shall be an asciidoc document.
     * The document name should contain the contract identifier, the collaborator identifier, and the period of time.
     * The prefix should be the year and month of the period end. The prefix is used to sort the documents.
     *
     * @param assignment assignment which efforts are of interest
     * @param from       start of the considered time interval
     * @param to         end of the considered time interval
     * @param filename   name of the file to create
     * @param unit       unit of the time interval (minutes, hours, and days)
     */
    void exportEffortsDocument(@NotNull DomainAudit audit, @NotNull Assignment assignment, LocalDate from, LocalDate to, @NotNull String filename,
                               @NotNull ChronoUnit unit);

    /**
     * Exports all efforts of the assignment in the given period of time. The export shall create an asciidoc document per month.
     * The document name should contain the contract identifier, the collaborator identifier, and the period of time.
     * The prefix should be the year and month of the period end. The prefix is used to sort the documents.
     *
     * @param assignment assignment which efforts are of interest
     * @param from       first month to consider
     * @param to         last month to consider
     * @param unit       unit of the time interval (minutes, hours, and days)
     */
    void exportEffortsDocumentsSplitPerMonth(@NotNull DomainAudit audit, @NotNull Assignment assignment, @NotNull YearMonth from, @NotNull YearMonth to,
                                             @NotNull ChronoUnit unit);
}
