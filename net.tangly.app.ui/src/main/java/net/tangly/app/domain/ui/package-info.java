/*
 * Copyright 2023 Marcel Baumann
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

/**
 * Define a demonstration application for the domain-driven design open-source libraries. The demonstration creates domain for:
 * <dl>
 * <dt>Items</dt>
 * <dd>Items are plain old Java objects. No restrictions exist. An item does not need to implement any interface or inherit from a class.
 * One entity is visualized as editable. Another one is visualized as read-only values.</dd>
 * <dt>Simple Entities</dt>
 * <dd>Simple entities have an internal identifier, an external identifier, a human-readable name, a textual description, and validity time interval.
 * One entity is visualized as editable. Another one is visualized as read-only values.</dd>
 * <dt>Full Entities</dt>
 * <dd>Full entities are simple entities with an additional set of tags and a set of comments.
 * One entity is visualized as editable. Another one is visualized as read-only values.</dd>
 * </dl>
 */

package net.tangly.app.domain.ui;
