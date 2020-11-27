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

package net.tangly.invoices.ports;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import net.tangly.bus.invoices.Article;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.InvoiceLegalEntity;
import net.tangly.core.Address;
import org.jetbrains.annotations.NotNull;
import org.mustangproject.ZUGFeRD.IExportableTransaction;
import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableContact;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableProduct;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableTradeParty;
import org.mustangproject.ZUGFeRD.IZUGFeRDTradeSettlementPayment;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceZugFerd implements IExportableTransaction, InvoiceGenerator {
    static record TradeParty(@NotNull InvoiceLegalEntity entity, @NotNull Address address) implements IZUGFeRDExportableTradeParty {
        @Override
        public IZUGFeRDExportableContact getContact() {
            return new Contact(entity, address);
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
     * Implements a ZugFerd exportable contact and maps a legal entity to ZugFerd abstraction.
     */
    static record Contact(@NotNull InvoiceLegalEntity entity, @NotNull Address address) implements IZUGFeRDExportableContact {
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
    static record Item(@NotNull InvoiceItem item) implements IZUGFeRDExportableItem {
        @Override
        public IZUGFeRDExportableProduct getProduct() {
            return new Product(item.article());
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
    static record Product(@NotNull Article article) implements IZUGFeRDExportableProduct {
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
            return article.vatRate().multiply(HUNDRED);
        }
    }

    static record Payment(@NotNull Invoice invoice) implements IZUGFeRDTradeSettlementPayment {
        @Override
        public String getOwnBIC() {
            return invoice.invoicingConnection().bic();
        }

        @Override
        public String getOwnIBAN() {
            return invoice.invoicingConnection().iban();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private Invoice invoice;

    public void exports(@NotNull Invoice invoice, @NotNull Path invoicePath, @NotNull Map<String, Object> properties) {
        this.invoice = invoice;
        try (ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3().setProducer("tangly ERP").setCreator(invoice.invoicingEntity().name())
            .setZUGFeRDVersion(2).setProfile("EN16931").load(Files.newInputStream(invoicePath))) {
            exporter.setTransaction(this);
            exporter.export(Files.newOutputStream(invoicePath));
        } catch (IOException e) {
            logger.atError().setCause(e).log("Could not read or write file {}", invoicePath);
        }
    }

    @Override
    public String getCurrency() {
        return invoice.currency().getCurrencyCode();
    }

    @Override
    public Date getDeliveryDate() {
        return java.sql.Date.valueOf(invoice.invoicedDate());
    }

    @Override
    public Date getDueDate() {
        return java.sql.Date.valueOf(invoice.dueDate());
    }

    @Override
    public Date getIssueDate() {
        return java.sql.Date.valueOf(invoice.invoicedDate());
    }

    @Override
    public String getNumber() {
        return invoice.id();
    }

    @Override
    public String getOwnCountry() {
        return invoice.invoicingAddress().country();
    }


    @Override
    public String getOwnLocation() {
        return invoice.invoicingAddress().locality();
    }

    @Override
    public String getOwnOrganisationFullPlaintextInfo() {
        return invoice.invoicedEntity().name() + " " + invoice.invoicingAddress().text();
    }

    @Override
    public String getOwnOrganisationName() {
        return invoice.invoicingEntity().name();
    }

    @Override
    public String getOwnStreet() {
        return invoice.invoicingAddress().street();
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
        return invoice.invoicingAddress().postcode();
    }

    @Override
    public String getPaymentTermDescription() {
        return invoice.paymentConditions();
    }

    @Override
    public IZUGFeRDExportableTradeParty getRecipient() {
        return new TradeParty(invoice.invoicedEntity(), invoice.invoicedAddress());
    }

    @Override
    public String getReferenceNumber() {
        return invoice.id();
    }

    @Override
    public IZUGFeRDAllowanceCharge[] getZFAllowances() {
        return null;
    }

    @Override
    public IZUGFeRDAllowanceCharge[] getZFCharges() {
        return null;
    }

    @Override
    public IZUGFeRDExportableItem[] getZFItems() {
        return invoice.items().stream().map(Item::new).collect(Collectors.toUnmodifiableList()).toArray(new Item[0]);
    }

    public IZUGFeRDAllowanceCharge[] getZFLogisticsServiceCharges() {
        return null;
    }
}
