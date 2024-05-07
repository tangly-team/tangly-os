/*
 * Copyright 2006-2024 Marcel Baumann
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
package net.tangly.erp.products.domain;

import net.tangly.core.EntityExtendedImp;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An outcome such as a product or a research result which should be produced. An outcome is financed through one or more contracts.
 */
public class Product extends EntityExtendedImp {
    private List<String> contractIds;

    public Product(long oid) {
        super(oid);
        contractIds = Collections.emptyList();
    }

    public List<String> contractIds() {
        return Collections.unmodifiableList(contractIds);
    }

    public void contractIds(@NotNull List<String> contractIds) {
        this.contractIds = List.copyOf(contractIds);
    }

    @Override
    public boolean validate() {
        return !contractIds().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Product o) && super.equals(o) && Objects.equals(id(), o.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id());
    }

    @Override
    public String toString() {
        return """
            Product[oid=%s, id=%s, fromDate=%s, toDate=%s, text=%s, contractIds=%s, tags=%s]
            """.formatted(oid(), id(), from(), to(), text(), contractIds(), tags());
    }
}
