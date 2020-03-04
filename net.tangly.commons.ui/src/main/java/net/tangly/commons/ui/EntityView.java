/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.core.Entity;

public class EntityView extends Composite<Div> {
    private transient Entity entity;
    private TextField oid;
    private TextField id;
    private TextField name;
    private DatePicker fromDate;
    private DatePicker toDate;
    private TextArea text;


    public EntityView(Entity entity) {
        this.entity = entity;
        getContent().setHeight("40vh");
        getContent().add(init());
        update();
    }

    private void update() {
        oid.setValue(Long.toString(entity.oid()));
        id.setValue(entity.id() != null ? entity.id() : id.getEmptyValue());
        name.setValue(entity.name() != null ? entity.name() : name.getEmptyValue());
        fromDate.setValue(entity.fromDate() != null ? entity.fromDate() : fromDate.getEmptyValue());
        toDate.setValue(entity.toDate() != null ? entity.toDate() : toDate.getEmptyValue());
        text.setValue(entity.text() != null ? entity.text() : text.getEmptyValue());
    }

    private Component init() {
        FormLayout form = new FormLayout();

        oid = new TextField("Oid");
        oid.setPlaceholder("oid");

        id = new TextField("Id");
        id.setPlaceholder("id");

        name = new TextField("Name");
        name.setPlaceholder("name");

        fromDate = new DatePicker();
        toDate = new DatePicker();

        HtmlComponent br = new HtmlComponent("br");

        text = new TextArea("Text");
        text.setHeight("8em");
        text.getStyle().set("colspan", "2");

        form.add(oid, id, name, fromDate, toDate, br, text);

        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3));
        return form;
    }
}
