/*
 * Copyright 2021-2024 Marcel Baumann
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

package net.tangly.erp.agile.model;

import net.tangly.core.codes.Code;

public enum StoryCode implements Code {
    draft, refined, ready, planned, inWork, done, blocked;

    @Override
    public int id() {
        return this.ordinal();
    }

    @Override
    public String code() {
        return this.name();
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
