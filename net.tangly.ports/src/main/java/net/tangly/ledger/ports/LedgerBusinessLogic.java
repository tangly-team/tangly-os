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

package net.tangly.ledger.ports;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import net.tangly.bus.ledger.Ledger;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Define business logic rules and functions for the ledger double entry accounting domain model.
 */
public class LedgerBusinessLogic {
    public static final String TURNOVER_ACCOUNT = "3";
    public static final String EBIT_ACCOUNT = "E4";
    public static final String EARNINGS_ACCOUNT = "E7";
    public static final String SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "20";
    public static final String LONG_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "2A";
    public static final String EQUITY_ACCOUNT = "28";
    public static final String CASH_ON_HAND_ACCOUNT = "100";

    private final Ledger ledger;

    public LedgerBusinessLogic(@NotNull Ledger ledger) {
        this.ledger = ledger;
    }

    public Ledger ledger() {
        return ledger;
    }

    public void createLedgerReport(@NotNull Path directory, String filenameWithoutExtension, LocalDate from, LocalDate to) {
        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(ledger);
        report.create(from, to, directory.resolve(filenameWithoutExtension + AsciiDoctorHelper.ASCII_DOC_EXT));
        AsciiDoctorHelper.createPdf(directory, filenameWithoutExtension);
        try {
            Files.delete(directory.resolve(filenameWithoutExtension + AsciiDoctorHelper.ASCII_DOC_EXT));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    public List<LocalDate> quarterLegends(LocalDate from, LocalDate to) {
        return List.of(LocalDate.parse("2015-12-31"), LocalDate.parse("2016-03-31"), LocalDate.parse("2016-06-30"), LocalDate.parse("2016-09-30"),
                LocalDate.parse("2016-12-31"), LocalDate.parse("2017-03-31"), LocalDate.parse("2017-06-30"), LocalDate.parse("2017-09-30"),
                LocalDate.parse("2017-12-31"), LocalDate.parse("2018-03-31"), LocalDate.parse("2018-06-30"), LocalDate.parse("2018-09-30"),
                LocalDate.parse("2018-12-31"), LocalDate.parse("2019-03-31"), LocalDate.parse("2019-06-30"), LocalDate.parse("2019-09-30"),
                LocalDate.parse("2019-12-31"), LocalDate.parse("2020-03-31"), LocalDate.parse("2020-06-30"), LocalDate.parse("2020-09-30"),
                LocalDate.parse("2020-12-31"));
    }

    /**
     * Return the change of the selected account during the specified time interval.
     *
     * @param accountId identifier of the account which change shall be computed
     * @param from      start of the time interval
     * @param to        end of the itme interval
     * @return change in the account over the time interval if found otherwise zero
     */
    public BigDecimal accountChangeInTime(@NotNull String accountId, @NotNull LocalDate from, @NotNull LocalDate to) {
        return ledger.accountBy(accountId).map(o -> o.balance(to).subtract(o.balance(from)).negate()).orElse(BigDecimal.ZERO);
    }

    /**
     * Return the balance of the selected account at the given date.
     *
     * @param accountId identifier of the account which change shall be computed
     * @param date      date of the account state
     * @return the account state if found otherwise zero
     */
    public BigDecimal balance(@NotNull String accountId, @NotNull LocalDate date) {
        return ledger.accountBy(accountId).map(o -> o.balance(date)).orElse(BigDecimal.ZERO);
    }
}
