/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ports;

import net.tangly.erp.crm.services.CrmPort;
import net.tangly.erp.crm.services.CrmRealm;
import org.jetbrains.annotations.NotNull;

public class CrmAdapter implements CrmPort {
    private final CrmRealm realm;

    public CrmAdapter(@NotNull CrmRealm realm) {
        this.realm = realm;
    }
}
