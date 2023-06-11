/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

/**
 * Provides the adapters for the bounded domain. The package is based on the hexagonal architecture pattern. It avoids polluting the business model with
 * implementation dependant constructs.
 * <p>The handler classes implements the transformation between a business model and an external system or storage.n The workflow classes provide aggregations
 * of functions which are semantically related. For example the handler publishes methods to import individual domain entities and the workflow publishes
 * methods to import all semantically related entities of a domain.</p>
 * <p>The business logic classes publish domain methods spanning multiple domain entities.</p>
 */
package net.tangly.erp.crm.ports;
