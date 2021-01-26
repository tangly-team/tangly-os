/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.ui;

import com.vaadin.flow.component.menubar.MenuBar;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the interface for the user interface of a bounded domain.
 */
public interface BoundedDomainUi {
    String ENTITIES = "Entities";
    String ADMINISTRATION = "Administration";
    String STATISTICS = "Statistics";
    String IMPORT = "Import";
    String EXPORT = "Export";

    /**
     * Returns the name of bounded domain user interface as displayed in the user interface.
     *
     * @return name of the bounded domain
     */
    String name();

    /**
     * Selects the view to be displayed from the bounded domain user interface and update the menu to reflect the bounded domain.
     *
     * @param layout  layout in which the view will be displayed
     * @param menuBar empty menu bar of the layout
     */
    void select(@NotNull MainLayout layout, @NotNull MenuBar menuBar);
}
