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

package net.tangly.erp.invoices.artifacts;

import net.tangly.core.Address;
import net.tangly.core.domain.DocumentGenerator;
import net.tangly.core.domain.DomainAudit;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceItem;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustangproject.ZUGFeRD.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class InvoiceZugFerd implements IExportableTransaction, DocumentGenerator<Invoice> {
    record TradeParty(@NotNull InvoiceLegalEntity entity) implements IZUGFeRDExportableTradeParty {
        @Override
        public IZUGFeRDExportableContact getContact() {
            return new Contact(entity, entity.address());
        }

        @Override
        public String getName() {
            return entity.name();
        }

        @Override
        public String getVATID() {
            return entity.vatNr();
        }

        @Override
        public String getCountry() {
            return entity.address().country();
        }

        @Override
        public String getLocation() {
            return entity.address().locality();
        }

        @Override
        public String getStreet() {
            return entity.address().street();
        }

        @Override
        public String getZIP() {
            return entity.address().postcode();
        }
    }

    /**
     * Implements a ZugFerd exportable contact and maps a legal entity to ZugFerd abstraction.
     */
    record Contact(@NotNull InvoiceLegalEntity entity, @NotNull Address address) implements IZUGFeRDExportableContact {
        @Override
        public String getName() {
            return entity.name();
        }

        @Override
        public String getVATID() {
            return entity.vatNr();
        }

        @Override
        public String getCountry() {
            return address.country();
        }

        @Override
        public String getLocation() {
            return address.locality();
        }

        @Override
        public String getStreet() {
            return address.street();
        }

        @Override
        public String getZIP() {
            return address.postcode();
        }
    }

    /**
     * Implements a ZugFerd exportable item and maps the invoice item to ZugFerd abstraction.
     */
    record Item(@NotNull InvoiceItem item) implements IZUGFeRDExportableItem {
        @Override
        public IZUGFeRDExportableProduct getProduct() {
            return new Product(item.article(), item.vatRate());
        }

        @Override
        public IZUGFeRDAllowanceCharge[] getItemAllowances() {
            return new IZUGFeRDAllowanceCharge[0];
        }

        @Override
        public IZUGFeRDAllowanceCharge[] getItemCharges() {
            return new IZUGFeRDAllowanceCharge[0];
        }

        @Override
        public BigDecimal getPrice() {
            return item.unitPrice();
        }

        @Override
        public BigDecimal getQuantity() {
            return item.quantity();
        }
    }

    /**
     * Implements a ZugFerd exportable product and maps the product to the ZugFerd abstraction.
     */
    record Product(@NotNull Article article, BigDecimal vatRate) implements IZUGFeRDExportableProduct {
        @Override
        public String getUnit() {
            return article.unit();
        }

        @Override
        public String getName() {
            return article.id();
        }

        @Override
        public String getDescription() {
            return article.text();
        }

        @Override
        public BigDecimal getVATPercent() {
            return vatRate.multiply(HUNDRED);
        }
    }

    record Payment(@NotNull Invoice invoice) implements IZUGFeRDTradeSettlementPayment {
        @Override
        public String getOwnBIC() {
            return invoice.invoicingConnection().bic();
        }

        @Override
        public String getOwnIBAN() {
            return invoice.invoicingConnection().iban();
        }
    }

    private static final Logger logger = LogManager.getLogger();
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private Invoice invoice;

    public void export(@NotNull Invoice invoice, boolean overwrite, @NotNull Path invoicePath, @NotNull DomainAudit audit) {
        this.invoice = invoice;
        try {
            IZUGFeRDExporter exporter =
                new ZUGFeRDExporterFromPDFA().load(Files.readAllBytes(invoicePath)).setProducer("tangly ERP").setCreator(invoice.invoicingEntity().name());
            exporter.setTransaction(this);
            exporter.export(new BufferedOutputStream(Files.newOutputStream(invoicePath)));
        } catch (IOException e) {
            logger.atError().withThrowable(e).log("Could not read or write file {}", invoicePath);
        }
    }

    @Override
    public String getCurrency() {
        return invoice.currency().getCurrencyCode();
    }

    @Override
    public Date getDeliveryDate() {
        return java.sql.Date.valueOf(invoice.date());
    }

    @Override
    public Date getDueDate() {
        return java.sql.Date.valueOf(invoice.dueDate());
    }

    @Override
    public Date getIssueDate() {
        return java.sql.Date.valueOf(invoice.date());
    }

    @Override
    public String getNumber() {
        return invoice.id();
    }

    @Override
    public String getOwnCountry() {
        return invoice.invoicedEntity().address().country();
    }


    @Override
    public String getOwnLocation() {
        return invoice.invoicingEntity().address().locality();
    }

    @Override
    public String getOwnOrganisationFullPlaintextInfo() {
        return "%s %s".formatted(invoice.invoicedEntity().name(), invoice.invoicingEntity().address().text());
    }

    @Override
    public String getOwnOrganisationName() {
        return invoice.invoicingEntity().name();
    }

    @Override
    public String getOwnStreet() {
        return invoice.invoicingEntity().address().street();
    }

    @Override
    public String getOwnTaxID() {
        return invoice.invoicingEntity().id();
    }

    @Override
    public String getOwnVATID() {
        return invoice.invoicingEntity().vatNr();
    }

    @Override
    public String getOwnZIP() {
        return invoice.invoicingEntity().address().postcode();
    }

    @Override
    public String getPaymentTermDescription() {
        return invoice.paymentConditions();
    }

    @Override
    public IZUGFeRDExportableTradeParty getSender() {
        return new TradeParty(invoice.invoicingEntity());
    }

    @Override
    public IZUGFeRDExportableTradeParty getRecipient() {
        return new TradeParty(invoice.invoicedEntity());
    }

    @Override
    public String getReferenceNumber() {
        return invoice.id();
    }

    @Override
    public IZUGFeRDExportableItem[] getZFItems() {
        return invoice.items().stream().map(Item::new).toList().toArray(new Item[0]);
    }
}
