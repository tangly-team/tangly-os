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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.commons.utilities.AsciiDoctorHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Define business logic rules and functions for the ledger double entry accounting domain model.
 */
public class LedgerPort {
    public static final String TURNOVER_ACCOUNT = "3";
    public static final String EBIT_ACCOUNT = "E4";
    public static final String EARNINGS_ACCOUNT = "E7";
    public static final String SHORT_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "20";
    public static final String LONG_TERM_THIRD_PARTY_CAPITAL_ACCOUNT = "2A";
    public static final String EQUITY_ACCOUNT = "28";
    public static final String CASH_ON_HAND_ACCOUNT = "100";

    private final LedgerRealm ledger;
    private final Path folder;

    public LedgerPort(@NotNull LedgerRealm ledger, @NotNull Path folder) {
        this.ledger = ledger;
        this.folder = folder;
    }

    public LedgerRealm ledger() {
        return ledger;
    }

    public void createLedgerReport(String filenameWithoutExtension, LocalDate from, LocalDate to) {
        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(ledger);
        report.create(from, to, folder.resolve(filenameWithoutExtension + AsciiDoctorHelper.ASCIIDOC_EXT));
        AsciiDoctorHelper.createPdf(folder.resolve(filenameWithoutExtension + AsciiDoctorHelper.ASCIIDOC_EXT),
            folder.resolve(filenameWithoutExtension + AsciiDoctorHelper.PDF_EXT));
        try {
            Files.delete(folder.resolve(filenameWithoutExtension + AsciiDoctorHelper.ASCIIDOC_EXT));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
