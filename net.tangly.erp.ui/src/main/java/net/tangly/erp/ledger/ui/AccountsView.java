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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.services.LedgerBoundedDomain;
import net.tangly.ui.components.EntitiesView;
import net.tangly.ui.components.VaadinUtils;
import net.tangly.ui.grids.GridDecorators;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Charts of accounts are defined externally as TSV files. Currently the account view is a read-only view on accounts instances.
 */
@PageTitle("ledger-accounts")
class AccountsView extends EntitiesView<Account> {
    private final transient LedgerBoundedDomain domain;
    private LocalDate from;
    private LocalDate to;
    private Binder<Account> binder;

    /**
     * Constructor of the CRUD view for accounts of the ledger.
     *
     * @param domain ledger business domain containing the accounts should be displayed
     * @param mode   mode of the view
     */
    public AccountsView(@NotNull LedgerBoundedDomain domain, @NotNull Mode mode) {
        super(Account.class, mode, domain.realm().accounts());
        this.domain = domain;
        from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        to = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        initialize();
    }

    public void interval(@NotNull LocalDate from, @NotNull LocalDate to) {
        this.from = from;
        this.to = to;
        grid().getDataProvider().refreshAll();
    }

    @Override
    protected void initialize() {
        var grid = grid();
        grid.addColumn(Account::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::group).setKey("group").setHeader("Group").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true);
        grid.addColumn(VaadinUtils.coloredRender(o -> o.balance(from), VaadinUtils.FORMAT)).setKey("opening").setHeader("Opening").setAutoWidth(true)
            .setResizable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(VaadinUtils.coloredRender(o -> o.balance(to), VaadinUtils.FORMAT)).setKey("balance").setHeader("Balance").setAutoWidth(true)
            .setResizable(true).setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Account::kind).setKey("kind").setHeader("Kind").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::ownedBy).setKey("ownedBy").setHeader("Owned By").setAutoWidth(true).setResizable(true);

        GridDecorators<Account> decorators = gridFiltersAndActions(true, false);
        decorators.addFilter(new GridDecorators.FilterText<>(decorators, Account::id, "Id", "id"))
            .addFilter(new GridDecorators.FilterText<>(decorators, Account::name, "Name", "name"));
        decorators.addItemAction("Print", e -> new CmdCreateLedgerDocument(domain).execute());
        addAndExpand(decorators, grid(), gridButtons());
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Account entity, FormLayout form) {
        TextField id = VaadinUtils.createTextField("Id", "id");
        id.setRequired(true);
        TextField name = VaadinUtils.createTextField("Name", "name");
        name.setRequired(true);
        Select<Account.AccountKind> kind = VaadinUtils.createSelectFor(Account.AccountKind.class, "Kind");
        kind.setRequiredIndicatorVisible(true);
        Select<Account.AccountGroup> group = VaadinUtils.createSelectFor(Account.AccountGroup.class, "Group");
        group.setRequiredIndicatorVisible(true);
        TextField currency = VaadinUtils.createTextField("Currency", "currency");
        TextField ownedBy = VaadinUtils.createTextField("Owned By", "owned by");
        VaadinUtils.readOnly(operation, id, name, kind, group, currency, ownedBy);

        binder = new Binder<>();
        binder.bind(id, Account::id, null);
        binder.bind(name, Account::name, null);
        binder.bind(kind, Account::kind, null);
        binder.bind(group, Account::group, null);
        binder.bind(currency, o -> o.currency().getCurrencyCode(), null);
        binder.bind(ownedBy, Account::ownedBy, null);
        form.add(id, name, kind, group, currency, ownedBy);
        if (entity != null) {
            binder.readBean(entity);
        }
        return form;
    }

    @Override
    protected Account updateOrCreate(Account entity) {
        return null;
    }
}
