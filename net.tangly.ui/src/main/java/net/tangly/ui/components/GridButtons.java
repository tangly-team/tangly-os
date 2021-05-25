/*
 * Copyright 2006-2021 Marcel Baumann
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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Objects;

public class GridButtons<T> extends HorizontalLayout implements SelectedItemListener<T> {
    private final Crud.Mode mode;
    private final CrudForm<T> form;
    private final CrudActionsListener<T> actionsListener;
    private final Button details;
    private final Button add;
    private final Button update;
    private final Button delete;
    private T selectedItem;

    public GridButtons(Crud.Mode mode, CrudForm<T> form, CrudActionsListener<T> actionsListener) {
        this.mode = mode;
        this.form = form;
        this.actionsListener = actionsListener;
        setDefaultVerticalComponentAlignment(Alignment.END);
        details = new Button("Details", VaadinIcon.ELLIPSIS_H.create(), event -> displayDialog(CrudForm.Operation.VIEW));
        add = new Button("Add", VaadinIcon.PLUS.create(), event -> displayDialog(CrudForm.Operation.CREATE));
        update = new Button("Update", VaadinIcon.PENCIL.create(), event -> displayDialog(CrudForm.Operation.UPDATE));
        delete = new Button("Delete", VaadinIcon.TRASH.create(), event -> displayDialog(CrudForm.Operation.DELETE));

        add.setEnabled((mode == Crud.Mode.EDITABLE) || (mode == Crud.Mode.IMMUTABLE) || (mode == Crud.Mode.AUDITABLE));
        update.setEnabled(mode == Crud.Mode.EDITABLE);
        delete.setEnabled((mode == Crud.Mode.EDITABLE) || (mode == Crud.Mode.IMMUTABLE));
        selectedItem(null);
        if (mode.canAdd()) {
            add(add);
        }
        if (mode.canDelete()) {
            add(delete);
        }
        if (mode.canUpdate()) {
            add(update);
        }
        add(details);
    }

    public T selectedItem() {
        return selectedItem;
    }

    public void selectedItem(T item) {
        selectedItem = item;
        if (Objects.nonNull(selectedItem)) {
            details.setEnabled(true);
            add.setEnabled(mode.canAdd());
            update.setEnabled(mode.canUpdate());
            delete.setEnabled(mode.canDelete());
        } else {
            details.setEnabled(false);
            add.setEnabled(mode.canAdd());
            update.setEnabled(false);
            delete.setEnabled(false);
        }
    }

    void displayDialog(CrudForm.Operation operation) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setWidth("80vw");
        dialog.setHeight("70vh");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.add(new VerticalLayout(form.createForm(operation, operation != CrudForm.Operation.CREATE ? selectedItem : null), new HtmlComponent("br"),
            form.createFormButtons(dialog, operation, mode.isCancellable(), selectedItem(), actionsListener)));
        dialog.open();
    }
}
