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
import net.tangly.core.HasComments;
import org.jetbrains.annotations.NotNull;

/**
 * The comments view is a Crud view with all the comments defined for an object implementing the {@link HasComments}. Edition functions are provided to add, delete, and view
 * individual comments. Update function is not supported because comments are immutable objects. Immutable objects must explicitly be deleted before a new version is added. This
 * approach supports auditing approaches.
 * <p>the filter conditions are defined as follow. You can filter by author, by a string contained in the text, or a time interval during which the comment was created.
 * Therefore to select the range of interest you need to input two dates.</p>
 */
public class BindableComponent<C extends Component & BindableComponent.HasBindValue<V>, V> extends CustomField<V> {
    public interface HasBindValue<V> {
        V value();

        void value(V item);

        ItemView.Mode mode();

        void mode(@NotNull ItemView.Mode mode);

        void bind(@NotNull Binder<V> binder, boolean readonly);
    }

    private final C component;

    public BindableComponent(@NotNull C component) {
        this.component = component;
        setHeightFull();
        setWidthFull();
        addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        component.addClassNames(LumoUtility.Padding.NONE, LumoUtility.Margin.NONE, LumoUtility.Border.NONE);
        add(component);
    }

    public BindableComponent(@NotNull C component, @NotNull ItemView.Mode mode) {
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
        component.mode(ItemView.Mode.VIEW);
    }
}
