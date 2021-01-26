/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.util.Objects;

import com.vaadin.flow.component.select.Select;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;

/**
 * Selection field for a reference code and all its values.
 * <p>Beware that the selected item can be null when {@link com.vaadin.flow.component.select.Select#setEmptySelectionAllowed(boolean)} is set to true.</p>
 *
 * @param <T> reference code to display
 * @see Code
 */
public class CodeField<T extends Code> extends Select<T> {
    private final CodeType<T> codeType;

    public CodeField(CodeType<T> codeType, String label) {
        setLabel(label);
        this.codeType = codeType;
        setItemLabelGenerator(o -> (Objects.isNull(o) ? "" : o.code()));
        setItems(codeType.codes());
        setItemEnabledProvider(o -> (Objects.isNull(o) ? true : o.isEnabled()));
    }
}
