/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.erp.collabortors.services;

import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.TenantDirectory;
import org.jetbrains.annotations.NotNull;

public class CollaboratorsBoundedDomain extends BoundedDomain<CollaboratorsRealm, CollaboratorsBusinessLogic, CollaboratorsPort> {
    public static final String DOMAIN = "collaborators";

    public CollaboratorsBoundedDomain(@NotNull CollaboratorsRealm realm, @NotNull CollaboratorsBusinessLogic logic, @NotNull CollaboratorsPort port,
                                      TenantDirectory directory) {
        super(DOMAIN, realm, logic, port, directory);
    }
}
