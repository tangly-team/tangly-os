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

import net.tangly.app.services.AppsBoundedDomain;
import net.tangly.app.services.AppsRealm;
import net.tangly.core.domain.AccessRights;
import net.tangly.core.domain.AccessRightsCode;
import net.tangly.core.domain.TsvHdl;
import net.tangly.core.domain.User;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static net.tangly.gleam.model.TsvEntity.get;

public class AppsTsvHdl {
    public static final String USERNAME = "username";
    public static final String PASSWORD_HASH = "passwordHash";
    public static final String PASSWORD_SALT = "passwordSalt";
    public static final String ACTIVE = "active";
    public static final String GRAVATAR_EMAIL = "gravatarEmail";
    public static final String NATURAL_ENTITY_ID = "naturalEntityId";
    public static final String RIGHTS = "rights";

    private final AppsRealm realm;

    public AppsTsvHdl(@NotNull AppsRealm realm) {
        this.realm = realm;
    }

    public void importUsers(@NotNull Reader usersReader, @NotNull Reader accessRightsReader, @NotNull String source) {
        ProviderInMemory<AccessRights> rights = new ProviderInMemory<>();
        TsvHdl.importEntities(AppsBoundedDomain.DOMAIN, accessRightsReader, source, createTsvAccessRights(), rights);
        TsvHdl.importEntities(AppsBoundedDomain.DOMAIN, usersReader, source, createTsvUser(rights.items()), realm.users());
    }

    public void exportUsers(@NotNull Path usersPath, @NotNull Path accessRightsPath) {
        var rights = ProviderInMemory.of(realm.users().items().stream().flatMap(o -> o.accessRights().stream()).toList());
        TsvHdl.exportEntities(AppsBoundedDomain.DOMAIN, usersPath, createTsvUser(rights.items()), realm.users());
        TsvHdl.exportEntities(AppsBoundedDomain.DOMAIN, accessRightsPath, createTsvAccessRights(), rights);
    }

    private static TsvEntity<User> createTsvUser(@NotNull List<AccessRights> rights) {
        Function<CSVRecord, User> imports = (CSVRecord o) -> {
            var username = get(o, USERNAME);
            var accessRights = accessRights(rights, username);
            return new User(username, get(o, PASSWORD_HASH), get(o, PASSWORD_SALT), true, get(o, NATURAL_ENTITY_ID), accessRights, get(o, GRAVATAR_EMAIL));
        };
        List<TsvProperty<User, ?>> fields = List.of(TsvProperty.ofString(USERNAME, User::username), TsvProperty.ofString(PASSWORD_HASH, User::passwordHash),
            TsvProperty.ofString(PASSWORD_SALT, User::passwordSalt), TsvProperty.ofBoolean(ACTIVE, User::active),
            TsvProperty.ofString(NATURAL_ENTITY_ID, User::naturalPersonId), TsvProperty.ofString(GRAVATAR_EMAIL, User::gravatarEmail));
        return TsvEntity.of(User.class, fields, imports);
    }

    private static TsvEntity<AccessRights> createTsvAccessRights() {
        Function<CSVRecord, AccessRights> imports = (CSVRecord o) -> new AccessRights(get(o, USERNAME), get(o, TsvHdl.DOMAIN),
            TsvHdl.parseEnum(o, RIGHTS, AccessRightsCode.class));
        List<TsvProperty<AccessRights, ?>> fields = List.of(TsvProperty.ofString(USERNAME, AccessRights::username),
            TsvProperty.ofString(TsvHdl.DOMAIN, AccessRights::domain), TsvProperty.ofEnum(AccessRightsCode.class, RIGHTS, AccessRights::right));
        return TsvEntity.of(AccessRights.class, fields, imports);
    }

    private static List<AccessRights> accessRights(@NotNull List<AccessRights> rights, String username) {
        return rights.stream().filter(o -> o.username().equals(username)).toList();
    }
}
