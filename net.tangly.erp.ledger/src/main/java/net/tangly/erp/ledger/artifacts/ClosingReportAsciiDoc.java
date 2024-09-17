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

package net.tangly.erp.ledger.artifacts;


import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.commons.utilities.BigDecimalUtilities;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.domain.VatCode;
import net.tangly.erp.ledger.services.LedgerRealm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.tangly.commons.utilities.AsciiDocHelper.format;

/**
 * A complete accounting report for a specific time interval. We suggest you use year, half-year, and quarter ports. The output format is AsciiDoc.
 */
public class ClosingReportAsciiDoc {
    private static final Logger logger = LogManager.getLogger();
    private final LedgerRealm realm;
    private final TypeRegistry registry;

    /**
     * Constructor of the closing report. Multiple ports with variable reporting period length can be generated with the same instance.
     *
     * @param realm    ledger realm containing all the accounting information for the ports
     * @param registry registry containing the VAT codes
     */
    public ClosingReportAsciiDoc(LedgerRealm realm, TypeRegistry registry) {
        this.realm = realm;
        this.registry = registry;
    }

    public void create(LocalDate from, LocalDate to, Path reportPath, boolean withBalanceSheet, boolean withProfitsAndLosses, boolean withEmptyAccounts,
                       boolean withTransactions, boolean withVat) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(reportPath), true, StandardCharsets.UTF_8)) {
            create(from, to, writer, withBalanceSheet, withProfitsAndLosses, withEmptyAccounts, withTransactions, withVat);
        } catch (IOException e) {
            logger.error("Error during reporting", e);
        }
    }

    /**
     * Creates the accounting report for the specified period. The report can contain the balance sheet, the profit and loss statement, the VAT report and the
     *
     * @param from                 start date of the report
     * @param to                   end date of the report
     * @param writer               writer to write the report
     * @param withBalanceSheet     flag to include the balance sheet in the report
     * @param withProfitsAndLosses flag to include the profit and loss statement in the report
     * @param withTransactions     flag to include the transactions in the report
     * @param withVat              flag to include the VAT report in the report
     */
    public void create(LocalDate from, LocalDate to, PrintWriter writer, boolean withBalanceSheet, boolean withProfitsAndLosses, boolean withEmptyAccounts,
                       boolean withTransactions, boolean withVat) {
        var helper = new AsciiDocHelper(writer);
        String folder = "/var/tangly-erp/tenant-tangly/docs";
        writer.println("= Yearly Closing Report");
        writer.println("tangly llc, Lorzenhof 27, 6330 Cham, Switzerland");
        writer.println("Version 1.0");
        writer.println(":doctype: book");
        writer.println(":organization: " + "tangly llc");
        writer.println(":copyright: " + "Lorzenhof 27, 6330 Cham, Switzerland");
        writer.printf(":title-logo-image: image:%s/tenant-logo.svg[top=25%%,align=center,pdfwidth=40mm]%n", folder);
        writer.printf(":pdf-themesdir: %s%n", folder);
        writer.println(":pdf-theme: " + "tenant");
        writer.println(":icons: font");
        writer.println(":sectnums:");
        writer.println(":sectlinks:");
        writer.println(":sectanchors:");
        writer.println(":!chapter-signifier:");
        writer.println(":toclevels: 3");
        writer.println(":toc:");
        writer.println();

        introduction(helper, from, to);

        if (withBalanceSheet || withProfitsAndLosses) {
            helper.header("Balance Sheet", 2);
        }
        if (withBalanceSheet) {
            addResultBalanceTableFor(helper, realm.assets(), from, to, "Assets", withEmptyAccounts);
            addResultBalanceTableFor(helper, realm.liabilities(), from, to, "Liabilities", withEmptyAccounts);
        }
        if (withProfitsAndLosses) {
            addResultIncomeTableFor(helper, realm.profitAndLoss(), from, to, "Profits and Losses", withEmptyAccounts);
        }
        if (withTransactions) {
            helper.header("Transactions", 2);
            helper.tableHeader(null, "cols=\"20, 20, 70 , 15, 15, >20, >15\"", "Date", "Voucher", "Description", "Debit", "Credit", "Amount", "VAT");
            realm.transactions(from, to).forEach(o -> addTransactionRow(helper, o));
            helper.tableEnd();
        }
        if (withVat) {
            helper.header("VAT", 2);
            YearMonth currentYearMonth = YearMonth.from(from);
            while (!YearMonth.from(to).isBefore(currentYearMonth)) {
                addVatPeriod(helper, currentYearMonth);
                currentYearMonth = currentYearMonth.plusMonths(6);
            }
        }
    }

    private static void introduction(AsciiDocHelper helper, LocalDate from, LocalDate to) {
        helper.header("Introduction", 2);
        helper.paragraph("The closing report is a comprehensive report summarizing the financial situation of the company at the end of the year.");
        helper.paragraph("The report contains the balance sheet, the profit and loss statement.");
        helper.paragraph("The report is for _tangly llc, Lorzenhof 27, 6330 Cham, Switzerland_.");
        helper.paragraph("The company identifier is CHE-357.875.339.");
        helper.paragraph(
            "The reported period is from _%s_ to _%s_.".formatted(DateTimeFormatter.ISO_LOCAL_DATE.format(from), DateTimeFormatter.ISO_LOCAL_DATE.format(to)));
    }

    private static void addResultBalanceTableFor(AsciiDocHelper helper, List<Account> accounts, LocalDate from, LocalDate to, String category,
                                                 boolean withEmptyAccounts) {
        helper.header(category, 3);
        helper.tableHeader(null, "cols=\"20a, <100a, 25, >25, >25\"", "Account", "Description", "Kind", "Opening", "Balance");
        accounts.forEach(o -> addBalanceRow(helper, o, from, to, withEmptyAccounts));
        helper.tableEnd();
    }

    private static void addResultIncomeTableFor(AsciiDocHelper helper, List<Account> accounts, LocalDate from, LocalDate to, String category,
                                                boolean withEmptyAccounts) {
        helper.header(category, 3);
        helper.tableHeader(null, "cols=\"20a, <100a, 25, >25\"", "Account", "Description", "Kind", "Balance");
        accounts.forEach(o -> addIncomeRow(helper, o, from, to, withEmptyAccounts));
        helper.tableEnd();
    }

    /**
     * Creates the VAT results table row for a half-year. It is the period used by the Swiss government as a VAT payment period for small companies using the
     * net tax rate VAT variant.
     *
     * @param helper    AsciiDoc helper to write the report
     * @param yearMonth the year and month from which a half-year period is computed. The period is from January to June or from July to December
     */
    private void addVatPeriod(AsciiDocHelper helper, YearMonth yearMonth) {
        LocalDate periodStart;
        LocalDate periodEnd;
        boolean isFirstHalf = yearMonth.getMonth().getValue() < Month.JULY.getValue();
        if (isFirstHalf) {
            periodStart = LocalDate.of(yearMonth.getYear(), Month.JANUARY, 1);
            periodEnd = LocalDate.of(yearMonth.getYear(), Month.JUNE, 30);
        } else {
            periodStart = LocalDate.of(yearMonth.getYear(), Month.JULY, 1);
            periodEnd = LocalDate.of(yearMonth.getYear(), Month.DECEMBER, 31);
        }

        helper.header("VAT Period %d%s".formatted(yearMonth.getYear(), isFirstHalf ? "-H1" : "-H2"), 3);
        helper.tableHeader("VAT Period %d%s".formatted(yearMonth.getYear(), isFirstHalf ? "-H1" : "-H2"), "cols=\"50a, >25a, >25a , >25a\"",
            "VAT Code/Rate/DueRate", "Turnover", "VAT", "Due VAT");
        var vatCodes = registry.find(VatCode.class).orElseThrow();
        for (var vatCode : vatCodes.codes()) {
            BigDecimal turnover = realm.computeVatSales(periodStart, periodEnd, vatCode);
            if (BigDecimal.ZERO.equals(turnover)) {
                continue;
            }
            BigDecimal vat = realm.computeVat(periodStart, periodEnd, vatCode);
            BigDecimal vatDue = realm.computeDueVat(periodStart, periodEnd, vatCode);
            helper.tableRow("%s / %s / %s".formatted(vatCode.code(), BigDecimalUtilities.formatToPercentage(vatCode.vatRate()),
                BigDecimalUtilities.formatToPercentage(vatCode.vatDueRate())), format(turnover, false), format(vat, false), format(vatDue, false));
        }
        BigDecimal turnover = realm.computeVatSales(periodStart, periodEnd, null);
        BigDecimal vat = realm.computeVat(periodStart, periodEnd, null);
        BigDecimal vatDue = realm.computeDueVat(periodStart, periodEnd, null);
        helper.tableRow("*Total*", format(turnover, true), format(vat, true), format(vatDue, true));
        helper.tableEnd();
    }

    private static void addTransactionRow(AsciiDocHelper helper, Transaction transaction) {
        if (transaction.isSplit()) {
            if (transaction.hasDebitSplits()) {
                helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.text(), "", transaction.creditAccount(),
                    format(transaction.amount(), false), "-");
                for (AccountEntry entry : transaction.debitSplits()) {
                    helper.tableRow("", "", entry.text(), entry.accountId(), "", format(transaction.amount(), false), "-");
                }
            } else if (transaction.hasCreditSplits()) {
                helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.text(), transaction.debitAccount(), "",
                    format(transaction.amount(), false), "-");
                for (AccountEntry entry : transaction.debitSplits()) {
                    helper.tableRow("", "", "", entry.text(), entry.accountId(), format(transaction.amount(), false), "-");
                }
            }
        } else {
            helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.text(), transaction.debitAccount(), transaction.creditAccount(),
                format(transaction.amount(), false), vat(transaction.creditSplits().getFirst()));
        }
    }

    private static String vat(@NotNull AccountEntry entry) {
        return entry.getVat().map(o -> "%s%%".formatted(format(o.multiply(new BigDecimal(100)), false))).orElse("");
    }

    private static void addBalanceRow(@NotNull AsciiDocHelper helper, @NotNull Account account, @NotNull LocalDate from, @NotNull LocalDate to,
                                      boolean withEmptyAccounts) {
        var fromBalance = account.balance(from.minusDays(1));
        var toBalance = account.balance(to);
        if (!withEmptyAccounts && (BigDecimal.ZERO.equals(fromBalance) && BigDecimal.ZERO.equals(toBalance))) {
            return;
        }
        var accountId = account.id();
        var description = account.name();
        if (account.id().startsWith("E")) {
            accountId = AsciiDocHelper.bold(account.id());
            description = AsciiDocHelper.bold(account.name());
        } else if (account.isAggregate()) {
            accountId = AsciiDocHelper.italics(account.id());
            description = AsciiDocHelper.italics(account.name());
        }
        helper.tableRow(accountId, description, account.isAggregate() ? "" : account.kind().name().toLowerCase(), format(fromBalance, false),
            format(toBalance, false));
    }

    private static void addIncomeRow(@NotNull AsciiDocHelper helper, @NotNull Account account, @NotNull LocalDate from, @NotNull LocalDate to,
                                     boolean withEmptyAccounts) {
        var balance = account.balance(from, to);
        if (!withEmptyAccounts && (BigDecimal.ZERO.equals(balance))) {
            return;
        }
        var accountId = account.id();
        var description = account.name();
        if (account.id().startsWith("E")) {
            accountId = AsciiDocHelper.bold(account.id());
            description = AsciiDocHelper.bold(account.name());
        } else if (account.isAggregate()) {
            accountId = AsciiDocHelper.italics(account.id());
            description = AsciiDocHelper.italics(account.name());
        }
        helper.tableRow(accountId, description, account.isAggregate() ? "" : account.kind().name().toLowerCase(), format(balance, false));
    }
}
