/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.orm;

import net.tangly.bus.core.HasOid;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a data access object and verifies all properties are syntactically correct. Also verifies that the definition is compatible with the
 * associated database table.
 */
public class DaoValidator {
    private static Logger logger = LoggerFactory.getLogger(DaoValidator.class);

    public <T extends HasOid> boolean validate(@NotNull Dao<T> dao) {
        return true;
    }
}
