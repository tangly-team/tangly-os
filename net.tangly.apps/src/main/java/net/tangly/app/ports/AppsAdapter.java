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

package net.tangly.app.ports;

import net.tangly.app.services.AppsPort;
import net.tangly.app.services.AppsRealm;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AppsAdapter implements AppsPort {
    public static final String USERS_TSV = "users.tsv";
    public static final String ACCESS_RIGHTS_TSV = "access-rights.tsv";

    private final AppsRealm realm;

    private final Path dataFolder;

    public AppsAdapter(@NotNull AppsRealm realm, @NotNull Path dataFolder) {
        this.realm = realm;
        this.dataFolder = dataFolder;
    }

    @Override
    public AppsRealm realm() {
        return realm;
    }

    @Override
    public void importEntities() {
        var handler = new AppsTsvHdl(realm());
        try (var usersReader = Files.newBufferedReader(dataFolder.resolve(USERS_TSV)); var accessRightsReader = Files.newBufferedReader(
            dataFolder.resolve(ACCESS_RIGHTS_TSV))) {
            handler.importUsers(usersReader, accessRightsReader, dataFolder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exportEntities() {
        var handler = new AppsTsvHdl(realm());
        handler.exportUsers(dataFolder.resolve(USERS_TSV), dataFolder.resolve(ACCESS_RIGHTS_TSV));
    }

    @Override
    public void clearEntities() {
        realm().users().deleteAll();
    }
}
