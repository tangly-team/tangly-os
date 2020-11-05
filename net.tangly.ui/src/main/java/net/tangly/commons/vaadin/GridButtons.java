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

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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
        details = new Button("Details", VaadinIcon.ELLIPSIS_H.create(), event -> displayDialog(CrudForm.Operation.VIEW));
        add = new Button("Add", VaadinIcon.PLUS.create(), event -> displayDialog(CrudForm.Operation.CREATE));
        update = new Button("Update", VaadinIcon.PENCIL.create(), event -> displayDialog(CrudForm.Operation.UPDATE));
        delete = new Button("Delete", VaadinIcon.TRASH.create(), event -> displayDialog(CrudForm.Operation.DELETE));

        add.setEnabled((mode == Crud.Mode.EDITABLE) || (mode == Crud.Mode.IMMUTABLE) || (mode == Crud.Mode.AUDITABLE));
        update.setEnabled(mode == Crud.Mode.EDITABLE);
        delete.setEnabled((mode == Crud.Mode.EDITABLE) || (mode == Crud.Mode.IMMUTABLE));
        selectedItem(null);
        addAndExpand(add, delete, update, details);
    }

    public T selectedItem() {
        return selectedItem;
    }

    public void selectedItem(T item) {
        selectedItem = item;
        if (selectedItem != null) {
            details.setEnabled(true);
            add.setEnabled(Crud.Mode.canAdd(mode));
            update.setEnabled(Crud.Mode.canUpdate(mode));
            delete.setEnabled(Crud.Mode.canDelete(mode));
        } else {
            details.setEnabled(false);
            add.setEnabled(Crud.Mode.canAdd(mode));
            update.setEnabled(false);
            delete.setEnabled(false);
        }
    }

    void displayDialog(CrudForm.Operation operation) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setWidth("400vw");
        dialog.setHeight("80vh");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.add(new VerticalLayout(form.createForm(operation, operation != CrudForm.Operation.CREATE ? selectedItem : null), new HtmlComponent("br"),
            CrudForm.createFormButtons(dialog, form, operation, selectedItem(), actionsListener)));
        dialog.open();
    }
}
