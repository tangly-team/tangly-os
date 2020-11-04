package net.tangly.commons.ledger.ui;

import java.time.LocalDate;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import net.tangly.bus.ledger.LedgerBusinessLogic;
import net.tangly.commons.vaadin.Crud;
import net.tangly.commons.vaadin.TabsComponent;
import org.jetbrains.annotations.NotNull;

public class LedgerView extends VerticalLayout {
    private final TabsComponent tabs;
    private final AccountsView accountsView;
    private final TransactionsView transactionsView;
    private final LedgerBusinessLogic ledgerLogic;
    private LocalDate from;
    private LocalDate to;

    public LedgerView(@NotNull LedgerBusinessLogic ledgerLogic, @NotNull Crud.Mode mode) {
        this.ledgerLogic = ledgerLogic;
        from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        to = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        DatePicker fromDate = new DatePicker("From Date");
        fromDate.setValue(from);
        fromDate.addValueChangeListener(e -> {
            from = e.getValue();
            updateInterval();
        });

        DatePicker toDate = new DatePicker("To Date");
        toDate.setValue(to);
        toDate.addValueChangeListener(e -> {
            to = e.getValue();
            updateInterval();
        });

        accountsView = new AccountsView(ledgerLogic, mode);
        transactionsView = new TransactionsView(ledgerLogic, mode);

        tabs = new TabsComponent();
        setSizeFull();
        Div div = new Div();
        div.setText("Contracts");
        tabs.add(new Tab("Accounts"), accountsView);
        Tab transactions = new Tab("Transactions");
        tabs.add(transactions, transactionsView);
        tabs.initialize(transactions);
        addAndExpand(new HorizontalLayout(fromDate, toDate), tabs);
    }

    void updateInterval() {
        accountsView.interval(from, to);
        transactionsView.interval(from, to);
    }
}
