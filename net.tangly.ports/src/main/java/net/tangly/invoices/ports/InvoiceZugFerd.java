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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import net.tangly.bus.core.Address;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.Product;
import org.jetbrains.annotations.NotNull;
import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableContact;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableProduct;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableTransaction;
import org.mustangproject.ZUGFeRD.IZUGFeRDTradeSettlementPayment;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1Factory;

public class InvoiceZugFerd implements IZUGFeRDExportableTransaction, InvoiceGenerator {
    /**
     * Implements a ZugFerd exportable contact and maps a legal entity to ZugFerd abstraction.
     */
    static class ZugFerdContact implements IZUGFeRDExportableContact {
        private final LegalEntity entity;

        public ZugFerdContact(@NotNull LegalEntity entity) {
            this.entity = entity;
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
            return entity.address(CrmTags.CRM_ADDRESS_WORK).map(Address::country).orElse(null);
        }

        @Override
        public String getLocation() {
            return entity.address(CrmTags.CRM_ADDRESS_WORK).map(Address::locality).orElse(null);
        }

        @Override
        public String getStreet() {
            return entity.address(CrmTags.CRM_ADDRESS_WORK).map(Address::street).orElse(null);
        }

        @Override
        public String getZIP() {
            return entity.address(CrmTags.CRM_ADDRESS_WORK).map(Address::postcode).orElse(null);
        }
    }

    /**
     * Implements a ZugFerd exportable item and maps the invoice item to ZugFerd abstraction.
     */
    static class ZugFerdItem implements IZUGFeRDExportableItem {
        private final InvoiceItem item;

        public ZugFerdItem(@NotNull InvoiceItem item) {
            this.item = item;
        }

        @Override
        public IZUGFeRDExportableProduct getProduct() {
            return new ZugFerdProduct(item.product());
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
    static class ZugFerdProduct implements IZUGFeRDExportableProduct {
        private final Product product;

        public ZugFerdProduct(@NotNull Product product) {
            this.product = product;
        }

        @Override
        public String getUnit() {
            return product.unit();
        }

        @Override
        public String getName() {
            return product.id();
        }

        @Override
        public String getDescription() {
            return product.description();
        }

        @Override
        public BigDecimal getVATPercent() {
            return product.vatRate().multiply(HUNDRED);
        }
    }

    static class ZugFerdPayment implements IZUGFeRDTradeSettlementPayment {
        private final Invoice invoice;

        public ZugFerdPayment(@NotNull Invoice invoice) {
            this.invoice = invoice;
        }

        @Override
        public String getOwnBIC() {
            return invoice.invoicingConnection().bic();
        }

        @Override
        public String getOwnBankName() {
            return invoice.invoicingConnection().institute();

        }

        @Override
        public String getOwnIBAN() {
            return invoice.invoicingConnection().iban();
        }
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InvoiceZugFerd.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private Invoice invoice;

    public void create(@NotNull Invoice invoice, @NotNull Path invoicePath, @NotNull Map<String, Object> properties) {
        this.invoice = invoice;
        try {
            ZUGFeRDExporter exporter = new ZUGFeRDExporterFromA1Factory().ignorePDFAErrors().setZUGFeRDVersion(2).setProducer("tangly ERP")
                    .setCreator(invoice.invoicingEntity().name()).load(invoicePath.toString());
            exporter.PDFattachZugferdFile(this);
            exporter.export(invoicePath.toString());
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
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).map(Address::country).orElse(null);
    }


    @Override
    public String getOwnLocation() {
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).map(Address::locality).orElse(null);
    }

    @Override
    public String getOwnOrganisationFullPlaintextInfo() {
        return invoice.invoicedEntity().name() + " " + invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).map(Address::text).orElse(null);
    }

    @Override
    public String getOwnOrganisationName() {
        return invoice.invoicingEntity().name();
    }

    @Override
    public String getOwnStreet() {
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).map(Address::street).orElse(null);
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
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).map(Address::postcode).orElse(null);
    }

    @Override
    public String getPaymentTermDescription() {
        return null;
    }

    @Override
    public IZUGFeRDExportableContact getRecipient() {
        return new ZugFerdContact(invoice.invoicedEntity());
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
        return invoice.items().stream().map(ZugFerdItem::new).collect(Collectors.toUnmodifiableList()).toArray(new ZugFerdItem[0]);
    }

    public IZUGFeRDAllowanceCharge[] getZFLogisticsServiceCharges() {
        return null;
    }
}
