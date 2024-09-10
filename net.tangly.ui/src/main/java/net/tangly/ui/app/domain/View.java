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

package net.tangly.ui.app.domain;

import net.tangly.ui.components.Mode;

/**
 * Define the contract for a view in the application. A view is a component that can be displayed in the application.
 */
public interface View {
    /**
     * Return the mode of the view. Currently, we support LIST, VIEW, and EDITABLE modes.
     *
     * @return mode of the view
     */
    default Mode mode() {
        return Mode.LIST;
    }

    /**
     * Return if the view is in readonly mode.
     *
     * @return true if the view is readonly otherwise false
     * @see #readonly(boolean)
     */
    boolean readonly();

    /**
     * Set the view in readonly mode.
     *
     * @param readonly true if the view is readonly otherwise false
     * @see #readonly()
     */
    void readonly(boolean readonly);

    /**
     * Refresh the view with the latest data.
     */
    void refresh();
}
