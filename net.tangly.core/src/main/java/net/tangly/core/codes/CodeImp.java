/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.core.codes;

import java.util.Objects;

/**
 * The class represents an immutable code describing one format of a set of related codes aggregated in the same code table. The protected default constructor
 * is necessary for JSON parsing of subclasses if no annotations are used to select a non-default constructor.
 */
public abstract class CodeImp implements Code {
    /**
     * Identifier of the code in the context of the code table.
     */
    private final int id;

    /**
     * Human readable format of the code.
     */
    private final String code;

    /**
     * Flag indicating if the code is enabled or not.
     */
    private final boolean enabled;

    /**
     * Constructor of the immutable class.
     *
     * @param id      code identifier of the instance
     * @param code    human readable code text of the instance
     * @param enabled flag indicating if the code is enabled
     */
    protected CodeImp(int id, String code, boolean enabled) {
        this.id = id;
        this.code = code;
        this.enabled = enabled;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (getClass() == obj.getClass()) && Objects.equals(id, ((Code) obj).id());
    }

    @Override
    public String toString() {
        return String.format("{id=%d, code=%s, enabled=%b}", id, code, enabled);
    }
}
