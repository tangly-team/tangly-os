/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.storedobject.chart.BarChart;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.Position;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Size;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.InteractionCode;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.ui.app.domain.AnalyticsView;
import net.tangly.ui.components.VaadinUtils;
import net.tangly.ui.grids.PaginatedGrid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class AnalyticsCrmView extends AnalyticsView {
    private static final String CustomersTurnover = "Customers Turnover";
    private static final String ContractsTurnover = "Contracts Turnover";
    private static final String Funnel = "Funnel";
    private static final String SpentOnContracts = "Spent On Contracts";
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private final CrmBoundedDomain crmDomain;
    private final InvoicesBusinessLogic invoicesLogic;
    private SOChart contractsSoChart;
    private SOChart customersSoChart;
    private SOChart funnelSoChart;
    private PaginatedGrid<Contract> contractsGrid;

    public AnalyticsCrmView(@NotNull CrmBoundedDomain crmDomain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        this.crmDomain = crmDomain;
        this.invoicesLogic = invoicesDomain.logic();
        initialize();
        update();
    }

    protected void initialize() {
        customersSoChart = createAndRegisterChart(CustomersTurnover);
        contractsSoChart = createAndRegisterChart(ContractsTurnover);
        funnelSoChart = createAndRegisterChart(Funnel);
        contractsGrid = contractsTable();
        tabs.add(new Tab(SpentOnContracts), new VerticalLayout(contractsGrid));
        tabs.initialize(tabs.tabByName(CustomersTurnover).orElseThrow());
    }

    @Override
    protected void update() {
        update(customersSoChart, this::customersChart);
        update(contractsSoChart, this::contractsChart);
        update(funnelSoChart, this::funnelChart);
        contractsGrid.getDataProvider().refreshAll();
    }

    private PaginatedGrid<Contract> contractsTable() {
        PaginatedGrid<Contract> grid = new PaginatedGrid<>();
        grid.setPageSize(8);
        grid.setHeightFull();
        grid.dataProvider(DataProvider.ofCollection(crmDomain.realm().contracts().items()));
        grid.addColumn(Contract::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setHeader("Amount").setAutoWidth(true).setResizable(true)
            .setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> invoicesLogic.invoicedAmountWithoutVatForContract(o.id(), from(), to()), VaadinUtils.FORMAT))
            .setHeader("Invoiced").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> invoicesLogic.expensesForContract(o.id(), from(), to()), VaadinUtils.FORMAT)).setHeader("Expenses")
            .setAutoWidth(true).setResizable(true).setSortable(true);
        return grid;
    }

    private void contractsChart(@NotNull SOChart chart) {
        List<String> contracts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        crmDomain.realm().contracts().items().forEach(contract -> {
            BigDecimal amount = invoicesLogic.invoicedAmountWithoutVatForContract(contract.id(), from(), to());
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
        crmDomain.realm().legalEntities().items().forEach(customer -> {
            BigDecimal amount = invoicesLogic.paidAmountWithoutVatForCustomer(customer.id(), from(), to());
            if (!amount.equals(BigDecimal.ZERO)) {
                customers.add(customer.name());
                amounts.add(amount);
            }
        });
        chart.add(createChart("Customers Turnover", new CategoryData(customers.toArray(new String[0])), new Data(amounts.toArray(new BigDecimal[0]))));
    }

    private void funnelChart(@NotNull SOChart chart) {
        CategoryData labels = new CategoryData("Prospects", "Leads", "Ordered", "Lost", "Completed");
        CrmBusinessLogic logic = crmDomain.logic();
        Data data = new Data(logic.funnel(InteractionCode.prospect, from(), to()), logic.funnel(InteractionCode.lead, from(), to()),
            logic.funnel(InteractionCode.ordered, from(), to()), logic.funnel(InteractionCode.lost, from(), to()),
            logic.funnel(InteractionCode.completed, from(), to()));
        BarChart barchart = new BarChart(labels, data);
        RectangularCoordinate rc = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);
        barchart.plotOn(rc);
        chart.add(barchart);
    }
}
