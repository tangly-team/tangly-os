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

package net.tangly.commons.crm.ui;

import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.Tag;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.crm.ports.Crm;
import org.jetbrains.annotations.NotNull;

/**
 * The CRM entity form provides a CRUD abstraction to all properties of an entity instance part of the CRM domain model.
 *
 * @param <T> type displayed in the CRUD view
 */
public abstract class CrmEntitiesView<T extends Entity> extends InternalEntitiesView<T> {
    private final Crm crm;

    protected CrmEntitiesView(@NotNull Crm crm, @NotNull Class<T> clazz, @NotNull Mode mode, Consumer<Grid<T>> gridConfigurator, Provider<T> provider) {
        super(clazz, mode, gridConfigurator, provider, crm.tagTypeRegistry());
        this.crm = crm;
    }

    public static <T extends Entity> ComponentRenderer<Anchor, T> linkedInComponentRenderer(Function<String, String> linkedInUrl) {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            Tag tag = item.findBy(CrmTags.CRM_IM_LINKEDIN).orElse(null);
            String linkedInRef = (tag != null) ? tag.value() : null;
            anchor.setText(linkedInRef);
            anchor.setHref((linkedInRef != null) ? linkedInUrl.apply(linkedInRef) : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static <T extends Entity> ComponentRenderer<Anchor, T> urlComponentRenderer(String tagName) {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            Tag tag = item.findBy(tagName).orElse(null);
            String url = (tag != null) ? tag.value() : null;
            anchor.setText(url);
            anchor.setHref((url != null) ? "https://" + url : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }
    
    protected Crm crm() {
        return crm;
    }
}
