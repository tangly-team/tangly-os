/*
 * Copyright 2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.gleam.model;

import org.jetbrains.annotations.NotNull;

/**
 * Model an one to multiple relation.
 *
 * @param ownerId     identifier of the owner entity. It is stored as a foreign key in the TSV file
 * @param ownedEntity the entity owned through the relation
 * @param <T>         class of the owned entity
 */
public record TsvRelation<T>(long ownerId, @NotNull T ownedEntity) {
}
