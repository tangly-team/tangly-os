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

package net.tangly.erp.ledger.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import net.tangly.core.DateRange;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


/**
 * Regular CRUD view on transaction abstraction. The grid and edition dialog were optimized for usability.
 * The transaction view displays only non-synthetic transactions. Synthetic transactions are used to compute flat rate VAT.
 */
@PageTitle("ledger-transactions")
class TransactionsView extends ItemView<Transaction> {
    static class TransactionFilter extends ItemFilter<Transaction> {
        private DateRange dateRange;
        private String text;
        private String debit;
        private String credit;
        private BigDecimal amount;
        private Boolean synthetic;

        public TransactionFilter() {
        }

        public void dateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            dataView().refreshAll();
        }

        public void text(String text) {
            this.text = text;
            dataView().refreshAll();
        }

        public void debit(String debit) {
            this.debit = debit;
            dataView().refreshAll();
        }

        public void credit(String credit) {
            this.credit = credit;
            dataView().refreshAll();
        }

        public void synthetic(Boolean synthetic) {
            this.synthetic = synthetic;
            dataView().refreshAll();
        }

        public boolean test(@NotNull Transaction entity) {
            return (Objects.isNull(dateRange) || Objects.isNull(entity.date()) || dateRange.isActive(entity.date())) &&
                ItemFilter.matches(entity.text(), text) && checkAccountId(entity.credit(), credit) && checkAccountId(entity.debit(), debit) &&
                checkBoolean(entity.synthetic(), synthetic);
        }

        private boolean checkAccountId(AccountEntry entry, String accountId) {
            return Objects.isNull(entry) || ItemFilter.matches(entry.accountId(), accountId);
        }

        private boolean checkBoolean(boolean value, Boolean filter) {
            return Objects.isNull(filter) || filter.equals(value);
        }
    }

    static class TransactionForm extends ItemForm<Transaction, TransactionsView> {
        TransactionForm(@NotNull TransactionsView view) {
            super(view);
            init();
        }

        private void init() {
            DatePicker date = VaadinUtils.createDatePicker("date");
            TextField text = VaadinUtils.createTextField("Text", "text");
            TextField reference = VaadinUtils.createTextField("Reference", "reference");
            ComboBox<String> debitAccount = new ComboBox<>("Debit");

            debitAccount.setItems(logic().bookableAccountIds());
            debitAccount.setClearButtonVisible(true);
            ComboBox<String> creditAccount = new ComboBox<>("Credit");
            creditAccount.setItems(logic().bookableAccountIds());
            creditAccount.setClearButtonVisible(true);
            TextField amount = VaadinUtils.createTextField(AMOUNT_LABEL, AMOUNT);

            binder().bindReadOnly(date, Transaction::date);
            binder().bindReadOnly(debitAccount, Transaction::debitAccount);
            binder().bindReadOnly(creditAccount, Transaction::creditAccount);
            binder().bindReadOnly(amount, o -> o.amount().toString());
            binder().bindReadOnly(text, Transaction::text);
            binder().bindReadOnly(reference, Transaction::reference);
            // TODO debit and credit including splits
            FormLayout form = new FormLayout();
            form.add(date, reference, text, debitAccount, creditAccount, amount);
            addTabAt("details", form, 0);
        }

        @Override
        public void value(Transaction value) {
            if (value != null) {
                binder().readBean(value);
            }
        }

        @Override
        protected Transaction createOrUpdateInstance(Transaction entity) {
            Transaction transaction;
            if (entity == null) {
                LocalDate date = (LocalDate) binder().getBinding("date").orElseThrow().getField().getValue();
                String debitAccount = (String) binder().getBinding("debitAccount").orElseThrow().getField().getValue();
                String creditAccount = (String) binder().getBinding("creditAccount").orElseThrow().getField().getValue();
                // handle BigDecimal amount
                String text = (String) binder().getBinding("text").orElseThrow().getField().getValue();
                String reference = (String) binder().getBinding("reference").orElseThrow().getField().getValue();
                transaction = new Transaction(date, debitAccount, creditAccount, null, text, reference);
            } else {
                transaction = entity;
            }
            return transaction;
        }

        LedgerBusinessLogic logic() {
            return view().domain().logic();
        }
    }

    public TransactionsView(@NotNull LedgerBoundedDomainUi domain, @NotNull Mode mode) {
        super(Transaction.class, domain, ProviderView.of(domain.domain().realm().transactions(), o -> !o.synthetic()), new TransactionFilter(), mode);
        form(() -> new TransactionForm(this));
        init();
    }

    @Override
    public LedgerBoundedDomain domain() {
        return (LedgerBoundedDomain) super.domain();
    }

    @Override
    protected void addActions(@NotNull GridMenu<Transaction> menu) {
        menu().add(new Hr());
        menu().add(GridMenu.PRINT_TEXT, e -> Cmd.ofGlobalCmd(e, () -> new CmdCreateLedgerDocument(domain()).execute()), GridMenu.MenuItemType.GLOBAL);
    }

    @Override
    protected TransactionFilter filter() {
        return (TransactionFilter) super.filter();
    }

    private void init() {
        Grid<Transaction> grid = grid();
        grid.addColumn(Transaction::date).setKey(DATE).setHeader(DATE_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::reference).setKey("reference").setHeader("Reference").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Transaction::text).setKey(TEXT).setHeader(TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        VaadinUtils.addColumnBoolean(grid, Transaction::isSplit, "split", "Split");
        grid.addColumn(Transaction::debitAccount).setKey("debit").setHeader("Debit").setAutoWidth(true).setResizable(true);
        grid.addColumn(Transaction::creditAccount).setKey("credit").setHeader("Credit").setAutoWidth(true).setResizable(true);
        grid.addColumn(new NumberRenderer<>(Transaction::amount, VaadinUtils.FORMAT)).setKey(AMOUNT).setHeader(AMOUNT_LABEL).setComparator(Transaction::amount)
            .setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Transaction::vatCodeAsString).setKey("vatCode").setHeader("VatCode").setAutoWidth(true).setResizable(true).setSortable(true);
        addEntityFilterFields(grid(), filter());
        grid.setItemDetailsRenderer(createTransactionDetailsRenderer());
    }

    protected void addEntityFilterFields(@NotNull Grid<Transaction> grid, @NotNull TransactionFilter filter) {
        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();
        grid.getHeaderRows().clear();
        headerRow.getCell(grid.getColumnByKey(EntityView.DATE)).setComponent(ItemView.createDateRangeFilterField(filter::dateRange));
        headerRow.getCell(grid.getColumnByKey(EntityView.TEXT)).setComponent(ItemView.createTextFilterField(filter::text));
        headerRow.getCell(grid.getColumnByKey("debit")).setComponent(ItemView.createTextFilterField(filter::debit));
        headerRow.getCell(grid.getColumnByKey("credit")).setComponent(ItemView.createTextFilterField(filter::credit));
    }

    private static ComponentRenderer<TransactionDetails, Transaction> createTransactionDetailsRenderer() {
        return new ComponentRenderer<>(TransactionDetails::new, TransactionDetails::value);
    }

    private static class TransactionDetails extends FormLayout {
        private final Grid<AccountEntry> grid;

        public TransactionDetails() {
            grid = new Grid<>();
            grid.addColumn(AccountEntry::date).setKey(DATE).setHeader(DATE_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AccountEntry::reference).setKey("reference").setHeader("Reference").setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(AccountEntry::text).setKey(TEXT).setHeader(TEXT_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
            grid.addColumn(new NumberRenderer<>(AccountEntry::amount, VaadinUtils.FORMAT)).setKey(AMOUNT).setHeader(AMOUNT_LABEL)
                .setComparator(AccountEntry::amount).setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
            grid.addColumn(AccountEntry::vatCodeAsString).setKey("vatCode").setHeader("VatCode").setAutoWidth(true).setResizable(true).setSortable(true);
            add(grid);
            grid.setWidthFull();
        }

        public void value(@NotNull Transaction transaction) {
            grid.setItems(transaction.splits());
        }
    }
}
