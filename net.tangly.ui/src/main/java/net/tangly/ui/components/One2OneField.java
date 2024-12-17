/*
 * Copyright 2006-2024 Marcel Baumann
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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.core.MutableEntity;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A composite field to display a one-2-one relationship instance. The unique name of the referenced instance is shown. The button allows the change of the relationship instance.
 * <p>The referenced object is stored as a field. The displayed fields are updated through {@link One2OneField#setPresentationValue(T)}.</p>
 * <p>The selection view displays the fields of an entity as columns. The provided provider can be used to filter out the items being considered for the field.</p>
 *
 * @param <T> type of the entity referenced in the one-2-one relationship.
 */
@Tag("tangly-field-one2one")
public class One2OneField<T extends MutableEntity> extends CustomField<T> {
    private final Class<T> entityClass;
    private final transient Provider<T> provider;
    private final TextField oid;
    private final TextField id;
    private final TextField name;
    private final Button update;

    public One2OneField(@NotNull String relation, @NotNull Class<T> entityClass, @NotNull Provider<T> provider) {
        this.entityClass = entityClass;
        this.provider = provider;
        setLabel(relation);
        oid = VaadinUtils.createTextField(EntityView.OID_LABEL, EntityView.OID, true, true);
        id = VaadinUtils.createTextField(EntityView.ID_LABEL, EntityView.ID, true, true);
        name = VaadinUtils.createTextField(EntityView.NAME_LABEL, EntityView.NAME, true, true);
        update = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_V));
        update.addClickListener(e -> displayRelationships());
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.add(oid, id, name, update);
        add(layout);
    }


    @Override
    protected T generateModelValue() {
        return super.getValue();
    }

    @Override
    protected void setPresentationValue(T one2one) {
        if (Objects.isNull(one2one)) {
            clear();
        } else {
            oid.setValue(Long.toString(one2one.oid()));
            VaadinUtils.setValue(id, one2one.id());
            VaadinUtils.setValue(name, one2one.name());
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
        oid.clear();
        id.clear();
        name.clear();
    }

    private void displayRelationships() {
        Dialog dialog = VaadinUtils.createDialog();
        EntityView<T> view = EntityView.ofLIST(entityClass, null, provider);
        dialog.add(new VerticalLayout(view, new HtmlComponent("br"), createFormButtons(dialog, view)));
        view.selectedItem(getValue());
        dialog.open();
    }

    private HorizontalLayout createFormButtons(@NotNull Dialog dialog, @NotNull EntityView<T> view) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        Button cancel = new Button("Cancel", event -> dialog.close());
        Button clear = new Button("Remove", event -> {
            clear();
            dialog.close();
        });
        Button change = new Button("Update", event -> {
            setValue(view.selectedItem());
            dialog.close();
        });
        actions.add(cancel, clear, change);
        return actions;
    }
}
