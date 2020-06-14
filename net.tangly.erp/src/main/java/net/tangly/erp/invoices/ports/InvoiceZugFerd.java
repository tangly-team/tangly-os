/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.erp.invoices.ports;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Collectors;

import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.Product;
import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableContact;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableProduct;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableTransaction;
import org.mustangproject.ZUGFeRD.IZUGFeRDTradeSettlementPayment;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1Factory;

public class InvoiceZugFerd implements IZUGFeRDExportableTransaction {
    /**
     * Implements a ZugFerd exportable contact and maps a legal entity to ZugFerd abstraction.
     */
    static class ZugFerdContact implements IZUGFeRDExportableContact {
        private final LegalEntity entity;

        public ZugFerdContact(LegalEntity entity) {
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
            return entity.address(CrmTags.CRM_ADDRESS_WORK).country();
        }

        @Override
        public String getLocation() {
            return entity.address(CrmTags.CRM_ADDRESS_WORK).locality();
        }

        @Override
        public String getStreet() {
            return entity.address(CrmTags.CRM_ADDRESS_WORK).street();
        }

        @Override
        public String getZIP() {
            return entity.address(CrmTags.CRM_ADDRESS_WORK).postcode();
        }
    }

    /**
     * Implments a ZugFerd exportable item and maps the invoice item to ZugFerd abstraction.
     */
    static class ZugFerdItem implements IZUGFeRDExportableItem {
        private InvoiceItem item;

        public ZugFerdItem(InvoiceItem item) {
            this.item = item;
        }

        @Override
        public IZUGFeRDExportableProduct getProduct() {
            return new ZugFerdProduct(item.product(), null);
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
        private final BigDecimal vatPercent;

        public ZugFerdProduct(Product product, BigDecimal vatPercent) {
            this.product = product;
            this.vatPercent = vatPercent;
        }

        @Override
        public String getUnit() {
            return product.unit();
        }

        @Override
        public String getName() {
            return product.productId();
        }

        @Override
        public String getDescription() {
            return product.description();
        }

        @Override
        public BigDecimal getVATPercent() {
            return vatPercent;
        }
    }

    static class ZugFerdPayment implements IZUGFeRDTradeSettlementPayment {
        private Invoice invoice;

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

    private Invoice invoice;

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
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).country();
    }


    @Override
    public String getOwnLocation() {
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).locality();
    }

    @Override
    public String getOwnOrganisationFullPlaintextInfo() {
        return invoice.invoicedEntity().name() + " " + invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).text();
    }

    @Override
    public String getOwnOrganisationName() {
        return invoice.invoicingEntity().name();
    }

    @Override
    public String getOwnStreet() {
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).street();
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
        return invoice.invoicingEntity().address(CrmTags.CRM_ADDRESS_WORK).postcode();
    }

    @Override
    public String getPaymentTermDescription() {
        // TODO Auto-generated method stub
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
        return invoice.positions().stream().map(o -> new ZugFerdItem(o)).collect(Collectors.toUnmodifiableList()).toArray(new ZugFerdItem[0]);
    }

    public IZUGFeRDAllowanceCharge[] getZFLogisticsServiceCharges() {
        return null;
    }

    private void apply(Path invoicePath, String creator) throws IOException {
        ZUGFeRDExporter exporter =
                new ZUGFeRDExporterFromA1Factory().ignorePDFAErrors().setZUGFeRDVersion(2).setProducer("tangly ERP").setCreator(creator)
                        .load(invoicePath.toString());

        exporter.PDFattachZugferdFile(this);

        exporter.export("./MustangGnuaccountingBeispielRE-20190610_507new.pdf");
    }
}
