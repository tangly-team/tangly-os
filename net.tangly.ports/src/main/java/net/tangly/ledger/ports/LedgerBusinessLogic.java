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
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import net.tangly.bus.ledger.Ledger;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Define business logic rules and functions for the ledger double entry accounting domain model.
 */
public class LedgerBusinessLogic {
    private static final String TURNOVER_ACCOUNT = "3";
    private static final String EBIT_ACCOUNT = "E4";
    private static final String EARNINGS_ACCOUNT = "E7";
    private static final String ASCII_DOC_EXT = ".adoc";
    private static final String PDF_EXT = ".pdf";

    private final Ledger ledger;

    public LedgerBusinessLogic(@NotNull Ledger ledger) {
        this.ledger = ledger;
    }

    public Ledger ledger() {
        return ledger;
    }

    public void createLedgerReport(@NotNull Path directory, String filenameWithoutExtension, LocalDate from, LocalDate to) {
        ClosingReportAsciiDoc report = new ClosingReportAsciiDoc(ledger);
        report.create(from, to, directory.resolve(filenameWithoutExtension + ASCII_DOC_EXT));
        createPdf(directory, filenameWithoutExtension);
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
                LocalDate.parse("2019-12-31"));
    }

    private BigDecimal accountChangeInTime(@NotNull String accountId, @NotNull LocalDate from, @NotNull LocalDate to) {
        return ledger.accountBy(accountId).map(o -> o.balance(to).subtract(o.balance(from)).negate()).orElse(BigDecimal.ZERO);
    }

    public static void createPdf(@NotNull Path directory, @NotNull String filenameWithoutExtension) {
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create();
             OutputStream out = Files.newOutputStream(directory.resolve(filenameWithoutExtension + PDF_EXT))) {
            String asciidoc = Files.readString(directory.resolve(filenameWithoutExtension + ASCII_DOC_EXT));
            Attributes attributes = AttributesBuilder.attributes().get();
            Options options = OptionsBuilder.options().inPlace(true).attributes(attributes).backend("pdf").toStream(out).get();
            asciidoctor.convert(asciidoc, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
