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

package net.tangly.ledger.ports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.AccountEntry;
import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.bus.ledger.Transaction;
import net.tangly.commons.lang.Strings;
import net.tangly.commons.logger.EventData;
import net.tangly.core.Tag;
import net.tangly.ports.TsvHdl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ledger CSV handler can import ledger plans and transactions journal. The import assumes that the program language and ledger template use English.
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

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LedgerRealm ledger;

    @Inject
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
     *     <dt>Currency</dt><dd>Defines the currency of the accoun.t</dd>
     * </dl>
     *
     * @param path path to the file containing the chart of accounts
     * @see #exportChartOfAccounts(Path)
     */
    public void importChartOfAccounts(@NotNull Path path) {
        try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            int counter = 0;
            Account.AccountGroup currentSection = null;
            for (CSVRecord record : TsvHdl.FORMAT.parse(in)) {
                String section = record.get(SECTION);
                if (!Strings.isNullOrBlank(section)) {
                    currentSection = ofGroup(section);
                }
                String accountGroup = record.get(GROUP);
                String id = record.get(ACCOUNT);
                String text = record.get(DESCRIPTION);
                String accountKind = record.get(BCLASS);
                String ownedByGroupId = record.get(GR);
                String currency = record.get(CURRENCY);
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
                    EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.INFO, Account.class.getSimpleName() + " imported",
                        Map.of("filename", path, "object", account));

                }
                EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.INFO, Account.class.getSimpleName() + " imported objects",
                    Map.of("filename", path, "count", counter));
            }
        } catch (IOException e) {
            EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.FAILURE, "Entities not imported from", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Exports the chart of accounts to the provided file.
     *
     * @param path path to the file
     * @see #importChartOfAccounts(Path)
     */
    public void exportChartOfAccounts(@NotNull Path path) {
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
                EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.SUCCESS, Account.class.getSimpleName() + " exported to charter of accounts",
                    Map.of("filename", path, "entity", account));
            }
            EventData
                .log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.INFO, "exported to charter of accounts", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.FAILURE, "Accounts exported to", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Imports the transaction list exported from a CSV file with header.
     * <p>The file structure provided by the external program is:</p>
     * <dl>
     *     <dt>date</dt><dd>date of the transaction encoded as ISO standard YYYY-MM-DD.</dd>
     *     <dt>reference</dt><dd>optional reference to a document associated with the transaction.</dd>
     *     <dt>text</dt><dd>text deecribing the transaction.</dd>
     *     <dt>account debit</dt><dd>account debited. If empty indicates a split transaction.</dd>
     *     <dt>account credit</dt><dd>account credited. If empty indicates a split transaction.</dd>
     *     <dt>amount</dt><dd>amount of the transaction.</dd>
     *     <dt>VAT code</dt><dd>optional VAT code associated with the transaction or the split part of a transaction.</dd>
     * </dl>
     *
     * @param path path to the file containing the list of transactions
     * @see #exportJournal(Path, LocalDate, LocalDate)
     */
    public void importJournal(@NotNull Path path) {
        try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            int counter = 0;
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                String date = record.get(DATE);
                String reference = record.get(DOC);
                String text = record.get(DESCRIPTION);
                String debitAccount = record.get(ACCOUNT_DEBIT);
                String creditAccount = record.get(ACCOUNT_CREDIT);
                String[] debitValues = debitAccount.split("-");
                String[] creditValues = creditAccount.split("-");
                String amount = record.get(AMOUNT);
                String vatCode = record.get(VAT_CODE);
                String dateExpected = record.get(DATE_EXPECTED);
                Transaction transaction = null;
                if (isPartOfSplitTransaction(record)) {
                    List<AccountEntry> splits = new ArrayList<>();
                    record = importSplits(records, splits);
                    try {
                        transaction = new Transaction(LocalDate.parse(date), Strings.emptyToNull(debitValues[0]), Strings.emptyToNull(creditValues[0]),
                            new BigDecimal(amount), splits, text, reference);
                    } catch (NumberFormatException e) {
                        logger.atError().setCause(e).log("{}: not a legal amount {}", date, amount);
                    }
                } else {
                    try {
                        transaction = new Transaction(LocalDate.parse(date), Strings.emptyToNull(debitValues[0]), Strings.emptyToNull(creditValues[0]),
                            Strings.isNullOrEmpty(amount) ? BigDecimal.ZERO : new BigDecimal(amount), text, reference);
                        defineVat(transaction.creditSplits().get(0), vatCode);
                    } catch (NumberFormatException e) {
                        logger.atError().setCause(e).log("{}: not a legal amount {}", date, amount);
                    }
                    record = records.hasNext() ? records.next() : null;
                }
                if (transaction != null) {
                    defineProject(transaction.debitSplits(), debitAccount);
                    defineProject(transaction.creditSplits(), creditAccount);
                    ledger.addVat(transaction);
                    ++counter;
                    EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.SUCCESS, Transaction.class.getSimpleName() + " imported to journal",
                        Map.of("filename", path, "entity", transaction));
                }
            }
            EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.INFO, "imported to journal", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.FAILURE, "Transactions imported from", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Exports the transactions with a date in the given time interval.
     *
     * @param path path to the file containing the list of transactions
     * @param from start of the time interval of relevant transactions
     * @param to   end of the time interval of relevant transactions
     */
    public void exportJournal(@NotNull Path path, LocalDate from, LocalDate to) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), TsvHdl.FORMAT)) {
            out.printRecord(DATE, DOC, DESCRIPTION, ACCOUNT_DEBIT, ACCOUNT_CREDIT, AMOUNT, VAT_CODE, DATE_EXPECTED);
            int counter = 0;
            for (Transaction transaction : ledger.transactions(from, to)) {
                Optional<Tag> vat = transaction.creditSplits().get(0).findBy(AccountEntry.FINANCE, AccountEntry.VAT_FLAG);
                out.printRecord(transaction.date(), transaction.reference(), transaction.text(), transaction.debitAccount(), transaction.creditAccount(),
                    transaction.amount(), vat.isPresent() ? vat.get().value() : null, null);
                // TODO write export code for project
                // TODO write export code for expected date
                if (transaction.isSplit()) {
                    if (transaction.debitAccount() == null) {
                        for (AccountEntry entry : transaction.debitSplits()) {
                            vat = entry.findBy(AccountEntry.FINANCE, AccountEntry.VAT_FLAG);
                            out.printRecord(entry.date(), transaction.reference(), entry.text(), entry.accountId(), null, entry.amount(),
                                vat.isPresent() ? vat.get().value() : null, null);
                        }
                    } else if (transaction.creditAccount() == null) {
                        for (AccountEntry entry : transaction.creditSplits()) {
                            vat = entry.findBy(AccountEntry.FINANCE, AccountEntry.VAT_FLAG);
                            out.printRecord(entry.date(), transaction.reference(), entry.text(), null, entry.accountId(), entry.amount(),
                                vat.isPresent() ? vat.get().value() : null, null);
                        }
                    }
                }
                ++counter;
            }
            EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.INFO, "exported from journal", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CSVRecord importSplits(@NotNull Iterator<CSVRecord> records, List<AccountEntry> splits) {
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (isPartOfSplitTransaction(record)) {
            String date = record.get(DATE);
            String text = record.get(DESCRIPTION);
            String debitAccount = record.get(ACCOUNT_DEBIT);
            String creditAccount = record.get(ACCOUNT_CREDIT);
            String splitAmount = record.get(AMOUNT);
            String vatCode = record.get(VAT_CODE);
            AccountEntry entry = null;
            if (!Strings.isNullOrEmpty(debitAccount)) {
                String[] debitValues = debitAccount.split("-");
                entry = AccountEntry.debit(debitValues[0], date, splitAmount, text);
            } else if (!Strings.isNullOrEmpty(creditAccount)) {
                String[] creditValues = creditAccount.split("-");
                entry = AccountEntry.credit(creditValues[0], date, splitAmount, text);
            }
            defineVat(entry, vatCode);
            splits.add(entry);
            record = records.hasNext() ? records.next() : null;
        }
        return record;
    }

    /**
     * Returns true if the record is relevant for the ledger plan, meaning it has a description and either an account identifier not starting with an semicolon
     * or a group with an identifier different from 0.
     *
     * @return flag indicating if hte record is relevant for the ledger plan or not
     */
    private static boolean isRecordPlanRelevant(String description, String accountId, String groupId) {
        return !Strings.isNullOrEmpty(description) &&
            ((!Strings.isNullOrEmpty(accountId) && !accountId.startsWith(":")) || (!Strings.isNullOrEmpty(groupId) && !groupId.equalsIgnoreCase("0")));
    }

    // potential strategy pattern
    private static final String F1 = "F1";
    private static final String F3 = "F3";
    private static final BigDecimal VAT_F1_VALUE = new BigDecimal("0.08");
    private static final BigDecimal VAT_F3_VALUE = new BigDecimal("0.077");
    private static final BigDecimal VAT_F1_DUE_VALUE = new BigDecimal("0.061");
    private static final BigDecimal VAT_F3_DUE_VALUE = new BigDecimal("0.065");

    private static void defineVat(@NotNull AccountEntry entry, String code) {
        // TODO wrong VAT codes are per credit account entry
        if (!Strings.isNullOrEmpty(code)) {
            switch (code) {
                case F1 -> {
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT, VAT_F1_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_DUE, VAT_F1_DUE_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_FLAG, F1));
                }
                case F3 -> {
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT, VAT_F3_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_DUE, VAT_F3_DUE_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_FLAG, F3));
                }
                default -> logger.info("Unknown VAT code in CSV file {}", code);
            }
        }
    }

    private static void defineProject(@NotNull List<AccountEntry> entries, String code) {
        if (!Strings.isNullOrEmpty(code)) {
            String[] values = code.split("-");
            if (values.length > 1) {
                entries.forEach(o -> o.add(new Tag(AccountEntry.FINANCE, AccountEntry.PROJECT, values[1])));
            }
        }
    }

    private static Account.AccountGroup ofGroup(String accountGroup) {
        Account.AccountGroup group = null;
        if (!accountGroup.contains("*")) {
            try {
                group = switch (Integer.parseInt(accountGroup)) {
                    case 1 -> Account.AccountGroup.ASSETS;
                    case 2 -> Account.AccountGroup.LIABILITIES;
                    case 3 -> Account.AccountGroup.EXPENSES;
                    case 4 -> Account.AccountGroup.PROFITS_AND_LOSSES;
                    default -> null;
                };
            } catch (NumberFormatException e) {
                logger.atError().setCause(e).log("Format error for account group {}", accountGroup);
            }
        }
        return group;
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
            logger.atError().setCause(e).log("Format error for account kind {}", accountKind);
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
            case ASSETS:
                out.printRecord("*", null, null, "BALANCE SHEET", null, null, null);
                out.println();
                out.printRecord(ofGroup(group), null, null, "ASSETS", null, null, null);
                out.println();
                break;
            case LIABILITIES:
                out.println();
                out.printRecord(ofGroup(group), null, null, "LIABILITIES", null, null, null);
                out.println();
                break;
            case PROFITS_AND_LOSSES:
                out.println();
                out.printRecord("*", null, null, "PROFIT & LOSS STATEMENT", null, null, null);
                out.printRecord(ofGroup(group), null, null, null, null, null, null);
                break;
        }
    }

    private static boolean isPartOfSplitTransaction(CSVRecord record) {
        return (record != null) && (Strings.isNullOrEmpty(record.get(ACCOUNT_DEBIT)) || Strings.isNullOrEmpty(record.get(ACCOUNT_CREDIT)));
    }
}
