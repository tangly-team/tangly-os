/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.ledger.reports;


import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.erp.ledger.Account;
import net.tangly.erp.ledger.Ledger;
import net.tangly.erp.ledger.LedgerCsvHdl;
import net.tangly.erp.ledger.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * A complete accounting report for a specific time interval. We suggest you use year, half-year and quarter reports. The output format is AsciiDoc.
 */
public class ClosingReportAsciiDoc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClosingReportAsciiDoc.class);
    private final Ledger ledger;
    private final BigDecimal retainedVatFactor = new BigDecimal("0.01307");
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public static void main(String[] args) throws IOException {
        Locale.setDefault(new Locale("en", "US"));
        LedgerCsvHdl handler = new LedgerCsvHdl(new Ledger());
        handler.importStructureLedgerFromBanana8(Paths.get("/Users/Shared/tmp/tangly-swiss-ledger.csv"));
        handler.importTransactionsLedgerFromBanana8(Paths.get("/Users/Shared/tmp/tangly-period-2016.csv"));
        handler.importTransactionsLedgerFromBanana8(Paths.get("/Users/Shared/tmp/tangly-period-2017.csv"));
        handler.importTransactionsLedgerFromBanana8(Paths.get("/Users/Shared/tmp/tangly-period-2018.csv"));

        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(handler.ledger());
        report.create(LocalDate.parse("2015-10-01"), LocalDate.parse("2016-12-31"), Paths.get("/Users/Shared/tmp/closing-test-2016.adoc"));
        report.create(LocalDate.parse("2017-01-01"), LocalDate.parse("2017-12-31"), Paths.get("/Users/Shared/tmp/closing-test-2017.adoc"));
        report.create(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31"), Paths.get("/Users/Shared/tmp/closing-test-2018.adoc"));
    }

    /**
     * Constructor of the closing report. Multiple reports with variable reporting period length can be generated with
     * the same instance.
     *
     * @param ledger ledger containing all the accounting information for the reports
     */
    public ClosingReportAsciiDoc(Ledger ledger) {
        this.ledger = ledger;
    }

    public void create(LocalDate from, LocalDate to, Path reportPath) {
        try (PrintWriter out = new PrintWriter(reportPath.toFile(), StandardCharsets.UTF_8)) {
            final AsciiDocHelper helper = new AsciiDocHelper(out);
            helper.header("Balance Sheet", 1);
            generateResultTableFor(helper, ledger.liabilities(), from, to, "Liabilities");
            generateResultTableFor(helper, ledger.assets(), from, to, "Assets");
            generateResultTableFor(helper, ledger.profitAndLoss(), from, to, "Profits and Losses");

            helper.header("VAT Contributions", 1);
            helper.tableHeader("VAT", "120, 20, 20 , 20", "Period", "Turnover", "Due VAT", "Retained VAT");
            BigDecimal turnover = ledger.computeVatSales(from, to);
            BigDecimal dueVat = ledger.computeDueVat(from, to);
            BigDecimal retainedVat = turnover.multiply(new BigDecimal("0.01307"));
            createVatRow(helper, "Period from " + from + " to " + to, turnover, dueVat, retainedVat);
            helper.tableEnd();

            helper.tableHeader("VAT", "120, 20, 20 , 20", "Period", "Turnover", "Due VAT", "Retained VAT");
            addVatRows(helper, from.getYear());
            if (from.getYear() != to.getYear()) {
                addVatRows(helper, to.getYear());
            }
            helper.tableEnd();

            helper.header("Transactions", 1);
            helper.tableHeader("VAT", "20, 20, 70 , 15, 15, 20, 15", "Date", "Voucher", "Description", "Debit", "Credit", "Amount", "VAT");
            ledger.transactions(from, to).forEach(o -> createTransactionRow(helper, o));
            helper.tableEnd();
        } catch (Exception e) {
            log.error("Error during reporting", e);
        }
    }

    private void generateResultTableFor(AsciiDocHelper helper, List<Account> accounts, LocalDate from, LocalDate to, String category) {
        helper.header(category, 2);
        helper.tableHeader(category, "25, 150, 20, 25, 25", "Account", "Description", "Kind", "Balance", "Initial Balance");
        accounts.forEach(o -> createBalanceRow(helper, o, from, to));
        helper.tableEnd();
    }

    private void addVatRows(AsciiDocHelper helper, int year) {
        LocalDate periodStart = LocalDate.of(year, 1, 1);
        LocalDate periodEnd = LocalDate.of(year, 6, 30);
        BigDecimal earningH1 = ledger.computeVatSales(periodStart, periodEnd);
        BigDecimal vatH1 = ledger.computeDueVat(periodStart, periodEnd);

        periodStart = LocalDate.of(year, 7, 1);
        periodEnd = LocalDate.of(year, 12, 31);
        BigDecimal earningH2 = ledger.computeVatSales(periodStart, periodEnd);
        BigDecimal vatH2 = ledger.computeDueVat(periodStart, periodEnd);

        // TODO compute the correct retained VAT Factor F1 -> 0.01307, F3 -> ?
        createVatRow(helper, "First Half Year " + year, earningH1, vatH1, earningH1.multiply(retainedVatFactor));
        createVatRow(helper, "Second Half Year " + year, earningH2, vatH2, earningH2.multiply(retainedVatFactor));
        createVatRow(helper, "Totals Year " + year, earningH1.add(earningH2), vatH1.add(vatH2),
                earningH1.multiply(retainedVatFactor).add(earningH2.multiply(retainedVatFactor)));
    }

    private void createVatRow(AsciiDocHelper helper, String text, BigDecimal turnover, BigDecimal dueVat, BigDecimal difference) {
        helper.tableRow(text, valueToText(turnover), valueToText(dueVat), valueToText(difference));
    }


    private void createTransactionRow(AsciiDocHelper helper, Transaction transaction) {
        helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.description(), transaction.debitAccount(),
                transaction.creditAccount(), transaction.amount().toString(), "-");
    }


    private void createBalanceRow(AsciiDocHelper helper, Account account, LocalDate from, LocalDate to) {
        BigDecimal fromBalance = account.balance(from.minusDays(1));
        BigDecimal toBalance = account.balance(to);

        if (BigDecimal.ZERO.equals(fromBalance) && BigDecimal.ZERO.equals(toBalance)) {
            return;
        }
        helper.tableRow(account.id(), account.description(), account.isAggregate() ? "" : account.kind().name().toLowerCase(), valueToText(toBalance),
                valueToText(fromBalance));
    }

    private String valueToText(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) >= 0) {
            return value.toString();
        } else {
            return "[red]#" + value.toString() + "#";
        }
    }
}
