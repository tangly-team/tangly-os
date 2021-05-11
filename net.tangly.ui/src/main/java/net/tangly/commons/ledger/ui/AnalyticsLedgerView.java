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

package net.tangly.commons.ledger.ui;

import com.storedobject.chart.*;
import net.tangly.ledger.services.LedgerBoundedDomain;
import net.tangly.ledger.services.LedgerBusinessLogic;
import net.tangly.commons.domain.ui.AnalyticsView;
import net.tangly.commons.vaadin.VaadinUtils;
import net.tangly.ledger.ports.LedgerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AnalyticsLedgerView extends AnalyticsView {
    private static final String ProfitAndLoss = "Profit & Loss";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final LedgerBusinessLogic ledgerLogic;
    private SOChart profitAndLossSoChart;
    private SOChart financialSoChart;

    public AnalyticsLedgerView(@NotNull LedgerBoundedDomain domain) {
        this.ledgerLogic = domain.logic();
        initialize();
        update();
    }

    protected void initialize() {
        profitAndLossSoChart = createAndRegisterChart(ProfitAndLoss);
        financialSoChart = createAndRegisterChart("Assets");
        setSizeFull();
        tabs.initialize(tabs.tabByName(ProfitAndLoss).orElseThrow());
    }

    @Override
    protected void update() {
        update(profitAndLossSoChart, this::profitAndLossChart);
        update(financialSoChart, this::financialsChart);
    }


    private void profitAndLossChart(@NotNull SOChart chart) {
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
        LineChart ebitsCharts = createLineChart("EBIT", xValues,
            (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerLogic.ebit(start, end) : BigDecimal.ZERO, rc);
        LineChart earningsChart = createLineChart("Earnings", xValues,
            (LocalDate start, LocalDate end) -> ((start != null) && (end != null)) ? ledgerLogic.earnings(start, end) : BigDecimal.ZERO, rc);

        chart.add(turnoversChart, ebitsCharts, earningsChart);
    }

    private void financialsChart(@NotNull SOChart chart) {
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
}
