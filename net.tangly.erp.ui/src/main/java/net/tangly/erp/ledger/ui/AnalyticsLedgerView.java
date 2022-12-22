/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.ledger.ui;

import com.storedobject.chart.DataType;
import com.storedobject.chart.DateData;
import com.storedobject.chart.LineChart;
import com.storedobject.chart.Position;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Size;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import net.tangly.erp.ledger.ports.LedgerAdapter;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.erp.ledger.services.LedgerBusinessLogic;
import net.tangly.ui.app.domain.AnalyticsView;
import net.tangly.ui.components.VaadinUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnalyticsLedgerView extends AnalyticsView {
    private static final String ProfitAndLoss = "Profit & Loss";
    private static final Logger logger = LogManager.getLogger();
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
