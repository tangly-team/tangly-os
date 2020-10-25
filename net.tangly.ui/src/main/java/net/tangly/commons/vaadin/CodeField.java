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

import com.vaadin.flow.component.select.Select;
import net.tangly.bus.codes.Code;
import net.tangly.bus.codes.CodeType;

/**
 * Selection field for a reference code and all its values.
 * @param <T> reference code to display
 * @see Code
 */
public class CodeField<T extends Code> extends Select<T> {
    private final CodeType<T> codeType;

    public CodeField(CodeType<T> codeType, String label) {
        setLabel(label);
        this.codeType = codeType;
        setItemLabelGenerator(T::code);
        setItems(codeType.codes());
        setItemEnabledProvider(e -> e.isEnabled());
    }
}
