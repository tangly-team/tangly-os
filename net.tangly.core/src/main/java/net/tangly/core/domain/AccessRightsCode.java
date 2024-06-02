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

import net.tangly.core.codes.Code;

/**
 * A user has different access rights:
 * <dl>
 *     <dt>restricted-user</dt><dd>has anly read and write access to owned entities in a domain. Ownership is identified through the username of the
 *     logged-in user.</dd>
 *     <dt>readonly user</dt><dd>has read access to all entities in a domain. No edition functions are available.</dd>
 *     <dt>user</dt> <dd>has write access to all entities in a domain. The user can create, modify, delete and duplify aggregates.</dd>
 *     <dt>domain-admin</dt><dd>has write access to all entities in a domain and access to specific domain actions. This right if necessary to import and
 *     export datasets of the domain.</dd>
 *     <dt>app-admin</dt><dd>has access to application specific data and function. She does not have access to damains.</dd>
 * </dl>
 */
public enum AccessRightsCode implements Code {
    none, restrictedUser, readonlyUser, user, domainAdmin, appAdmin;

    @Override
    public int id() {
        return this.ordinal();
    }

    @Override
    public String code() {
        return this.name();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
