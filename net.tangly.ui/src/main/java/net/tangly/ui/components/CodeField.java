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

package net.tangly.ui.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.select.Select;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Selection field for a reference code and all its values.
 * <p>Beware that the selected item can be null when {@link com.vaadin.flow.component.select.Select#setEmptySelectionAllowed(boolean)} is set to true.</p>
 *
 * @param <T> reference code to display
 * @see Code
 */
@Tag("tangly-field-code")
public class CodeField<T extends Code> extends Select<T> {
    private final CodeType<T> codeType;

    public CodeField(@NotNull CodeType<T> codeType, String label) {
        this.codeType = codeType;
        setLabel(label);
        setItemLabelGenerator(o -> (Objects.isNull(o) ? "" : o.code()));
        setItemEnabledProvider(o -> (Objects.isNull(o) ? true : o.isEnabled()));
        setItems(codeType.codes());
        setEmptySelectionAllowed(true);
        setPlaceholder("select item");
        setEmptySelectionCaption("no item selected");
        setOpened(false);
    }
}
