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

import net.tangly.app.services.AppsRealm;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.User;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

public class AppsTsvHdl {
    public static final String USERS_TSV = "users.tsv";
    public static final String ACCESS_RIGHTS_TSV = "access-rights.tsv";

    private final AppsRealm realm;

    public AppsTsvHdl(@NotNull AppsRealm realm) {
        this.realm = realm;
    }

    public void importProducts(@NotNull Reader reader, String source) {
        // TsvHdl.importEntities(AppsBoundedDomain.DOMAIN, reader, source, createTsvUser(), realm.users());
    }

    public void exportProducts(@NotNull Path path) {
        // TsvHdl.exportEntities(ProductsBoundedDomain.DOMAIN, path, createTsvProduct(), realm.products());
    }

    private static TsvEntity<User> createTsvUser() {
        List<TsvProperty<User, ?>> fields = List.of(
            TsvProperty.ofString("username", User::username),
            TsvProperty.ofString("passwordHash", User::passwordHash),
            TsvProperty.ofString("passwordSalt", User::passwordSalt),
            TsvProperty.ofString("gravatarEmail", User::gravatarEmail));
        // return TsvEntity.of(User.class, fields, Product::new);
        return null;
    }
    private static TsvEntity<AccessRights> createTsvAccessRights() {
        List<TsvProperty<AccessRights, ?>> fields = List.of(
            TsvProperty.ofString("username", AccessRights::username),
            TsvProperty.ofString("domain", AccessRights::domain));
        return null;
        // return TsvEntity.of(AccessRights.class, fields, AccessRights::new);
    }

}
