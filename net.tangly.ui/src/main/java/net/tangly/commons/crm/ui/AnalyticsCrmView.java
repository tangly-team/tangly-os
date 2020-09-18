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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import net.tangly.bus.ledger.Ledger;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.crm.ports.Crm;
import net.tangly.crm.ports.CrmBusinessLogic;
import net.tangly.ledger.ports.LedgerBusinessLogic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsCrmView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsCrmView.class);
    private final Crm crm;
    private final Ledger ledger;
    private final TabsComponent tabs;

    private SOChart contractsSoChart;
    private SOChart customersSoChart;
    private SOChart quaterlySoChart;
    private LocalDate from;
    private LocalDate to;


    public AnalyticsCrmView(@NotNull Crm crm, @NotNull Ledger ledger) {
        this.crm = crm;
        this.ledger = ledger;
        from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
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

        quaterlySoChart = new SOChart();
        quaterlySoChart.setSize("1024px", "600px");
        customersSoChart = new SOChart();
        customersSoChart.setSize("1024px", "600px");
        contractsSoChart = new SOChart();
        contractsSoChart.setSize("1024px", "600px");
        update();

        setSizeFull();
        Div div = new Div();
        div.setText("Contracts");
        tabs.add(new Tab("Customers Turnover"), new HorizontalLayout(customersSoChart));
        tabs.add(new Tab("Contracts Turnover"), new HorizontalLayout(contractsSoChart));
        tabs.add(new Tab("Quarterly Financials"), new HorizontalLayout(quaterlySoChart));
        tabs.initialize(tabs.tabByName("Customers Turnover").orElseThrow());
        add(new HorizontalLayout(fromDate, toDate), tabs);
    }

    private void contractsChart(@NotNull SOChart chart) {
        CrmBusinessLogic logic = new CrmBusinessLogic(crm);
        List<String> contracts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        crm.contracts().items().forEach(contract -> {
            BigDecimal amount = logic.contractAmountWithoutVat(contract, from, to);
            if (!amount.equals(BigDecimal.ZERO)) {
                contracts.add(contract.id());
                amounts.add(amount);
            }
        });
        CategoryData labels = new CategoryData(contracts.toArray(new String[0]));
        Data data = new Data(amounts.toArray(new BigDecimal[0]));
        NightingaleRoseChart roseChart = new NightingaleRoseChart(labels, data);
        roseChart.setName("Contracts Turnover");
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        roseChart.setPosition(chartPosition);
        chart.add(roseChart);
    }

    private void customersChart(@NotNull SOChart chart) {
        CrmBusinessLogic logic = new CrmBusinessLogic(crm);
        List<String> customers = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        crm.legalEntities().items().forEach(customer -> {
            BigDecimal amount = logic.customerAmountWithoutVat(customer, from, to);
            if (!amount.equals(BigDecimal.ZERO)) {
                customers.add(customer.name());
                amounts.add(amount);
            }
        });
        CategoryData labels = new CategoryData(customers.toArray(new String[0]));
        Data data = new Data(amounts.toArray(new BigDecimal[0]));
        NightingaleRoseChart roseChart = new NightingaleRoseChart(labels, data);
        roseChart.setName("Customers Turnover");
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        roseChart.setPosition(chartPosition);
        chart.add(roseChart);
    }

    private void quarterlyChart(@NotNull SOChart chart) {
        LedgerBusinessLogic ledgerBusinessLogic = new LedgerBusinessLogic(ledger);
        DateData xValues = new DateData(ledgerBusinessLogic.quarterLegends(null, null).toArray(new LocalDate[0]));
        xValues.setName("Quarters");

        Data turnovers = new Data();
        Data ebits = new Data();
        Data earnings = new Data();
        Data shortTermThirdPartyCapital = new Data();
        Data equity = new Data();
        Data cashOnHand = new Data();

        LocalDate start = null;
        LocalDate end = null;
        for (LocalDate date : xValues) {
            start = end;
            end = date;
            turnovers.add(((start != null) && (end != null)) ? ledgerBusinessLogic.turnover(start, end) : BigDecimal.ZERO);
            ebits.add(((start != null) && (end != null)) ? ledgerBusinessLogic.ebit(start, end) : BigDecimal.ZERO);
            earnings.add(((start != null) && (end != null)) ? ledgerBusinessLogic.earnings(start, end) : BigDecimal.ZERO);
            shortTermThirdPartyCapital
                    .add((end != null) ? ledgerBusinessLogic.balance(LedgerBusinessLogic.SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT, end).negate() : BigDecimal.ZERO);
            equity.add((end != null) ? ledgerBusinessLogic.balance(LedgerBusinessLogic.EQUITY_ACCOUNT, end).negate() : BigDecimal.ZERO);
            cashOnHand.add((end != null) ? ledgerBusinessLogic.balance(LedgerBusinessLogic.CASH_ON_HAND_ACCOUNT, end) : BigDecimal.ZERO);
        }

        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);

        turnovers.setName("Turnover");
        LineChart turnoversChart = new LineChart(xValues, turnovers);
        turnoversChart.setName("Turnover");
        turnoversChart.plotOn(rc);

        ebits.setName("EBIT");
        LineChart ebitsCharts = new LineChart(xValues, ebits);
        ebitsCharts.setName("EBIT");
        ebitsCharts.plotOn(rc);

        earnings.setName("Earnings");
        LineChart earningsChart = new LineChart(xValues, earnings);
        earningsChart.setName("Earnings");
        earningsChart.plotOn(rc);

        shortTermThirdPartyCapital.setName("Short-Term Third Party Capital");
        LineChart shortTermThirdPartyCapitalChart = new LineChart(xValues, shortTermThirdPartyCapital);
        shortTermThirdPartyCapitalChart.setName("Short-Term Third Party Capital");
        shortTermThirdPartyCapitalChart.plotOn(rc);

        equity.setName("Equity");
        LineChart equityChart = new LineChart(xValues, equity);
        equityChart.setName("Equity");
        equityChart.plotOn(rc);

        cashOnHand.setName("Cash On Hand");
        LineChart cashOnHandChart = new LineChart(xValues, cashOnHand);
        cashOnHandChart.setName("Cash On Hand");
        cashOnHandChart.plotOn(rc);

        chart.add(turnoversChart, ebitsCharts, earningsChart, shortTermThirdPartyCapitalChart, earningsChart, cashOnHandChart);
    }

    private void update() {
        update(customersSoChart, this::customersChart);
        update(contractsSoChart, this::contractsChart);
        update(quaterlySoChart, this::quarterlyChart);
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
