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

package net.tangly.core.domain;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Defines a provider for the list of users and the list of active users for a domain.
 * This approach isolates a bounded domain from the implementation of the user management system.
 * The domain has no knowledge of users defined in other domains in the application.
 */
public interface UsersProvider {
    static UsersProvider of(@NotNull Supplier<List<String>> users, @NotNull Supplier<List<String>> activeUsers) {
        record Provider(Supplier<List<String>> usersProvider, Supplier<List<String>> activeUsersProvider) implements UsersProvider {
            @Override
            public List<String> users() {
                return usersProvider.get();
            }

            @Override
            public List<String> activeUsers() {
                return activeUsersProvider.get();
            }
        }
        return new Provider(users, activeUsers);
    }

    /**
     * Returns the list of users available in the domain.
     * @return list of users
     */
    List<String> users();

    /**
     * Returns the list of active users in the domain.
     * @return list of active users
     */
    List<String> activeUsers();
}
