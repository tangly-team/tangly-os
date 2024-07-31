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

import java.util.List;
import java.util.Optional;

/**
 * The business logic of the application is responsible for the processing of the business rules and the orchestration of the domain objects.
 */
public class AppsBusinessLogic {
    private final AppsRealm realm;

    public AppsBusinessLogic(@NotNull AppsRealm realm) {
        this.realm = realm;
    }

    public AppsRealm realm() {
        return realm;
    }


    /**
     * Returns the list of active users for a domain.
     *
     * @param domain the domain for which the active users are requested
     * @return the list of active users for the domain
     */
    public List<String> activeUsersFor(String domain) {
        return realm().users().items().stream().filter(o -> o.active() && o.accessRightsFor(domain).isPresent()).map(User::username).toList();
    }

    /**
     * Returns the list of users for a domain.
     *
     * @param domain the domain for which the users are requested
     * @return the list of users for the domain
     */
    public List<String> usersFor(String domain) {
        return realm().users().items().stream().filter(o -> o.accessRightsFor(domain).isPresent()).map(User::username).toList();
    }

    /**
     * Logins a user identified by its username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return the user if the login is successful
     */
    public Optional<User> login(@NotNull String username, @NotNull String password) {
        return realm().users().items().stream().filter(o -> o.username().equals(username) && o.authenticate(password)).findAny();
    }

    /**
     * Changes the password of a user identified by its username and password.
     *
     * @param username    the username of the user
     * @param password    the current password of the user
     * @param newPassword the new password of the user
     * @return true if the password is changed successfully
     */
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
