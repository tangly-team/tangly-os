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

package net.tangly.erp.ui;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;

@PWA(name = "Tangly ERP", shortName = "ERP")
@Theme(themeClass = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/override-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport(value = "./styles/override-negative.css", themeFor = "vaadin-grid")
@JsModule("./prefers-color-scheme.js")
@Push
public class AppShell implements AppShellConfigurator {
}
