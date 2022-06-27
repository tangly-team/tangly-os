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

package net.tangly.erp.products.services;

import net.tangly.erp.products.domain.Assignment;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Defines the export port for the products bounded domain. It is the secondary port in the DDD terminology.
 */
public interface ProductsPort {
    /**
     * Exports all efforts of the assignment in the given period of time. The export shall be a asciidoc document.
     * @param assignment assignment which efforts are of interest
     * @param from start of the considered time interval
     * @param to end of the considered time interval
     */
    void exportEffortsDocument(@NotNull Assignment assignment, LocalDate from, LocalDate to);
}
