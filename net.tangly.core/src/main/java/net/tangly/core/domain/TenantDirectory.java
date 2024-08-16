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

import net.tangly.core.HasId;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface TenantDirectory extends HasId {
    String TENANT_ID_PROPERTY = "tenant.id";
    String TENANT_ORGANIZATION_NAME_PROPERTY = "tenant.organization.name";
    String TENANT_ORGANIZATION_ID_PROPERTY = "tenant.organization.id";
    String TENANT_ORGANIZATION_LANGUAGE_PROPERTY = "tenant.organization.language";
    String TENANT_ORGGANIZATION_DATE_FORMAT_PROPERTY = "tenant.organization.date.format";
    String TENANT_ORGANIZATION_ADDRESS_PROPERTY = "tenant.organization.address";

    /**
     * Returns the bounded domain associated with the domain name in this tenant.
     *
     * @param domain name of the bounded domain
     * @return bounded domain associated with the domain name
     */
    Optional<BoundedDomain<?, ?, ?>> getBoundedDomain(@NotNull String domain);

    /**
     * Returns the requested property from the tenant directory.
     *
     * @param property name of the property
     * @return value of the property
     */
    String getProperty(String property);

    /**
     * Returns the list of users available in the domain.
     *
     * @param domain name of the bounded domain
     * @return list of users
     */
    List<String> users(@NotNull String domain);

    /**
     * Returns the list of active users in the domain.
     *
     * @param
     * @return list of active users
     */
    List<String> activeUsers(@NotNull String domain);
}
