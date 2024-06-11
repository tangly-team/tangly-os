/*
 * Copyright 2024 Marcel Baumann
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

import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import net.tangly.core.HasDateRange;
import net.tangly.core.HasText;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.erp.crm.domain.ContractExtension;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import net.tangly.ui.app.domain.Cmd;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class ContractExtensionsView extends ItemView<ContractExtension> {

    public ContractExtensionsView(@NotNull CrmBoundedDomainUi domain, @NotNull Mode mode) {
        super(ContractExtension.class, domain, ProviderInMemory.of(), null, mode);
        init();
    }

    @Override
    public CrmBoundedDomain domain() {
        return domain();
    }

    @Override
    protected void addActions(@NotNull GridContextMenu<ContractExtension> menu) {
        menu().add(new Hr());
        menu().addItem("Activate", e -> Cmd.ofItemCmd(e, this::contractActivated));
    }

    private void init() {
        setHeight("15em");
        Grid<ContractExtension> grid = grid();
        VaadinUtils.addColumn(grid, ContractExtension::id, ID, ID_LABEL);
        VaadinUtils.addColumn(grid, ContractExtension::name, NAME, NAME_LABEL);
        VaadinUtils.addColumn(grid, new LocalDateRenderer<>(HasDateRange::from, ItemView.ISO_DATE_FORMAT), FROM, FROM_LABEL);
        VaadinUtils.addColumn(grid, new LocalDateRenderer<>(HasDateRange::to, ItemView.ISO_DATE_FORMAT), TO, TO_LABEL);

        grid.addColumn(HasText::text).setKey(TEXT).setHeader(TEXT_LABEL).setResizable(true).setSortable(true).setFlexGrow(0).setWidth("30em");
        grid.addColumn(new NumberRenderer<>(ContractExtension::amountWithoutVat, VaadinUtils.FORMAT)).setKey("amount").setHeader("Amount").setAutoWidth(true)
            .setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(new NumberRenderer<>(ContractExtension::budgetInHours, VaadinUtils.FORMAT)).setKey("budgetInHours").setHeader("Budget In Hours")
            .setAutoWidth(true).setResizable(true).setSortable(true).setTextAlign(ColumnTextAlign.END);
    }

    /**
     * Activate the contract extension in the ERP system. The {@link net.tangly.erp.crm.events.ContractSignedEvent} is published to the event bus.
     * The activation states the intent to start working on the contract.
     *
     * @param extension contract extension to activate
     */
    private void contractActivated(ContractExtension extension) {
        var contract = Provider.findById(domain().realm().contracts(), extension.contractId());
        var event = ContractExtension.of(extension, contract.orElseThrow());
        domain().channel().submit(event);
    }
}
