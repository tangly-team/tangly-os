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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.core.HasName;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A composite field to display an one two one relationship instance. The unique name of the referenced instance is shown. The button allows the change of the
 * one 2 one relationship instance.
 *
 * @param <T> type of the entity referenced in the one 2 one relationship
 * @param <V> type of the component used to display the details or select a new entity
 */
public class One2OneField<T extends HasName, V extends Component & HasIdView<T>> extends CustomField<T> {
    private final TextField name;
    private final Button update;
    private final V view;

    public One2OneField(@NotNull String relation, @NotNull V view) {
        this.view = view;
        setLabel(relation);
        name = VaadinUtils.createTextField("Name", "name", true, false);
        update = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
        update.addClickListener(e -> displayRelationships());
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(name, update);
        add(horizontalLayout);
    }

    @Override
    protected T generateModelValue() {
        return getValue();
    }

    @Override
    protected void setPresentationValue(T one2one) {
        if (Objects.nonNull(one2one)) {
            VaadinUtils.setValue(name, one2one.name());
        } else {
            clear();
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        update.setEnabled(!isReadOnly());
    }

    @Override
    public void clear() {
        super.clear();
        name.clear();
    }

    private void displayRelationships() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setResizable(true);
        dialog.add(new VerticalLayout(view, new HtmlComponent("br"), createFormButtons(dialog)));
        view.selectedItem(getValue());
        dialog.open();
    }

    private HorizontalLayout createFormButtons(@NotNull Dialog dialog) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        Button cancel = new Button("Cancel", event -> dialog.close());
        Button clear = new Button("Clear", event -> {
            clear();
            dialog.close();
        });
        Button select = new Button("Update", event -> {
            setValue(view.selectedItem());
            dialog.close();
        });
        actions.add(cancel, clear, select);
        return actions;
    }
}
