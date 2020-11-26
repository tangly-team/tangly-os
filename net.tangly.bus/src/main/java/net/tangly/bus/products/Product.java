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
package net.tangly.bus.products;

import java.util.Collections;
import java.util.List;

import net.tangly.core.QualifiedEntityImp;
import org.jetbrains.annotations.NotNull;

/**
 * An outcome such as a product or a research result which should be produced. An outcome is financed through one or more contracts.
 */
public class Product extends QualifiedEntityImp {
    private List<String> contractIds;

    public Product() {
        contractIds = Collections.emptyList();
    }

    public List<String> contractIds() {
        return contractIds;
    }

    public void contractIds(@NotNull List<String> contractIds) {
        this.contractIds = List.copyOf(contractIds);
    }

    public boolean check() {
        return (contractIds().size() > 0);
    }

    @Override
    public String toString() {
        return """
            Product[oid=%s, id=%s, fromDate=%s, toDate=%s, text=%s, contractIds=%s, tags=%s]
            """.formatted(oid(), id(), fromDate(), toDate(), text(), contractIds(), tags());
    }
}
