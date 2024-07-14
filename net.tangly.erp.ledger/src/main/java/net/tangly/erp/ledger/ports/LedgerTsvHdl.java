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

package net.tangly.erp.ledger.ports;

import net.tangly.commons.lang.Dates;
import net.tangly.commons.lang.Strings;
import net.tangly.commons.logger.EventData;
import net.tangly.core.Tag;
import net.tangly.core.domain.DomainAudit;
import net.tangly.core.domain.TsvHdl;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerRealm;
import net.tangly.erp.ledger.services.VatCode;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * The ledger CSV handler can import ledger plans and journal of transactions. The import assumes that the program language and ledger template use English.
 * <p>The ledger structure CSV file has the columns id, account kind, account group, description, owned by group id. The handler reads a ledger structure
 * description from a CSV file and update a full ledger structure. </p>
 * <p>The transaction CSV file has the columns date, doc, description, account debit, account credit, amount, defineVat code, and date expected.</p>
 * <p>The accounting program used to generate the export files is <a href="https://www.banana.ch/">banana</a></p>
 */
public class LedgerTsvHdl {
    private static final String AMOUNT = "Amount";
    private static final String SECTION = "Section";
    private static final String GROUP = "Group";
    private static final String ACCOUNT = "Account";
    private static final String DATE = "Date";
    private static final String DESCRIPTION = "Description";
    private static final String DOC = "Doc";
    private static final String BCLASS = "BClass";
    private static final String GR = "Gr";
    private static final String CURRENCY = "Currency";
    private static final String ACCOUNT_DEBIT = "AccountDebit";
    private static final String ACCOUNT_CREDIT = "AccountCredit";
    private static final String VAT_CODE = "VatCode";
    private static final String DATE_EXPECTED = "DateExpected";

    private static final String SOURCE = "source";

    private static final Logger logger = LogManager.getLogger();
    private final LedgerRealm ledger;

    public LedgerTsvHdl(@NotNull LedgerRealm ledger) {
        this.ledger = ledger;
    }

    public LedgerRealm ledger() {
        return ledger;
    }

    /**
     * The file structure provided by the external program is:
     * <dl>
     *     <dt>Section</dt><dd>Defines the section the account is belonging to: 1 for assets, 2 for liabilities, 4 for profits and losses. A star indicates a
     *     separation line with a title in the document.</dd>
     *     <dt>Group</dt><dd>if the line has a group value, it is an aggregated account and therefore has no account identifier.</dd>
     *     <dt>Account</dt><dd>if the line has a account value, it is an account and the value is the account identifier. It has no group value.</dd>
     *     <dt>Description</dt><dd>the description is the human readable name of the account or aggregated account.</dd>
     *     <dt>BClass</dt><dd>Defines the type of account: 1 for assets, 2 for liabilities, 3 for expenses, and 4 for incomes.</dd>
     *     <dt>Gr</dt><dd>Defines the aggregate account owning the account.</dd>
     *     <dt>Currency</dt><dd>Defines the currency of the account.</dd>
     * </dl>
     *
     * @param path path to the file containing the chart of accounts
     * @see #exportChartOfAccounts(DomainAudit, Path)
     */
    public void importChartOfAccounts(@NotNull DomainAudit audit, @NotNull Path path) {
        try {
            int counter = 0;
            Account.AccountGroup currentSection = null;
            for (CSVRecord csv : TsvHdl.FORMAT.parse(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
                String section = csv.get(SECTION);
                if (!Strings.isNullOrBlank(section)) {
                    currentSection = ofGroup(section);
                }
                String accountGroup = csv.get(GROUP);
                String id = csv.get(ACCOUNT);
                String text = csv.get(DESCRIPTION);
                String accountKind = csv.get(BCLASS);
                String ownedByGroupId = csv.get(GR);
                String currency = csv.get(CURRENCY);
                if (Strings.isNullOrBlank(currency)) {
                    currency = "CHF";
                }
                if (isRecordPlanRelevant(text, id, accountGroup)) {
                    Account account;
                    if (Strings.isNullOrEmpty(accountGroup)) {
                        account = Account.of(Integer.parseInt(id), ofKind(accountKind), currency, text, ownedByGroupId);
                    } else {
                        account = Account.of(accountGroup, currentSection, currency, text, ownedByGroupId);
                    }
                    ledger.add(account);
                    ++counter;
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, "%s imported".formatted(Account.class.getSimpleName()),
                        Map.of(SOURCE, path, "object", account));

                }
                audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, "%s imported objects".formatted(Account.class.getSimpleName()),
                    Map.of(SOURCE, path, "count", counter));
            }
        } catch (IOException e) {
            audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Entities not imported from", Map.of(SOURCE, path), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Exports the chart of accounts to the provided file.
     *
     * @param path path to the file
     * @see #importChartOfAccounts(DomainAudit, Path)
     */
    public void exportChartOfAccounts(@NotNull DomainAudit audit, Path path) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), TsvHdl.FORMAT)) {
            out.printRecord(SECTION, GROUP, ACCOUNT, DESCRIPTION, BCLASS, GR, CURRENCY);
            int counter = 0;
            Account.AccountGroup group = null;
            for (Account account : ledger.accounts().items()) {
                if ((account.group() != null) && (account.group() != group)) {
                    group = account.group();
                    writeSection(group, out);
                }
                out.print(null);
                if (account.isAggregate()) {
                    out.print(account.id());
                    out.print(null);
                } else {
                    out.print(null);
                    out.print(account.id());
                }
                out.print(account.name());
                out.print(ofKind(account.kind()));
                out.print(account.ownedBy());
                out.print(account.isAggregate() ? null : account.currency().getCurrencyCode());
                out.println();
                if (account.isAggregate()) {
                    out.println();
                }
                ++counter;
                audit.log(EventData.EXPORT_EVENT, EventData.Status.SUCCESS,
                    "%s exported to charter of accounts".formatted(Account.class.getSimpleName()), Map.of(SOURCE, path, "entity", account));
            }
            audit.log(EventData.EXPORT_EVENT, EventData.Status.INFO, "exported to charter of accounts",
                Map.of(SOURCE, path, "counter", counter));
        } catch (IOException e) {
            audit.log(EventData.EXPORT_EVENT, EventData.Status.FAILURE, "Accounts exported to", Map.of(SOURCE, path), e);

            throw new UncheckedIOException(e);
        }
    }

    /**
     * Imports the transaction list exported from a CSV file with header.
     * <p>The file structure provided by the external program is:</p>
     * <dl>
     *     <dt>date</dt><dd>date of the transaction encoded as ISO standard YYYY-MM-DD.</dd>
     *     <dt>reference</dt><dd>optional reference to a document associated with the transaction.</dd>
     *     <dt>text</dt><dd>text describing the transaction.</dd>
     *     <dt>account debit</dt><dd>account debited. If empty indicates a split transaction.</dd>
     *     <dt>account credit</dt><dd>account credited. If empty indicates a split transaction.</dd>
     *     <dt>amount</dt><dd>amount of the transaction.</dd>
     *     <dt>VAT code</dt><dd>optional VAT code associated with the transaction or the split part of a transaction.</dd>
     * </dl>
     *
     * @param reader reader to the file containing the chart of accounts
     * @param source name of the source for the chart of accounts
     * @see #exportJournal(DomainAudit, Path, LocalDate, LocalDate)
     */
    public void importJournal(@NotNull DomainAudit audit, @NotNull Reader reader, String source) {
        try {
            Iterator<CSVRecord> records = TsvHdl.FORMAT.parse(reader).iterator();
            int counter = 0;
            var csv = TsvHdl.nextNonEmptyRecord(records);
            while (csv != null) {
                String date = csv.get(DATE);
                String[] debitValues = csv.get(ACCOUNT_DEBIT).split("-");
                String[] creditValues = csv.get(ACCOUNT_CREDIT).split("-");
                BigDecimal amount = TsvHdl.parseBigDecimal(csv, AMOUNT);
                String dateExpected = csv.get(DATE_EXPECTED);
                String text = csv.get(DESCRIPTION);
                String reference = csv.get(DOC);
                String vatCode = csv.get(VAT_CODE);
                List<AccountEntry> splits = new ArrayList<>();
                if (isSplit(csv)) {
                    csv = importSplits(records, splits, isDebitSplit(csv));
                } else {
                    csv = TsvHdl.nextNonEmptyRecord(records);
                }
                try {
                    var debitAccount = Strings.emptyToNull(debitValues[0]);
                    var creditAccount = Strings.emptyToNull(creditValues[0]);
                    var debit = (debitAccount != null) ?
                        new AccountEntry(debitAccount, Dates.of(date), amount, null, null, true, VatCode.of(vatCode), defineSegments(debitValues)) :
                        null;
                    var credit = (creditAccount != null) ?
                        new AccountEntry(creditAccount, Dates.of(date), amount, null, null, false, VatCode.of(vatCode), defineSegments(creditValues)) :
                        null;
                    Transaction transaction =
                        Transaction.of(Dates.of(date), reference, text, debit, credit, VatCode.of(vatCode), Dates.of(dateExpected), splits);
                    ledger.book(transaction);
                    ++counter;
                    audit.log(EventData.IMPORT_EVENT, EventData.Status.SUCCESS, "%s imported to journal".formatted(Transaction.class.getSimpleName()),
                        Map.of(SOURCE, source, "entity", transaction));
                } catch (NumberFormatException | DateTimeParseException e) {
                    logger.atError().withThrowable(e).log("{}: not a legal amount {}", date, amount);
                }
            }
            audit.log(EventData.IMPORT_EVENT, EventData.Status.INFO, "imported to journal", Map.of(SOURCE, source, "counter", counter));
        } catch (IOException e) {
            audit.log(EventData.IMPORT_EVENT, EventData.Status.FAILURE, "Transactions imported from", Map.of(SOURCE, source), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Exports the transactions with a date in the given time interval.
     *
     * @param path path to the file containing the list of transactions
     * @param from start of the time interval for the relevant transactions
     * @param to   end of the time interval for the relevant transactions
     */
    public void exportJournal(@NotNull DomainAudit audit, @NotNull Path path, LocalDate from, LocalDate to) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), TsvHdl.FORMAT)) {
            out.printRecord(DATE, DOC, DESCRIPTION, ACCOUNT_DEBIT, ACCOUNT_CREDIT, AMOUNT, VAT_CODE, DATE_EXPECTED);
            int counter = 0;
            for (Transaction transaction : ledger.transactions(from, to)) {
                if (!transaction.isSynthetic()) {
                    out.printRecord(transaction.date(), transaction.reference(), transaction.text(), accountCompositeId(transaction.debit()),
                        accountCompositeId(transaction.credit()), transaction.amount(), transaction.vatCodeAsString(), transaction.dateExpected());
                    if (transaction.isSplit()) {
                        for (AccountEntry entry : transaction.debitSplits()) {
                            out.printRecord(entry.date(), entry.reference(), entry.text(), entry.isDebit() ? accountCompositeId(entry) : null,
                                entry.isCredit() ? accountCompositeId(entry) : null, entry.amount(), entry.vatCodeAsString(), null);
                        }
                    }
                    ++counter;
                    audit.log(EventData.EXPORT_EVENT, EventData.Status.INFO, "exported from journal", Map.of(SOURCE, path, "counter", counter));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CSVRecord importSplits(@NotNull Iterator<CSVRecord> records, List<AccountEntry> splits, boolean isDebitSplit) {
        var csv = records.hasNext() ? records.next() : null;
        while ((isDebitSplit && isCreditSplit(csv)) || isDebitSplit(csv)) {
            if (!Strings.isNullOrBlank(csv.get(ACCOUNT_DEBIT))) {
                var values = csv.get(ACCOUNT_DEBIT).split("-");
                var entry = AccountEntry.debit(values[0], TsvHdl.parseDate(csv, DATE), TsvHdl.parseBigDecimal(csv, AMOUNT), csv.get(DOC), csv.get(DESCRIPTION),
                    VatCode.of(csv.get(VAT_CODE)), defineSegments(values));
                splits.add(entry);
            } else if (!Strings.isNullOrBlank(csv.get(ACCOUNT_CREDIT))) {
                var values = csv.get(ACCOUNT_CREDIT).split("-");
                var entry = AccountEntry.credit(values[0], TsvHdl.parseDate(csv, DATE), TsvHdl.parseBigDecimal(csv, AMOUNT), csv.get(DOC), csv.get(DESCRIPTION),
                    VatCode.of(csv.get(VAT_CODE)), defineSegments(values));
                splits.add(entry);
            }
            csv = TsvHdl.nextNonEmptyRecord(records);
        }
        return csv;
    }

    /**
     * Return true if the record is relevant for the ledger plan, meaning it has a description and either an account identifier not starting with an semicolon
     * or a group with an identifier different from 0.
     *
     * @return flag indicating if hte record is relevant for the ledger plan or not
     */
    private static boolean isRecordPlanRelevant(String description, String accountId, String groupId) {
        return !Strings.isNullOrEmpty(description) &&
            ((!Strings.isNullOrEmpty(accountId) && !accountId.startsWith(":")) || (!Strings.isNullOrEmpty(groupId) && !groupId.equalsIgnoreCase("0")));
    }

    private static Set<Tag> defineSegments(String[] values) {
        Set<Tag> tags = new HashSet<>();
        if (values.length > 1) {
            tags.add(new Tag(AccountEntry.FINANCE, AccountEntry.PROJECT, values[1]));
        }
        if (values.length > 2) {
            tags.add(new Tag(AccountEntry.FINANCE, AccountEntry.SEGMENT, values[2]));
        }
        return tags;
    }

    private static String accountCompositeId(AccountEntry entry) {
        if (entry != null) {
            Optional<Tag> project = entry.findBy(AccountEntry.FINANCE, AccountEntry.PROJECT);
            Optional<Tag> segment = entry.findBy(AccountEntry.FINANCE, AccountEntry.SEGMENT);
            return "%s%s%s".formatted(entry.accountId(), project.map(tag -> "-%s".formatted(tag.value())).orElse(""),
                segment.map(value -> "-" + value.value()).orElse(""));
        } else {
            return null;
        }
    }

    private static Account.AccountGroup ofGroup(String accountGroup) {
        if (!accountGroup.contains("*")) {
            try {
                return switch (Integer.parseInt(accountGroup)) {
                    case 1 -> Account.AccountGroup.ASSETS;
                    case 2 -> Account.AccountGroup.LIABILITIES;
                    case 3 -> Account.AccountGroup.EXPENSES;
                    case 4 -> Account.AccountGroup.PROFITS_AND_LOSSES;
                    default -> null;
                };
            } catch (NumberFormatException e) {
                logger.atError().withThrowable(e).log("Format error for account group {}", accountGroup);
            }
        }
        return null;
    }

    private static String ofGroup(Account.AccountGroup group) {
        return switch (group) {
            case ASSETS -> "1";
            case LIABILITIES -> "2";
            case EXPENSES -> "3";
            case PROFITS_AND_LOSSES -> "4";
        };
    }

    private static Account.AccountKind ofKind(String accountKind) {
        try {
            return Strings.isNullOrBlank(accountKind) ? Account.AccountKind.AGGREGATE : switch (Integer.parseInt(accountKind)) {
                case 1 -> Account.AccountKind.ASSET;
                case 2 -> Account.AccountKind.LIABILITY;
                case 3 -> Account.AccountKind.EXPENSE;
                case 4 -> Account.AccountKind.INCOME;
                default -> Account.AccountKind.AGGREGATE;
            };
        } catch (NumberFormatException e) {
            logger.atError().withThrowable(e).log("Format error for account kind {}", accountKind);
            return null;
        }
    }

    private static String ofKind(Account.AccountKind kind) {
        return switch (kind) {
            case ASSET -> "1";
            case LIABILITY -> "2";
            case EXPENSE -> "3";
            case INCOME -> "4";
            case AGGREGATE -> null;
        };
    }

    private static void writeSection(Account.AccountGroup group, CSVPrinter out) throws IOException {
        switch (group) {
            case ASSETS -> {
                out.printRecord("*", null, null, "BALANCE SHEET", null, null, null);
                out.println();
                out.printRecord(ofGroup(group), null, null, "ASSETS", null, null, null);
                out.println();
            }
            case LIABILITIES -> {
                out.println();
                out.printRecord(ofGroup(group), null, null, "LIABILITIES", null, null, null);
                out.println();
            }
            case PROFITS_AND_LOSSES -> {
                out.println();
                out.printRecord("*", null, null, "PROFIT & LOSS STATEMENT", null, null, null);
                out.printRecord(ofGroup(group), null, null, null, null, null, null);
            }
        }
    }

    private static boolean isSplit(CSVRecord csv) {
        return isDebitSplit(csv) || isCreditSplit(csv);
    }

    private static boolean isDebitSplit(CSVRecord csv) {
        return (csv != null) && !Strings.isNullOrEmpty(csv.get(ACCOUNT_DEBIT)) && Strings.isNullOrEmpty(csv.get(ACCOUNT_CREDIT));
    }

    private static boolean isCreditSplit(CSVRecord csv) {
        return (csv != null) && Strings.isNullOrEmpty(csv.get(ACCOUNT_DEBIT)) && !Strings.isNullOrEmpty(csv.get(ACCOUNT_DEBIT));
    }
}
