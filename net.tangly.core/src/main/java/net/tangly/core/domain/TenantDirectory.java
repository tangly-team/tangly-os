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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The tenant directory provides access to the bounded domains and the properties of a tenant.
 * The tenant directory is the entry point when a bounded domain needs to interact with other bounded domains in our modular monolith design approach.
 * It is equivalent to a DNS server in a distributed microservice architecture. The provided services are:
 * <dl>
 *     <dt>Bounded domain registry</dt><dd>Provides access to the bounded domains in the tenant based on their domain name.</dd>
 *     <dt>User registry</dt><dd>Provides access to the active users having access to a bounded domain based on the domain name. A list of all users is
 *     also available for historical display functions. For example, a disabled user can still be the creator of an entity in a domain.</dd>
 *     <dt>Property registry</dt><dd>Simple approach to provide tenant specific information and configuration to all bounded domain. For example, the default
 *     organization date format can be defined through this mechanism.</dd>
 * </dl>
 * <p>For privacy and security reasons, no communication between tenants is supported.</p>
 */
public interface TenantDirectory extends HasId {
    String TENANT_ID_PROPERTY = "tenant.id";
    String TENANT_ORGANIZATION_NAME_PROPERTY = "tenant.organization.name";
    String TENANT_ORGANIZATION_ID_PROPERTY = "tenant.organization.id";
    String TENANT_ORGANIZATION_LANGUAGE_PROPERTY = "tenant.organization.language";
    String TENANT_ORGANIZATION_DATE_FORMAT_PROPERTY = "tenant.organization.date.format";
    String TENANT_ORGANIZATION_ADDRESS_PROPERTY = "tenant.organization.address";

    /**
     * Returns the bounded domains available in the tenant.
     *
     * @return bounded domains available in the tenant
     */
    Collection<BoundedDomain<?, ?, ?>> boundedDomains();

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
    String getProperty(@NotNull String property);

    /**
     * Returns the list of registered users available in the domain. A registered user is a user that has been once active in the domain.
     *
     * @param domain name of the bounded domain
     * @return list of users
     */
    List<String> usersFor(@NotNull String domain);

    /**
     * Returns the list of active users in the domain.
     *
     * @param domain name of the bounded domain
     * @return list of active users
     */
    List<String> activeUsersFor(@NotNull String domain);

    /**
     * Returns the path to the documents folder of the domain.
     *
     * @param domain name of the bounded domain
     * @return path to the documents folder of the domain
     */
    String docs(@NotNull String domain);

    /**
     * Returns the path to the resources folder of the domain.
     *
     * @param domain name of the bounded domain
     * @return path to the resources folder of the domain
     */
    String resources(@NotNull String domain);
}
