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

package net.tangly.app.services;

import net.tangly.core.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AppsBusinessLogic {
    private final AppsRealm realm;

    public AppsBusinessLogic(@NotNull AppsRealm realm) {
        this.realm = realm;
    }

    public AppsRealm realm() {
        return realm;
    }

    public Optional<User> login(String username, String password) {
        return realm().users().items().stream().filter(o -> o.username().equals(username) && o.authenticate(password)).findAny();
    }

    public boolean changePassword(String username, String password, String newPassword) {
        var user = realm().users().items().stream().filter(o -> o.username().equals(username) && o.authenticate(password)).findAny();
        user.ifPresent(o -> {
            String passwordHash = User.encryptPassword(newPassword, o.passwordSalt());
            var updatedUser = new User(o.username(), passwordHash, o.passwordSalt(), o.active(), o.naturalPersonId(), o.accessRights(), o.gravatarEmail());
            realm().users().replace(o, updatedUser);
        });
        return user.isPresent();
    }
}
