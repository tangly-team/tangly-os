/*
 * Copyright 2006-2023 Marcel Baumann
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.jetbrains.annotations.NotNull;

public class BindableComponent<C extends Component & HasBindValue<V>, V> extends CustomField<V> {
    private final C component;

    public BindableComponent(@NotNull C component) {
        this.component = component;
        setHeightFull();
        setWidthFull();
        addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        component.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        add(component);
    }

    public BindableComponent(@NotNull C component, @NotNull Mode mode) {
        this(component);
        component.mode(mode);
    }

    public C component() {
        return component;
    }

    public void bind(@NotNull Binder<V> binder, boolean readonly) {
        component().bind(binder, readonly);
    }

    @Override
    protected V generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(V value) {
        component.value(value);
    }

    @Override
    public void clear() {
        setPresentationValue(null);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        component.mode(Mode.VIEW);
    }
}
