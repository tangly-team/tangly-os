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

/**
 * The main view shall support the domain driven design approach of bounded domains. At the same time the naked object approach for domain entities shall also
 * be part of the solution space.
 * <p>Each bounded domain shall be accessible through a router link on the left drawer. All naked entities are accessble through a bounded domain specific
 * menu list. The menu list consists again of router links. Therefore we have navigation URL for all bounded domains and exported entities.</p>
 * <p>The bounded domain instances are singletons in the application and available through the application class. The user interface bounded domains are defined for each
 * session and available through the main window instance.</p>
 */
package net.tangly.erp.ui;
