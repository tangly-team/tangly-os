/*
 * Copyright 2021-2024 Marcel Baumann
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

package net.tangly.ui.components;

import com.vaadin.flow.component.html.Anchor;
import org.jetbrains.annotations.NotNull;

/**
 * Defines an HTML link with a test and a reference.
 */
public class Link extends Anchor {
    public Link(@NotNull String text, @NotNull String href) {
        super(href, text);
        setTitle(text);
    }
}
