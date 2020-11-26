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
import java.util.Map;
import java.util.stream.Stream;
import javax.inject.Inject;

import net.tangly.bus.ledger.LedgerHandler;
import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.commons.logger.EventData;
import org.jetbrains.annotations.NotNull;

import static net.tangly.ports.TsvHdl.MODULE;

/**
 * Provide workflows for ledger activities.
 * <ul>
 *     <li>Import of the ledger account structure. If using the <a href="https://www.banan.ch">banana</a> application, select all definition rows in
 *     the accounts tab and export it as <i>Data/Export Rows/Export Rows to Txt</i>. Once completed you can use for example MacOs Numbers to remove company
 *     specific information such as segments.</li>
 *     <li>Import transaction journal into the ledger. If using the <a href="https://www.banan.ch">banana</a> application, select all transaction rows in
 *     the accounts tab and export it as <i>Data/Export Rows/Export Rows to Txt</i>. </li>
 * </ul>
 */
public class LedgerHdl implements LedgerHandler {
    private final LedgerRealm ledger;
    private final Path folder;

    @Inject
    public LedgerHdl(@NotNull LedgerRealm ledger, @NotNull Path folder) {
        this.ledger = ledger;
        this.folder = folder;
    }

    public LedgerRealm realm() {
        return ledger;
    }

    @Override
    public void importEntities() {
        var handler = new LedgerTsvHdl(ledger);
        var chartOfAccounts = folder.resolve("swiss-ledger.tsv");
        handler.importChartOfAccounts(chartOfAccounts);
        ledger.build();
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith("-period.tsv")).forEach(o -> {
                handler.importJournal(o);
                EventData.log(EventData.IMPORT, MODULE, EventData.Status.SUCCESS, "Journal imported {}", Map.of("journalPath", o));
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities() {
        var handler = new LedgerTsvHdl(ledger);
        handler.exportChartOfAccounts(folder.resolve("swiss-ledger.tsv"));
        handler.exportJournal(folder.resolve("journal.tsv"), null, null);
    }
}
