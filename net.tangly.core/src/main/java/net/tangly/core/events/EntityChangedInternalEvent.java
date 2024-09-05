/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.core.events;

import net.tangly.core.domain.Operation;
import org.jetbrains.annotations.NotNull;

/**
 * Event published when an entity is changed in the domain programatically. This change was not performed through the user interface. The event is used to
 * propagate changes in the domain to the user interface.
 *
 * @param domain     name of the domain where the entity was changed
 * @param entityName should be the simple name of the entity class {@link Class#getSimpleName()}
 * @param operation  operation performed on the entity
 */
public record EntityChangedInternalEvent(@NotNull String domain, @NotNull String entityName, @NotNull Operation operation) {
}
