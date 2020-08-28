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
import com.vaadin.flow.component.Component;
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

public class AnalyticsCrmView extends HorizontalLayout {
    private final Crm crm;
    private final Ledger ledger;
    private LocalDate fromDate;
    private LocalDate toDate;
    private final TabsComponent tabs;

    public AnalyticsCrmView(@NotNull Crm crm, @NotNull Ledger ledger) {
        this.crm = crm;
        this.ledger = ledger;
        tabs = new TabsComponent();
        setSizeFull();
        Div div = new Div();
        div.setText("Contracts");
        Tab contracts = new Tab("Contracts Turnover");
        tabs.add(contracts, contractsChart());
        tabs.add(new Tab("Customers Turnover"), customersChart());
        tabs.add(new Tab("Quarterly Turnover"), quarterlyChart());
        tabs.initialize(contracts);
        add(tabs);
    }

    private Component contractsChart() {
        DatePicker fromDatePicker = new DatePicker("From Date");
        fromDatePicker.setPlaceholder("From Date");
        fromDatePicker.setClearButtonVisible(true);
        fromDatePicker.setValue(fromDate);
        fromDatePicker.addValueChangeListener(e -> fromDate = e.getValue());
        DatePicker toDatePicker = new DatePicker("To Date");
        toDatePicker.setPlaceholder("To Date");
        toDatePicker.setClearButtonVisible(true);
        fromDatePicker.addValueChangeListener(e -> toDate = e.getValue());
        toDatePicker.setValue(toDate);
        HorizontalLayout dates = new HorizontalLayout(fromDatePicker, toDatePicker);

        CrmBusinessLogic logic = new CrmBusinessLogic(crm);
        List<String> contracts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        crm.contracts().getAll().forEach(contract -> {
            BigDecimal amount = logic.contractAmountWithoutVat(contract, null, null);
            if (!amount.equals(BigDecimal.ZERO)) {
                contracts.add(contract.id());
                amounts.add(amount);
            }
        });
        CategoryData labels = new CategoryData(contracts.toArray(new String[0]));
        Data data = new Data(amounts.toArray(new BigDecimal[0]));
        NightingaleRoseChart chart = new NightingaleRoseChart(labels, data);
        chart.setName("Contracts Turnover");
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        chart.setPosition(chartPosition);
        SOChart soChart = new SOChart();
        soChart.setSize("1024px", "600px");
        soChart.add(chart);
        return new VerticalLayout(dates, new HorizontalLayout(soChart));
    }

    private HorizontalLayout customersChart() {
        CrmBusinessLogic logic = new CrmBusinessLogic(crm);
        List<String> customers = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        crm.legalEntities().getAll().forEach(customer -> {
            BigDecimal amount = logic.customerAmountWithoutVat(customer, null, null);
            if (!amount.equals(BigDecimal.ZERO)) {
                customers.add(customer.name());
                amounts.add(amount);
            }
        });
        CategoryData labels = new CategoryData(customers.toArray(new String[0]));
        Data data = new Data(amounts.toArray(new BigDecimal[0]));
        NightingaleRoseChart chart = new NightingaleRoseChart(labels, data);
        chart.setName("Customers Turnover");
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        chart.setPosition(chartPosition);
        SOChart soChart = new SOChart();
        soChart.setSize("1024px", "600px");
        soChart.add(chart);
        return new HorizontalLayout(soChart);
    }

    private HorizontalLayout quarterlyChart() {
        LedgerBusinessLogic logic = new LedgerBusinessLogic(ledger);
        DateData xValues = new DateData(logic.quarterLegends(null, null).toArray(new LocalDate[0]));
        xValues.setName("Quarters");

        Data turnovers = new Data();
        Data ebits = new Data();
        Data earnings = new Data();
        LocalDate start = null;
        LocalDate end = null;
        for (LocalDate date : xValues) {
            start = end;
            end = date;
            turnovers.add(((start != null) && (end != null)) ? logic.turnover(start, end) : BigDecimal.ZERO);
            ebits.add(((start != null) && (end != null)) ? logic.ebit(start, end) : BigDecimal.ZERO);
            earnings.add(((start != null) && (end != null)) ? logic.earnings(start, end) : BigDecimal.ZERO);
        }
        turnovers.setName("Turnovers");
        ebits.setName("Ebits");
        earnings.setName("Earnings");

        LineChart turnoversChart = new LineChart(xValues, turnovers);
        turnoversChart.setName("Turnover per Quarter");
        LineChart ebitsCharts = new LineChart(xValues, ebits);
        ebitsCharts.setName("Ebit per Quarter");
        LineChart earningsChart = new LineChart(xValues, earnings);
        earningsChart.setName("Earnings per Quarter");


        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);

        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);

        turnoversChart.plotOn(rc);
        ebitsCharts.plotOn(rc);
        earningsChart.plotOn(rc);

        SOChart soChart = new SOChart();
        soChart.setSize("1024px", "600px");
        soChart.add(turnoversChart, ebitsCharts, earningsChart);
        return new HorizontalLayout(soChart);
    }
}
