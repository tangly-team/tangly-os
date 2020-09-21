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

package net.tangly.crm.ports;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.tangly.bus.invoices.Invoice;
import net.tangly.invoices.ports.InvoiceJson;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the workflows defined for CRM activities. Currently we assume that invoices are often handled through the CRM subsystem.
 */
public final class CrmWorkflows {
    public static final String COMMENTS_TSV = "comments.tsv";
    public static final String LEGAL_ENTITIES_TSV = "legal-entities.tsv";
    public static final String NATURAL_ENTITIES_TSV = "natural-entities.tsv";
    public static final String EMPLOYEES_TSV = "employees.tsv";
    public static final String CONTRACTS_TSV = "contracts.tsv";
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String INTERACTIONS_TSV = "interactions.tsv";
    public static final String ACTIVITIES_TSV = "activities.tsv";
    public static final String SUBJECTS_TSV = "subjects.tsv";
    public static final String INVOICES = "invoices";
    public static final String REPORTS = "reports";
    public static final String JSON_EXT = ".json";
    public static final String VCARDS_FOLDER = "vcards";
    public static final String INVOICE_NAME_PATTERN = "\\d{4}-\\d{4}-.*";

    private static final Logger logger = LoggerFactory.getLogger(CrmWorkflows.class);
    private final Pattern invoicePattern;
    private final Crm crm;
    private final CrmBusinessLogic crmBusinessLogic;

    public CrmWorkflows(@NotNull Crm crm) {
        this.crm = crm;
        this.crmBusinessLogic = new CrmBusinessLogic(crm);
        invoicePattern = Pattern.compile(INVOICE_NAME_PATTERN);
    }

    public Crm crm() {
        return crm;
    }

    /**
     * Import all CRM domain entities defined in a set of TSV files.
     *
     * @param directory directory where the TSV files are stored
     * @see #exportCrmEntities(Path) 
     */
    public void importCrmEntities(@NotNull Path directory) {
        CrmTsvHdl handler = new CrmTsvHdl(crm);
        handler.importLegalEntities(directory.resolve(LEGAL_ENTITIES_TSV));
        handler.importNaturalEntities(directory.resolve(NATURAL_ENTITIES_TSV));
        handler.importEmployees(directory.resolve(EMPLOYEES_TSV));
        handler.importContracts(directory.resolve(CONTRACTS_TSV));
        handler.importProducts(directory.resolve(PRODUCTS_TSV));
        handler.importInteractions(directory.resolve(INTERACTIONS_TSV));
        handler.importActivities(directory.resolve(ACTIVITIES_TSV));
        handler.importSubjects(directory.resolve(SUBJECTS_TSV));

        handler.importComments(directory.resolve(COMMENTS_TSV));

        Path invoicesFolder = directory.resolve(INVOICES);
        if (Files.exists(invoicesFolder)) {
            importInvoices(invoicesFolder);
        } else {
            logger.atWarn().log("Invoices folder {} does not exist", invoicesFolder);
        }

        CrmVcardHdl crmVcardHdl = new CrmVcardHdl(crm());
        crmVcardHdl.importVCards(directory.resolve(VCARDS_FOLDER));
    }

    /**
     * Import all invoices to the file system. All invoices are imported from directory/INVOICES. If the name of the invoice starts with a four digits pattern,
     * it is assumed that it represents the year when the invoice was issued. 
     * 
     * @param directory directory in which the invoices will be written
     * @see #importInvoices(Path)
     */
    public void importInvoices(@NotNull Path directory) {
        InvoiceJson invoiceJson = new InvoiceJson(crm);
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(JSON_EXT)).forEach(o -> {
                Invoice invoice = invoiceJson.imports(o, Collections.emptyMap());
                if (invoice != null) {
                    crm.invoices().update(invoice);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Export all CRM domain entities into a set of TSV files.
     *
     * @param directory directory where the TSV files are stored
     * @see #importCrmEntities(Path)
     */
    public void exportCrmEntities(@NotNull Path directory) {
        CrmTsvHdl handler = new CrmTsvHdl(crm);
        handler.exportLegalEntities(directory.resolve(LEGAL_ENTITIES_TSV));
        handler.exportNaturalEntities(directory.resolve(NATURAL_ENTITIES_TSV));
        handler.exportEmployees(directory.resolve(EMPLOYEES_TSV));
        handler.exportProducts(directory.resolve(PRODUCTS_TSV));
        handler.exportContracts(directory.resolve(CONTRACTS_TSV));
        handler.exportInteractions(directory.resolve(INTERACTIONS_TSV));
        handler.exportActivities(directory.resolve(ACTIVITIES_TSV));
        handler.exportSubjects(directory.resolve(SUBJECTS_TSV));
        handler.exportComments(directory.resolve(COMMENTS_TSV));
        exportInvoices(directory);
    }

    /**
     * Export all invoices to the file system. All invoices are created under directory/INVOICES. If the name of the invoice starts with a four digits pattern,
     * it is assumed that it represents the year when the invoice was issued. A folder with the year will be created and the invoice will be written within.
     *
     * @param directory directory in which the invoices will be written
     * @see #exportInvoices(Path) 
     */
    public void exportInvoices(@NotNull Path directory) {
        InvoiceJson invoiceJson = new InvoiceJson(crm);
        crm.invoices().items().forEach(o -> {
            Path invoicePath = resolvePath(o, directory);
            invoiceJson.exports(o, invoicePath.resolve(o.name() + JSON_EXT), Collections.emptyMap());
        });
    }

    public void exportInvoiceToPdf(@NotNull Path directory, @NotNull Invoice invoice) {
        crmBusinessLogic.exportInvoiceDocument(invoice, resolvePath(invoice, directory.resolve(REPORTS)), true, true);
    }

    /**
     * Resolve the path to where an invoice should be located in the file system. The convention is <i>base directory/invoices/year</i>. If folders do not exist
     * they are created.
     *
     * @param invoice   invoice to write
     * @param directory base directory
     * @return path to the folder where the invoic should be outputed
     */
    public Path resolvePath(@NotNull Invoice invoice, @NotNull Path directory) {
        Path invoicesPath = directory.resolve(INVOICES);
        Matcher matcher = invoicePattern.matcher(invoice.name());
        Path invoicePath = matcher.matches() ? invoicesPath.resolve(invoice.name().substring(0, 4)) : invoicesPath;
        if (Files.notExists(invoicePath)) {
            try {
                Files.createDirectories(invoicePath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return invoicePath;
    }
}
