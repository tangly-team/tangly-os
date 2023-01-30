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

package net.tangly.core;

import java.util.Objects;

/**
 * Default implementation of the Entity interface.
 *
 * @see QualifiedEntity
 */
public abstract class NamedEntityImp extends EntityImp implements NamedEntity {
    private String name;

    // region HasName

    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    // endregion

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NamedEntityImp o) && super.equals(obj) && Objects.equals(name(), o.name());
    }
}
