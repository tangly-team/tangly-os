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

package net.tangly.ui.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Selection field for a reference code and all its values.
 *
 * @param <T> reference code to display
 * @see Code
 */
@Tag("tangly-field-code")
public class CodeField<T extends Code> extends ComboBox<T> {

    public CodeField(@NotNull CodeType<T> codeType, String label) {
        setLabel(label);
        setItemLabelGenerator(o -> (Objects.isNull(o) ? "" : o.code()));
        setItems(codeType.codes());
        setPlaceholder("select item");
        setOpened(false);
    }
}
