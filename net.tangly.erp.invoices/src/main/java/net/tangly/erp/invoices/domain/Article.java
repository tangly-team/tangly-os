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

package net.tangly.erp.invoices.domain;


import net.tangly.core.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * A product sold by a legal entity and referenced in an invoice or an invoice line. An article is a {@link HasMutableId} mixin because the identifier is external and the Java record
 * structure does not support internal {@link HasOid} manipulation through reflection.
 *
 * @param id        unique external identifier of the product
 * @param name      human-readable name of the product
 * @param text      human-readable description of the product
 * @param code      code of the product
 * @param unitPrice unit price of the product
 * @param unit      unit type of the product such as day, hour,or fix for a workshop
 */
public record Article(@NotNull String id, String name, String text, @NotNull ArticleCode code, @NotNull BigDecimal unitPrice,
                      String unit) implements HasId, HasName, HasText {
}
