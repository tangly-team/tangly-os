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

package net.tangly.erp.ledger;

import com.google.common.base.Strings;
import net.tangly.commons.models.Tag;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The ledger CSV handler can import ledger plans and transactions journal as exported by the banana 8 ledger application. The import assumes that
 * the program language and ledger template use English.
 * The ledger structure CSV file has the columns id, account kind, account group, description, owned by group id. The handler reads a ledger
 * structure description from a CSV file and create a full ledger structure.
 * The transaction CSV file has the columns date, doc, description, account debit, account credit, amount, vat code.
 */
public class LedgerCsvHdl {
    public static final String F1 = "F1";
    public static final String F3 = "F3";
    public static final String FINANCE = "fin";
    public static final String PROJECT = "project";
    public static final String VAT = "vat";

    private Ledger ledger;

    /**
     * Constructor of the class.
     *
     * @param ledger ledger to use when importing data from CSV files.
     */
    public LedgerCsvHdl(Ledger ledger) {
        this.ledger = ledger;
    }

    /**
     * Returns the ledger updated through the handler.
     *
     * @return the ledger used by the handler
     */
    public Ledger ledger() {
        return ledger;
    }

    public void importStructureLedgerFromBanana8(Path path) throws IOException {
        Reader in = new BufferedReader(new FileReader(path.toFile()));
        Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
        Account.AccountGroup currentSection = null;
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (record != null) {
            String section = record.get("Section");
            if ((section != null) && !section.isEmpty()) {
                currentSection = ofGroup(section);
            }
            String accountGroup = record.get("Group");
            String id = record.get("Account");
            String text = record.get("Description");
            String accountKind = record.get("BClass");
            String ownedByGroupId = record.get("Gr");
            if (isRecordPlanRelevant(text, id, accountGroup)) {
                // the line is relevant for the ledger plan
                if ((accountGroup != null) && (!accountGroup.isEmpty())) {
                    // the record defines a group account
                    of(id, currentSection, text, ownedByGroupId);
                } else {
                    // the record defines a regular account
                    of(Integer.parseInt(id), ofKInd(accountKind), text, ownedByGroupId);
                }
            }
            record = records.hasNext() ? records.next() : null;
        }
    }

    /**
     * Imports the transaction list exported from banana 8 software as CSV file with header.
     *
     * @param path path to the file containing the list of transactions
     * @throws IOException if file operations encountered a problem - no file or no privileges -
     */
    public void importTransactionsLedgerFromBanana8(Path path) throws IOException {
        Reader in = new BufferedReader(new FileReader(path.toFile()));
        Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (record != null) {
            String date = record.get("Date");
            String reference = record.get("Doc");
            String text = record.get("Description");
            if (isPartOfSplitTransaction(record)) {
                List<AccountEntry> debitSplits = new ArrayList<>();
                List<AccountEntry> creditSplits = new ArrayList<>();
                while (isPartOfSplitTransaction(record)) {
                    String creditAccount = record.get("AccountDebit");
                    String debitAccount = record.get("AccountCredit");
                    String amount = record.get("Amount");
                    String vatCode = record.get("VatCode");
                    if (!debitAccount.isEmpty()) {
                        String[] debitValues = debitAccount.split("-");
                        AccountEntry entry = AccountEntry.debit(debitValues[0], date, amount, text);
                        if (debitValues.length > 1) {
                            entry.add(new Tag(FINANCE, "project", debitValues[1]));
                        }
                        debitSplits.add(entry);
                    } else if (!creditAccount.isEmpty()) {
                        String[] creditValues = creditAccount.split("-");
                        AccountEntry entry = AccountEntry.credit(creditValues[0], date, amount, text);
                        if (creditValues.length > 1) {
                            entry.add(new Tag(FINANCE, "project", creditValues[1]));
                        }
                        creditSplits.add(entry);
                    }
                    record = records.hasNext() ? records.next() : null;
                    // TODO handle VAT code F1 = 8%, F3 = 7.7%
                    if (record == null) {
                        break;
                    }
                }
                ledger.add(new Transaction(LocalDate.parse(date), debitSplits, creditSplits, text, reference));
            } else {
                String[] creditValues = record.get("AccountDebit").split("-");
                String[] debitValues = record.get("AccountCredit").split("-");
                // ACC:customer=<customer name (HSLU, ARE, ADCUBUM, DINN
                String amount = record.get("Amount");
                amount = amount.isEmpty() ? "0" : amount;
                String vatCode = record.get("VatCode");
                try {
                    add(date, Integer.parseInt(debitValues[0]), Integer.parseInt(creditValues[0]), amount, text, reference, vatCode);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                record = records.hasNext() ? records.next() : null;
            }
        }
    }

    /**
     * Returns true if the record is relevant for the ledger plan, meaning it has a description and either an account identifier not starting with
     * an semicolon or a group with an identifier different from 0.
     *
     * @return flag indicating if hte record is relevant for the ledger plan or not
     */
    private boolean isRecordPlanRelevant(String description, String accountId, String groupId) {
        return !Strings.isNullOrEmpty(description) && ((!Strings.isNullOrEmpty(accountId) && !accountId.startsWith(":")) || (!Strings
                .isNullOrEmpty(groupId) && !groupId.equalsIgnoreCase("0")));
    }

    private Account.AccountGroup ofGroup(String accountGroup) {
        Account.AccountGroup group;
        try {
            switch (Integer.parseInt(accountGroup)) {
                case 1:
                    group = Account.AccountGroup.ASSETS;
                    break;
                case 2:
                    group = Account.AccountGroup.LIABILITIES;
                    break;
                case 3:
                    group = Account.AccountGroup.EXPENSES;
                    break;
                case 4:
                    group = Account.AccountGroup.PROFITS_AND_LOSSES;
                    break;
                default:
                    group = null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return group;
    }

    private Account.AccountKind ofKInd(String accountKind) {
        Account.AccountKind kind;
        try {
            switch (Integer.parseInt(accountKind)) {
                case 1:
                    kind = Account.AccountKind.ASSET;
                    break;
                case 2:
                    kind = Account.AccountKind.LIABILITY;
                    break;
                case 3:
                    kind = Account.AccountKind.EXPENSE;
                    break;
                case 4:
                    kind = Account.AccountKind.INCOME;
                    break;
                default:
                    kind = null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return kind;
    }

    private void of(int id, Account.AccountKind kind, String description, String ownedByGroupId) {
        ledger.add(Account.of(id, kind, description, ownedByGroupId));
    }

    private void of(String id, Account.AccountGroup group, String description, String ownedByGroupId) {
        ledger.add(Account.of(id, group, description, ownedByGroupId));
    }


    private void add(String date, int debit, int credit, String amount, String text) {
        ledger.add(Transaction.of(date, debit, credit, amount, text));
    }

    private void add(String date, int debit, int credit, String amount, String text, String reference) {
        ledger.add(Transaction.of(date, debit, credit, amount, text, reference));
    }

    private void add(String date, int debit, int credit, String amount, String text, String reference, String vatCode) {
        ledger.add(Transaction.of(date, debit, credit, amount, text, reference, vatCode));
    }

    private void add(String date, List<Integer> splitDebitAccounts, List<String> splitDebitAmounts, List<Integer> splitCreditAccounts, List<String> splitCreditAmounts, String text, String reference) {
        ledger.add(Transaction.of(date, splitDebitAccounts, splitDebitAmounts, splitCreditAccounts, splitCreditAmounts, text, reference));
    }

    private boolean isPartOfSplitTransaction(CSVRecord record) {
        return (record.get("AccountDebit").isEmpty()) || (record.get("AccountCredit").isEmpty());
    }
}
