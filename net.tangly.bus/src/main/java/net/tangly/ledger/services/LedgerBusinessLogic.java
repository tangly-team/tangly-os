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

package net.tangly.ledger.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.inject.Inject;

import net.tangly.ledger.domain.Account;
import org.jetbrains.annotations.NotNull;

public class LedgerBusinessLogic {
    public static final String TURNOVER_ACCOUNT = "3";
    public static final String EBIT_ACCOUNT = "E4";
    public static final String EARNINGS_ACCOUNT = "E7";
    public static final String SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "20";
    public static final String LONG_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "2A";
    public static final String EQUITY_ACCOUNT = "28";
    public static final String CASH_ON_HAND_ACCOUNT = "100";

    private final LedgerRealm ledger;

    @Inject
    public LedgerBusinessLogic(@NotNull LedgerRealm ledger) {
        this.ledger = ledger;
    }

    public LedgerRealm ledger() {
        return ledger;
    }

    public BigDecimal turnover(@NotNull LocalDate from, @NotNull LocalDate to) {
        return accountChangeInTime(TURNOVER_ACCOUNT, from, to);
    }

    public BigDecimal ebit(@NotNull LocalDate from, @NotNull LocalDate to) {
        return accountChangeInTime(EBIT_ACCOUNT, from, to);
    }

    public BigDecimal earnings(@NotNull LocalDate from, @NotNull LocalDate to) {
        return accountChangeInTime(EARNINGS_ACCOUNT, from, to);
    }

    /**
     * Returns the change of the selected account during the specified time interval.
     *
     * @param accountId identifier of the account which change shall be computed
     * @param from      start of the time interval
     * @param to        end of the time interval
     * @return change in the account over the time interval if found otherwise zero
     */
    public BigDecimal accountChangeInTime(@NotNull String accountId, @NotNull LocalDate from, @NotNull LocalDate to) {
        return ledger.accountBy(accountId).map(o -> o.balance(to).subtract(o.balance(from)).negate()).orElse(BigDecimal.ZERO);
    }

    /**
     * Returns the balance of the selected account at the given date.
     *
     * @param accountId identifier of the account which change shall be computed
     * @param date      date of the account state
     * @return the account state if found otherwise zero
     */
    public BigDecimal balance(@NotNull String accountId, @NotNull LocalDate date) {
        return ledger.accountBy(accountId).map(o -> o.balance(date)).orElse(BigDecimal.ZERO);
    }

    public List<String> bookableAccountIds() {
        return ledger().bookableAccounts().stream().map(Account::id).toList();
    }
}
