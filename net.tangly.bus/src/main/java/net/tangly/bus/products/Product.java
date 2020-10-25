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

import java.util.List;

import net.tangly.bus.core.QualifiedEntityImp;
import org.jetbrains.annotations.NotNull;

/**
 * An outcome such as a product or a research result which should be produced. An outcome is financed through one or more contracts.
 */
public class Product extends QualifiedEntityImp {
    private List<String> contracIds;

    public List<String> contractIds() {
        return contracIds;
    }

    public void contracIds(@NotNull List<String> contractIds) {
        this.contracIds = List.copyOf(contracIds);
    }
}
