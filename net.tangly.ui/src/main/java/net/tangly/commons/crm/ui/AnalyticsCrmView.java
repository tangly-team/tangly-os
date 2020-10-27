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

package net.tangly.commons.crm.ui;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.storedobject.chart.BarChart;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.DateData;
import com.storedobject.chart.LineChart;
import com.storedobject.chart.NightingaleRoseChart;
import com.storedobject.chart.Position;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Size;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.crm.BusinessLogicCrm;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.InteractionCode;
import net.tangly.bus.crm.RealmCrm;
import net.tangly.bus.invoices.BusinessLogicInvoices;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.bus.ledger.Ledger;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.ledger.ports.LedgerBusinessLogic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsCrmView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final BusinessLogicCrm logicCrm;
    private final BusinessLogicInvoices logicInvoices;
    private final Ledger ledger;
    private final TabsComponent tabs;
    private SOChart contractsSoChart;
    private SOChart customersSoChart;
    private SOChart profitAndLossSoChart;
    private SOChart financialSoChart;
    private SOChart funnelSoChart;
    private Grid<Contract> contractsGrid;
    private LocalDate from;
    private LocalDate to;

    public AnalyticsCrmView(@NotNull RealmCrm realmCrm, @NotNull RealmInvoices realmInvoices, @NotNull Ledger ledger) {
        logicCrm = new BusinessLogicCrm(realmCrm);
        logicInvoices = new BusinessLogicInvoices(realmInvoices);
        this.ledger = ledger;
        from = LocalDate.of(2015, 11, 1);
        to = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        tabs = new TabsComponent();
        initialize();
    }

    protected void initialize() {
        DatePicker fromDate = new DatePicker("From Date");
        fromDate.setValue(from);
        fromDate.addValueChangeListener(e -> {
            from = e.getValue();
            update();
        });
        DatePicker toDate = new DatePicker("To Date");
        toDate.setValue(to);
        toDate.addValueChangeListener(e -> {
            to = e.getValue();
            update();
        });
        customersSoChart = createAndRegisterChart("Customers Turnover");
        contractsSoChart = createAndRegisterChart("Contracts Turnover");
        profitAndLossSoChart = createAndRegisterChart("Profit & Loss");
        financialSoChart = createAndRegisterChart("Assets");
        funnelSoChart = createAndRegisterChart("Funnel");

        contractsGrid = contractsTable();
        tabs.add(new Tab("Spent On Contracts"), new HorizontalLayout(contractsGrid));

        update();
        setSizeFull();
        tabs.initialize(tabs.tabByName("Customers Turnover").orElseThrow());
        add(new HorizontalLayout(fromDate, toDate), tabs);
    }

    private Grid<Contract> contractsTable() {
        Grid<Contract> grid = new Grid<>();
        grid.setDataProvider(DataProvider.ofCollection(logicCrm.realm().contracts().items()));
        grid.addColumn(Contract::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setHeader("Amount").setAutoWidth(true).setResizable(true)
                .setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> logicInvoices.invoicedAmountWithoutVatForContract(o.id(), from, to), VaadinUtils.FORMAT))
                .setHeader("Invoiced").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> logicInvoices.expensesForContract(o.id(), from, to), VaadinUtils.FORMAT)).setHeader("Expenses")
                .setAutoWidth(true).setResizable(true).setSortable(true);
        return grid;
    }

    private void contractsChart(@NotNull SOChart chart) {
        List<String> contracts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        logicCrm.realm().contracts().items().forEach(contract -> {
            BigDecimal amount = logicInvoices.invoicedAmountWithoutVatForContract(contract.id(), from, to);
            if (!amount.equals(BigDecimal.ZERO)) {
                contracts.add(contract.id());
                amounts.add(amount);
            }
        });
        chart.add(createChart("Contracts Turnover", new CategoryData(contracts.toArray(new String[0])), new Data(amounts.toArray(new BigDecimal[0]))));
    }

    private void customersChart(@NotNull SOChart chart) {
        List<String> customers = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        logicCrm.realm().legalEntities().items().forEach(customer -> {
            BigDecimal amount = logicInvoices.paidAmountWithoutVatForCustomer(customer.id(), from, to);
            if (!amount.equals(BigDecimal.ZERO)) {
                customers.add(customer.name());
                amounts.add(amount);
            }
        });
        chart.add(createChart("Customers Turnover", new CategoryData(customers.toArray(new String[0])), new Data(amounts.toArray(new BigDecimal[0]))));
    }

    private NightingaleRoseChart createChart(@NotNull String name, @NotNull CategoryData categoryData, Data data) {
        NightingaleRoseChart roseChart = new NightingaleRoseChart(categoryData, data);
        roseChart.setName(name);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(10));
        roseChart.setPosition(chartPosition);
        return roseChart;
    }

    private void profitAndLossChart(@NotNull SOChart chart) {
        LedgerBusinessLogic ledgerBusinessLogic = new LedgerBusinessLogic(ledger);
        DateData xValues = new DateData(ledgerBusinessLogic.quarterLegends(null, null).toArray(new LocalDate[0]));
        xValues.setName("Quarters");

        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);

        LineChart turnoversChart = createLineChart("Turnover", xValues,
                (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerBusinessLogic.turnover(start, end) : BigDecimal.ZERO, rc);
        LineChart ebitsCharts = createLineChart("EBIT", xValues,
                (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerBusinessLogic.ebit(start, end) : BigDecimal.ZERO, rc);
        LineChart earningsChart = createLineChart("Earnings", xValues,
                (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerBusinessLogic.earnings(start, end) : BigDecimal.ZERO, rc);

        chart.add(turnoversChart, ebitsCharts, earningsChart);
    }

    private void financialsChart(@NotNull SOChart chart) {
        LedgerBusinessLogic logic = new LedgerBusinessLogic(ledger);
        DateData xValues = new DateData(logic.quarterLegends(null, null).toArray(new LocalDate[0]));
        xValues.setName("Quarters");

        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);

        LineChart shortTermThirdPartyCapitalChart = createLineChart("Short-Term Third Party Capital", xValues,
                (LocalDate start, LocalDate end) -> (end != null) ? logic.balance(LedgerBusinessLogic.SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT, end).negate() :
                        BigDecimal.ZERO, rc);
        LineChart longTermThirdPartyCapitalChart = createLineChart("LOng-Term Third Party Capital", xValues,
                (LocalDate start, LocalDate end) -> (end != null) ? logic.balance(LedgerBusinessLogic.LONG_TERM_THIRD_PARTY_CAPITAL_ACCOUNT, end).negate() :
                        BigDecimal.ZERO, rc);
        LineChart cashOnHandChart = createLineChart("Cash On Hand", xValues,
                (LocalDate start, LocalDate end) -> (end != null) ? logic.balance(LedgerBusinessLogic.CASH_ON_HAND_ACCOUNT, end) : BigDecimal.ZERO, rc);
        LineChart equityChart = createLineChart("Equity", xValues,
                (LocalDate start, LocalDate end) -> (end != null) ? logic.balance(LedgerBusinessLogic.EQUITY_ACCOUNT, end).negate() : BigDecimal.ZERO, rc);

        chart.add(shortTermThirdPartyCapitalChart, cashOnHandChart, longTermThirdPartyCapitalChart, equityChart);
    }

    private void funnelChart(@NotNull SOChart chart) {
        CategoryData labels = new CategoryData("Prospects", "Leads", "Customers", "Lost", "Completed");
        Data data = new Data(logicCrm.funnel(InteractionCode.prospect, from, to), logicCrm.funnel(InteractionCode.lead, from, to),
                logicCrm.funnel(InteractionCode.customer, from, to), logicCrm.funnel(InteractionCode.lost, from, to),
                logicCrm.funnel(InteractionCode.completed, from, to));
        BarChart barchart = new BarChart(labels, data);
        RectangularCoordinate rc = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);
        barchart.plotOn(rc);
        chart.add(barchart);
    }

    private SOChart createAndRegisterChart(String label) {
        SOChart chart = new SOChart();
        chart.setSize("1200px", "600px");
        HorizontalLayout layout = new HorizontalLayout(chart);
        layout.setSizeFull();
        tabs.add(new Tab(label), layout);
        return chart;
    }

    private LineChart createLineChart(String name, DateData xValues, BiFunction<LocalDate, LocalDate, BigDecimal> compute, RectangularCoordinate rc) {
        Data data = new Data();
        data.setName(name);
        LocalDate start;
        LocalDate end = null;
        for (LocalDate date : xValues) {
            start = end;
            end = date;
            data.add(compute.apply(start, end));
        }
        LineChart lineChart = new LineChart(xValues, data);
        lineChart.setName(name);
        lineChart.plotOn(rc);
        return lineChart;
    }

    private void update() {
        update(customersSoChart, this::customersChart);
        update(contractsSoChart, this::contractsChart);
        update(profitAndLossSoChart, this::profitAndLossChart);
        update(financialSoChart, this::financialsChart);
        update(funnelSoChart, this::funnelChart);
        contractsGrid.getDataProvider().refreshAll();
    }

    private void update(@NotNull SOChart chart, @NotNull Consumer<SOChart> populate) {
        try {
            chart.removeAll();
            populate.accept(chart);
            chart.update();
        } catch (Exception e) {
            // TODO logger.atError().setCause(e).log("Error when updating SO charts");
        }
    }
}
