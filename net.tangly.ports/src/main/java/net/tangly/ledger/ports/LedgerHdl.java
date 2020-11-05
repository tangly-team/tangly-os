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
import java.util.stream.Stream;
import javax.inject.Inject;

import net.tangly.bus.ledger.LedgerRealm;
import net.tangly.bus.ledger.LedgerHandler;
import org.jetbrains.annotations.NotNull;

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

    public LedgerRealm ledger() {
        return ledger;
    }

    @Override
    public void importEntities() {
        LedgerTsvHdl handler = new LedgerTsvHdl(ledger);
        handler.importLedgerStructureFromBanana(folder.resolve("swiss-ledger.tsv"));
        ledger.build();
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith("-period.tsv"))
                    .forEach(handler::importTransactionsLedgerFromBanana);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void exportEntities() {
    }
}
