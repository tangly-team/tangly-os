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

package net.tangly.erp.ledger.ui;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.ui.components.ItemForm;
import net.tangly.ui.components.ItemView;
import net.tangly.ui.components.Mode;
import net.tangly.ui.components.VaadinUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Charts of accounts are defined externally as TSV files. Currently the account view is a read-only view on accounts instances.
 */
@PageTitle("ledger-accounts")
class AccountsView extends ItemView<Account> {
    static class AccountFilter extends ItemFilter<Account> {
        String name;
        String group;
        String id;

        AccountFilter() {
        }

        void name(String name) {
            this.name = name;
        }

        void group(String group) {
            this.group = group;
        }

        void id(String id) {
            this.id = id;
        }

        public boolean test(@NotNull Account entity) {
            return ItemFilter.matches(entity.name(), name) && ItemFilter.matches(entity.group().name(), group) && ItemFilter.matches(entity.id(), id);
        }
    }

    static class AccountForm extends ItemForm<Account, AccountsView> {
        AccountForm(@NotNull AccountsView view) {
            super(view);
            init();
        }

        private void init() {
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

            binder().bind(id, Account::id, null);
            binder().bind(name, Account::name, null);
            binder().bind(kind, Account::kind, null);
            binder().bind(group, Account::group, null);
            binder().bind(currency, o -> o.currency().getCurrencyCode(), null);
            binder().bind(ownedBy, Account::ownedBy, null);
            FormLayout form = new FormLayout();
            form.add(id, name, kind, group, currency, ownedBy);
            addTabAt("details", form, 0);
        }

        @Override
        public void value(Account value) {
            if (value != null) {
                binder().readBean(value);
            }
        }

        @Override
        public void clear() {
        }

        @Override
        protected Account createOrUpdateInstance(Account entity)  {
            return null;
        }
    }

    private Binder<Account> binder;

    public AccountsView(@NotNull LedgerBoundedDomainUi domain, @NotNull Mode mode) {
        super(Account.class, domain, domain.domain().realm().accounts(), new AccountFilter(), mode);
        form(() -> new AccountForm(this));
        init();
    }

    private void init() {
        var grid = grid();
        grid.addColumn(Account::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::group).setKey("group").setHeader("Group").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::id).setKey("id").setHeader("Id").setAutoWidth(true).setResizable(true);
        //        grid.addColumn(VaadinUtils.coloredRender(o -> o.balance(from), VaadinUtils.FORMAT)).setKey("opening").setHeader("Opening").setAutoWidth(true).setResizable(true)
        //            .setTextAlign(ColumnTextAlign.END);
        //        grid.addColumn(VaadinUtils.coloredRender(o -> o.balance(to), VaadinUtils.FORMAT)).setKey("balance").setHeader("Balance").setAutoWidth(true).setResizable(true)
        //            .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Account::kind).setKey("kind").setHeader("Kind").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::currency).setKey("currency").setHeader("Currency").setAutoWidth(true).setResizable(true);
        grid.addColumn(Account::ownedBy).setKey("ownedBy").setHeader("Owned By").setAutoWidth(true).setResizable(true);
    }
}
