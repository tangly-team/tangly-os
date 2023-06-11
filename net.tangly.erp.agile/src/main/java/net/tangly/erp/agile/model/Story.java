/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.agile.model;

import net.tangly.core.ExternalEntity;
import net.tangly.core.ExternalEntityImp;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class Story extends ExternalEntityImp implements ExternalEntity {
    private int priority;
    private int estimate;
    private BigDecimal effort;
    private String featureId;
    private String sprintId;

    public Story(@NotNull String id) {
        super(id);
    }
}
