/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.commons.logger.EventData;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.components.*;
import org.jetbrains.annotations.NotNull;

public class EventDataView extends ItemView<EventData> {
    public static final String EVENT = "event";
    public static final String TIMESTAMP = "timestamp";
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final String Event_LABEL = "Event";
    public static final String TIMESTAMP_LABEL = "Timestamp";
    public static final String STATUS_LABEL = "Status";
    public static final String DATA_LABEL = "Data";

    static class EventDataForm extends ItemForm<EventData, EventDataView> {

        EventDataForm(@NotNull EventDataView parent) {
            super(parent);
            addTabAt("details", details(), 0);
        }

        protected FormLayout details() {
            TextField event = VaadinUtils.createTextField(Event_LABEL, EVENT, true, false);
            TextField timestamp = VaadinUtils.createTextField(TIMESTAMP_LABEL, TIMESTAMP, true, false);
            TextField domain = VaadinUtils.createTextField(DOMAIN_LABEL, DOMAIN, true, false);
            TextField status = VaadinUtils.createTextField(STATUS_LABEL, STATUS, true, false);
            TextField text = VaadinUtils.createTextField(TEXT_LABEL, TEXT, true, false);
            TextArea data = VaadinUtils.createTextArea(DATA_LABEL, DATA, true, false);

            FormLayout form = new FormLayout();
            form.add(event, timestamp, domain, status, text, data);
            form.setColspan(text,2);
            form.setColspan(data,2);

            binder().bindReadOnly(event, EventData::event);
            binder().bindReadOnly(timestamp, o -> o.timestamp().toString());
            binder().bindReadOnly(domain, EventData::domain);
            binder().bindReadOnly(status, o -> o.status().toString());
            binder().bindReadOnly(text, EventData::text);
            binder().bindReadOnly(data, o -> o.data().toString());
            return form;
        }

        @Override
        protected EventData createOrUpdateInstance(EventData entity) throws ValidationException {
            return entity;
        }
    }

    public EventDataView(BoundedDomainUi<?> domain) {
        super(EventData.class, domain, ProviderInMemory.of(domain.domain().auditEvents()), null, Mode.VIEW);
        form(() -> new EventDataForm(this));
        init();
    }

    @Override
    protected void addActions(@NotNull GridMenu<EventData> menu) {
        menu().add(new Hr());
        menu().add("Refresh", _ -> this.provider( ProviderInMemory.of(domain().auditEvents())), GridMenu.MenuItemType.GLOBAL);
    }


    private void init() {
        var grid = grid();
        grid.addColumn(EventData::event).setKey(EVENT).setHeader(Event_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(EventData::timestamp).setKey(TIMESTAMP).setHeader(TIMESTAMP_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(EventData::domain).setKey(DOMAIN).setHeader(DOMAIN_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(EventData::status).setKey(STATUS).setHeader(STATUS_LABEL).setSortable(true).setAutoWidth(true).setResizable(true);
    }
}
