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

import com.storedobject.chart.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesPort;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.services.LedgerPort;
import net.tangly.ui.app.domain.AnalyticsView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.tangly.ui.components.ItemView.*;

public class AnalyticsLedgerView extends AnalyticsView {
    private static final String ProfitAndLoss = "Profit & Loss";
    private static final String Assets = "Assets";
    private static final String Reconciliation = "Reconciliation";
    private final LedgerBoundedDomainUi domain;
    private SOChart profitAndLossSoChart;
    private SOChart financialSoChart;
    private Grid<Transaction> transactionsGrid;
    private Grid<LedgerPort.Segment> segmentsGrid;
    private Grid<LedgerPort.Segment> projectsGrid;
    private final Map<String, InvoicesPort.InvoiceView> invoiceViewsCache;

    public AnalyticsLedgerView(@NotNull LedgerBoundedDomainUi domain) {
        this.domain = domain;
        invoiceViewsCache = new HashMap<>();
        init();
    }

    private void init() {
        profitAndLossSoChart = createAndRegisterChart(ProfitAndLoss);
        financialSoChart = createAndRegisterChart(Assets);
        transactionsGrid = transactionsTable();
        var layout = new HorizontalLayout(transactionsGrid);
        layout.setSizeFull();
        tabSheet().add(Reconciliation, layout);
        segmentsGrid = segmentsTable(domain.domain().port().computeSegments(AccountEntry.SEGMENT, from(), to()));
        layout = new HorizontalLayout(segmentsGrid);
        layout.setSizeFull();
        tabSheet().add("Segments", layout);
        projectsGrid = segmentsTable(domain.domain().port().computeSegments(AccountEntry.PROJECT, from(), to()));
        layout = new HorizontalLayout(projectsGrid);
        layout.setSizeFull();
        tabSheet().add("Projects", layout);
    }

    @Override
    public void refresh() {
        refresh(profitAndLossSoChart, this::profitAndLossChart);
        refresh(financialSoChart, this::financialsChart);
        if (Objects.nonNull(transactionsGrid)) {
            transactionsGrid.setDataProvider(DataProvider.ofCollection(
                ProviderView.of(ProviderInMemory.of(domain.domain().realm().transactions(from(), to())), o -> !o.synthetic() && o.vatCode().isPresent())
                    .items()));
            transactionsGrid.getDataProvider().refreshAll();
        }
        if (Objects.nonNull(segmentsGrid)) {
            segmentsGrid.setDataProvider(DataProvider.ofCollection(domain.domain().port().computeSegments(AccountEntry.SEGMENT, from(), to())));
            segmentsGrid.getDataProvider().refreshAll();
        }
        if (Objects.nonNull(projectsGrid)) {
            projectsGrid.setDataProvider(DataProvider.ofCollection(domain.domain().port().computeSegments(AccountEntry.PROJECT, from(), to())));
            projectsGrid.getDataProvider().refreshAll();
        }
    }

    private Grid<LedgerPort.Segment> segmentsTable(Collection<LedgerPort.Segment> segments) {
        Grid<LedgerPort.Segment> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
        grid.setWidth("1200px");
        grid.setItems(DataProvider.ofCollection(segments));
        grid.addColumn(LedgerPort.Segment::name).setHeader(NAME_LABEL).setKey(NAME).setSortable(true).setResizable(true);
        grid.addColumn(new NumberRenderer<>(LedgerPort.Segment::amount, VaadinUtils.FORMAT)).setKey(AMOUNT_LABEL).setHeader(AMOUNT).setComparator(LedgerPort.Segment::amount)
            .setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(LedgerPort.Segment::from).setHeader(FROM_LABEL).setKey(FROM).setSortable(true).setResizable(true);
        grid.addColumn(LedgerPort.Segment::to).setHeader(TO_LABEL).setKey(TO).setSortable(true).setResizable(true);
        return grid;
    }

    private Grid<Transaction> transactionsTable() {
        Grid<Transaction> grid = new Grid<>();
        grid.setPageSize(8);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
        grid.setWidth("1200px");
        grid.setItems(DataProvider.ofCollection(
            ProviderView.of(ProviderInMemory.of(domain.domain().realm().transactions(from(), to())), o -> !o.synthetic() && o.vatCode().isPresent())
                .items()));
        VaadinUtils.addColumnBoolean(grid, this::isReconciled, "Reconciled", "reconciled");
        VaadinUtils.addColumnBigDecimal(grid, this::reconciliationDifference, "Difference", "difference");
        grid.addColumn(this::latencyPayment).setHeader("Latency").setKey("latency").setSortable(true).setResizable(true);
        grid.addColumn(Transaction::date).setHeader(DATE_LABEL).setKey(DATE).setSortable(true).setResizable(true);
        grid.addColumn(Transaction::reference).setHeader("Reference").setKey("reference").setSortable(true).setResizable(true);
        grid.addColumn(Transaction::amount).setHeader(AMOUNT_LABEL).setKey(AMOUNT).setSortable(true).setResizable(true);
        grid.addColumn(Transaction::vatCodeAsString).setHeader("VAT").setKey("vat").setSortable(true).setResizable(true);
        grid.addColumn(o -> o.vatCode().isPresent() ? o.vatCode().get().vatRate() : null).setHeader("VAT %").setKey("vat-percentage").setSortable(true)
            .setResizable(true);
        VaadinUtils.addColumnBoolean(grid, Transaction::isSplit, "Split", "split");

        grid.addColumn(o -> Objects.nonNull(invoiceFor(o.reference())) ? invoiceFor(o.reference()).invoicedDate() : null).setHeader("Invoiced Date")
            .setKey("invoicedDate").setSortable(true).setResizable(true);
        grid.addColumn(o -> Objects.nonNull(invoiceFor(o.reference())) ? invoiceFor(o.reference()).dueDate() : null).setHeader("Due Date").setKey("dueDate")
            .setSortable(true).setResizable(true);
        grid.addColumn(o -> Objects.nonNull(invoiceFor(o.reference())) ? invoiceFor(o.reference()).currency() : null).setHeader("Currency").setKey("currency")
            .setSortable(true).setResizable(true);
        VaadinUtils.addColumnBigDecimal(grid, o -> Objects.nonNull(invoiceFor(o.reference())) ? invoiceFor(o.reference()).amountWithoutVat() : null,
            "Invoice Net", "invoiceWithoutVat");
        VaadinUtils.addColumnBigDecimal(grid, o -> Objects.nonNull(invoiceFor(o.reference())) ? invoiceFor(o.reference()).amountWithoutVat() : null,
            "Invoice With VAT", "invoiceWitVat");
        VaadinUtils.addColumnBigDecimal(grid, o -> Objects.nonNull(invoiceFor(o.reference())) ? invoiceFor(o.reference()).vat() : null, "Invoice VAT",
            "invoiceVat");
        grid.setItemDetailsRenderer(createInvoiceViewDetailsRenderer());
        return grid;
    }

    private boolean isReconciled(@NotNull Transaction transaction) {
        InvoicesPort.InvoiceView item = invoiceFor(transaction.reference());
        return Objects.nonNull(item) && (transaction.amount().compareTo(item.amountWithVat()) == 0);
    }

    private BigDecimal reconciliationDifference(@NotNull Transaction transaction) {
        InvoicesPort.InvoiceView item = invoiceFor(transaction.reference());
        return Objects.nonNull(item) ? transaction.amount().subtract(item.amountWithVat()) : BigDecimal.ZERO;
    }

    private long latencyPayment(@NotNull Transaction transaction) {
        InvoicesPort.InvoiceView item = invoiceFor(transaction.reference());
        return Objects.nonNull(item) ? ChronoUnit.DAYS.between(transaction.date(), item.dueDate()) : 0;
    }

    private InvoicesPort.InvoiceView invoiceFor(@NotNull String id) {
        if (!invoiceViewsCache.containsKey(id)) {
            InvoicesBoundedDomain domain = (InvoicesBoundedDomain) this.domain.domain().directory().getBoundedDomain(InvoicesBoundedDomain.DOMAIN).orElseThrow();
            domain.port().invoiceViewFor(id).ifPresent(o -> invoiceViewsCache.put(id, o));
        }
        return invoiceViewsCache.get(id);
    }

    private void profitAndLossChart(@NotNull SOChart chart) {
        var ledgerLogic = domain.domain().logic();
        DateData xValues = new DateData(VaadinUtils.quarterLegends(null, null).toArray(new LocalDate[0]));
        xValues.setName("Quarters");

        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);

        LineChart turnoversChart = createLineChart("Turnover", xValues,
            (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerLogic.turnover(start, end) : BigDecimal.ZERO, rc);
        LineChart ebitCharts = createLineChart("EBIT", xValues,
            (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerLogic.ebit(start, end) : BigDecimal.ZERO, rc);
        LineChart earningsChart = createLineChart("Earnings", xValues,
            (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerLogic.earnings(start, end) : BigDecimal.ZERO, rc);

        chart.add(turnoversChart, ebitCharts, earningsChart);
    }

    private void financialsChart(@NotNull SOChart chart) {
        var ledgerLogic = domain.domain().logic();
        DateData xValues = new DateData(VaadinUtils.quarterLegends(null, null).toArray(new LocalDate[0]));
        xValues.setName("Quarters");

        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);

        LineChart shortTermThirdPartyCapitalChart = createLineChart("Short-Term Third Party Capital", xValues,
            (LocalDate start, LocalDate end) -> (end != null) ? ledgerLogic.balance(LedgerAdapter.SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT, end).negate() :
                BigDecimal.ZERO, rc);
        LineChart longTermThirdPartyCapitalChart = createLineChart("LOng-Term Third Party Capital", xValues,
            (LocalDate start, LocalDate end) -> (end != null) ? ledgerLogic.balance(LedgerAdapter.LONG_TERM_THIRD_PARTY_CAPITAL_ACCOUNT, end).negate() :
                BigDecimal.ZERO, rc);
        LineChart cashOnHandChart = createLineChart("Cash On Hand", xValues,
            (LocalDate start, LocalDate end) -> (end != null) ? ledgerLogic.balance(LedgerAdapter.CASH_ON_HAND_ACCOUNT, end) : BigDecimal.ZERO, rc);
        LineChart equityChart = createLineChart("Equity", xValues,
            (LocalDate start, LocalDate end) -> (end != null) ? ledgerLogic.balance(LedgerAdapter.EQUITY_ACCOUNT, end).negate() : BigDecimal.ZERO, rc);

        chart.add(shortTermThirdPartyCapitalChart, cashOnHandChart, longTermThirdPartyCapitalChart, equityChart);
    }

    private static ComponentRenderer<InvoiceViewDetails, Transaction> createInvoiceViewDetailsRenderer() {
        return new ComponentRenderer<>(InvoiceViewDetails::new, InvoiceViewDetails::value);
    }

    private static class InvoiceViewDetails extends FormLayout {
        private final DatePicker date;
        private final TextField amount;
        private final TextField text;
        private final TextField reference;
        private final TextField vatCode;
        private final TextField vatPercentage;

        public InvoiceViewDetails() {
            VaadinUtils.set3ResponsiveSteps(this);
            date = VaadinUtils.createDatePicker(DATE);
            amount = VaadinUtils.createTextField(AMOUNT_LABEL, AMOUNT);
            reference = VaadinUtils.createTextField("Reference", "reference");
            text = VaadinUtils.createTextField(TEXT_LABEL, TEXT);
            vatCode = VaadinUtils.createTextField("VAT Code", "vatCode");
            vatPercentage = VaadinUtils.createTextField("VAT Percentage", "vatPercentage");
            add(date, text, reference, vatCode, vatPercentage);
        }

        public void value(@NotNull Transaction transaction) {
            date.setValue(transaction.date());
            amount.setValue(transaction.amount().toString());
            text.setValue(transaction.text());
            reference.setValue(transaction.reference());
            vatCode.setValue(transaction.vatCodeAsString());
            vatPercentage.setValue(transaction.vatCode().isPresent() ? transaction.vatCode().get().vatRate().toString() : "");
        }
    }
}
