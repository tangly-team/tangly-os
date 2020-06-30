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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.codecrete.qrbill.canvas.PDFCanvas;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.Payments;
import net.codecrete.qrbill.generator.QRBill;
import net.codecrete.qrbill.generator.SwicoBillInformation;
import net.tangly.bus.core.Address;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import org.jetbrains.annotations.NotNull;

public class InvoiceQrCode implements InvoiceGenerator {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InvoiceQrCode.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public void create(@NotNull Invoice invoice, @NotNull Path invoicePath, @NotNull Map<String, Object> properties) {
        Bill bill = new Bill();

        bill.setFormat(createBillFormat());
        bill.setVersion(Bill.Version.V2_0);

        bill.setCreditor(create(invoice.invoicingEntity()));
        bill.setDebtor(create(invoice.invoicedEntity()));

        bill.setAccount(invoice.invoicingConnection().iban());
        bill.setAmount(invoice.amountWithVat());
        bill.setCurrency(invoice.currency().getCurrencyCode());

        bill.setBillInformation(createSwicoBillInformation(invoice).encodeAsText());
        // reference is the usual reference number of Swiss payment slips
        bill.setReference(Payments.createISO11649Reference(invoice.id().replaceAll("[^A-Za-z0-9]", "")));
        // human readable information text
        bill.setUnstructuredMessage(invoice.text());

        try (PDFCanvas canvas = new PDFCanvas(invoicePath, PDFCanvas.NEW_PAGE_AT_END)) {
            QRBill.draw(bill, canvas);
            canvas.saveAs(invoicePath);
        } catch (IOException e) {
            logger.atError().setCause(e).log("Error when generating QR code for {}", invoicePath);
        }
    }

    private static BillFormat createBillFormat() {
        BillFormat format = new BillFormat();
        format.setLanguage(Language.EN);
        format.setOutputSize(OutputSize.A4_PORTRAIT_SHEET);
        format.setGraphicsFormat(GraphicsFormat.SVG);
        return format;
    }

    /**
     * Create the SWICO information for the QR code. As remarks SIX Swico states that VAT number is without the MWST postfix.
     *
     * @param invoice invoice used to create SWICo information
     * @return new SWICO bill information
     */
    private static SwicoBillInformation createSwicoBillInformation(@NotNull Invoice invoice) {
        SwicoBillInformation swico = new SwicoBillInformation();
        swico.setInvoiceDate(invoice.invoicedDate());
        swico.setInvoiceNumber(invoice.id());
        swico.setVatNumber(invoice.invoicingEntity().id());
        swico.setCustomerReference(invoice.invoicedEntity().id());

        List<SwicoBillInformation.RateDetail> details =
                invoice.vatAmounts().entrySet().stream().filter(o -> o.getValue().compareTo(BigDecimal.ZERO) != 0).
                        map(o -> new SwicoBillInformation.RateDetail(o.getKey().multiply(HUNDRED).stripTrailingZeros(),
                                o.getValue().stripTrailingZeros()
                        )).collect(Collectors.toList());
        if (!details.isEmpty()) {
            swico.setVatRateDetails(details);
        }

        SwicoBillInformation.PaymentCondition paymentCondition = new SwicoBillInformation.PaymentCondition();
        paymentCondition.setDays(30);
        paymentCondition.setDiscount(BigDecimal.ZERO);
        swico.setPaymentConditions(List.of(paymentCondition));
        return swico;
    }

    private static net.codecrete.qrbill.generator.Address create(@NotNull LegalEntity entity) {
        Address address = entity.address(CrmTags.CRM_ADDRESS_WORK);
        net.codecrete.qrbill.generator.Address qrAddress = new net.codecrete.qrbill.generator.Address();
        qrAddress.setName(entity.name());
        qrAddress.setStreet(address.street());
        qrAddress.setHouseNo(null);
        qrAddress.setPostalCode(address.postcode());
        qrAddress.setTown(address.locality());
        qrAddress.setCountryCode(address.country());
        return qrAddress;
    }
}
