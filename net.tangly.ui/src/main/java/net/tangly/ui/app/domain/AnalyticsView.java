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

package net.tangly.ui.app.domain;

import com.storedobject.chart.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class AnalyticsView extends VerticalLayout {
    private static final Logger logger = LogManager.getLogger();
    private LocalDate from;
    private LocalDate to;
    protected final TabSheet tabSheet;

    public AnalyticsView() {
        from = LocalDate.of(2015, Month.NOVEMBER, 1);
        to = LocalDate.of(LocalDate.now().getYear(), Month.DECEMBER, 31);
        tabSheet = new TabSheet();
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
        setSizeFull();
        add(new HorizontalLayout(fromDate, toDate), tabSheet);
    }

    public LocalDate from() {
        return from;
    }

    public LocalDate to() {
        return to;
    }

    protected abstract void update();

    protected NightingaleRoseChart createChart(@NotNull String name, @NotNull CategoryData categoryData, @NotNull Data data) {
        NightingaleRoseChart roseChart = new NightingaleRoseChart(categoryData, data);
        roseChart.setName(name);
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(10));
        roseChart.setPosition(chartPosition);
        return roseChart;
    }

    protected SOChart createAndRegisterChart(@NotNull String label) {
        SOChart chart = new SOChart();
        chart.setSize("1200px", "600px");
        HorizontalLayout layout = new HorizontalLayout(chart);
        layout.setSizeFull();
        tabSheet.add(label, layout);
        return chart;
    }

    protected LineChart createLineChart(@NotNull String name, @NotNull DateData xValues, @NotNull BiFunction<LocalDate, LocalDate, BigDecimal> compute,
                                        @NotNull RectangularCoordinate rc) {
        LocalDate start;
        LocalDate end = null;
        List<BigDecimal> dataValues = new ArrayList<>();
        for (LocalDate date : xValues) {
            start = end;
            end = date;
            dataValues.add(compute.apply(start, end));
        }
        Data data = new Data(dataValues.toArray(BigDecimal[]::new));
        data.setName(name);
        LineChart lineChart = new LineChart(xValues, data);
        lineChart.setName(name);
        lineChart.plotOn(rc);
        return lineChart;
    }

    protected void update(@NotNull SOChart chart, @NotNull Consumer<SOChart> populate) {
        try {
            chart.removeAll();
            populate.accept(chart);
            chart.update();
        } catch (Exception e) {
            logger.atError().withThrowable(e).log("Error when updating SO charts");
        }
    }
}
