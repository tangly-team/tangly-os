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

package net.tangly.erp.crm.ui;

import com.storedobject.chart.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.core.HasDateRange;
import net.tangly.core.providers.ProviderView;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.OpportunityCode;
import net.tangly.erp.crm.services.CrmBusinessLogic;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.ui.app.domain.AnalyticsView;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.tangly.ui.components.ItemView.*;

public class AnalyticsCrmView extends AnalyticsView {
    private static final String CustomersTurnover = "Customers Turnover";
    private static final String ContractsTurnover = "Contracts Turnover";
    private static final String Funnel = "Funnel";
    private static final String SpentOnContracts = "Spent On Contracts";
    private final CrmBoundedDomainUi domain;
    private final InvoicesBoundedDomain invoicesBoundedDomain;
    private SOChart contractsSoChart;
    private SOChart customersSoChart;
    private SOChart funnelSoChart;
    private Grid<Contract> contractsGrid;

    public AnalyticsCrmView(@NotNull CrmBoundedDomainUi domain, @NotNull InvoicesBoundedDomain invoicesDomain) {
        this.domain = domain;
        this.invoicesBoundedDomain = invoicesDomain;
        init();
    }

    private void init() {
        customersSoChart = createAndRegisterChart(CustomersTurnover);
        contractsSoChart = createAndRegisterChart(ContractsTurnover);
        funnelSoChart = createAndRegisterChart(Funnel);
        contractsGrid = contractsTable();
        var layout = new HorizontalLayout(contractsGrid);
        layout.setSizeFull();
        tabSheet().add(SpentOnContracts, layout);
    }

    @Override
    public void refresh() {
        refresh(customersSoChart, this::customersChart);
        refresh(contractsSoChart, this::contractsChart);
        refresh(funnelSoChart, this::funnelChart);
        refreshContractTable();
    }

    private void refreshContractTable() {
        if (Objects.nonNull(contractsGrid)) {
            contractsGrid.setDataProvider(
                DataProvider.ofCollection(ProviderView.of(domain.domain().realm().contracts(), new HasDateRange.RangeFilter<>(from(), to())).items()));
            contractsGrid.getDataProvider().refreshAll();
        }
    }

    private Grid<Contract> contractsTable() {
        var invoicesLogic = invoicesBoundedDomain.logic();
        Grid<Contract> grid = new Grid<>();
        grid.setPageSize(8);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setHeight("24em");
        grid.setWidth("1200px");
        grid.setItems(
            DataProvider.ofCollection(ProviderView.of(domain.domain().realm().contracts(), new HasDateRange.RangeFilter<>(from(), to())).items()));
        grid.addColumn(Contract::id).setKey(ID).setHeader(ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::name).setKey(NAME).setHeader(ID_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::from).setKey(FROM).setHeader(FROM_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::to).setKey(TO).setHeader(TO_LABEL).setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Contract::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(Contract::amountWithoutVat, VaadinUtils.FORMAT)).setKey(AMOUNT).setHeader(AMOUNT_LABEL).setAutoWidth(true)
            .setResizable(true)
            .setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> invoicesLogic.invoicedAmountWithoutVatForContract(o.id(), from(), to()), VaadinUtils.FORMAT))
            .setHeader("Invoiced").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> invoicesLogic.expensesForContract(o.id(), from(), to()), VaadinUtils.FORMAT)).setHeader("Expenses")
            .setAutoWidth(true).setResizable(true).setSortable(true);
        return grid;
    }

    private void contractsChart(@NotNull SOChart chart) {
        var invoicesLogic = invoicesBoundedDomain.logic();
        List<String> contracts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        domain.domain().realm().contracts().items().forEach(contract -> {
            BigDecimal amount = invoicesLogic.invoicedAmountWithoutVatForContract(contract.id(), from(), to());
            if (!amount.equals(BigDecimal.ZERO)) {
                contracts.add(contract.id());
                amounts.add(amount);
            }
        });
        chart.add(createChart("Contracts Turnover", new CategoryData(contracts.toArray(new String[0])), new Data(amounts.toArray(new BigDecimal[0]))));
    }

    private void customersChart(@NotNull SOChart chart) {
        var invoicesLogic = invoicesBoundedDomain.logic();
        List<String> customers = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        domain.domain().realm().legalEntities().items().forEach(customer -> {
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
        CrmBusinessLogic logic = domain.domain().logic();
        BigDecimal prospects = logic.funnel(OpportunityCode.prospect, from(), to());
        BigDecimal leads = logic.funnel(OpportunityCode.lead, from(), to());
        BigDecimal ordered = logic.funnel(OpportunityCode.ordered, from(), to());
        BigDecimal lost = logic.funnel(OpportunityCode.lost, from(), to());
        BigDecimal completed = logic.funnel(OpportunityCode.completed, from(), to());
        BarChart barchart = new BarChart(labels, new Data(prospects, leads, ordered, lost, completed));
        RectangularCoordinate rc = new RectangularCoordinate(new XAxis(DataType.CATEGORY), new YAxis(DataType.NUMBER));
        Position chartPosition = new Position();
        chartPosition.setTop(Size.percentage(20));
        rc.setPosition(chartPosition);
        barchart.plotOn(rc);
        chart.add(barchart);
    }
}
