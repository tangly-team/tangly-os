/*
 * Copyright 2023-2024 Marcel Baumann
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

import com.vaadin.flow.component.formlayout.FormLayout;
import net.tangly.core.*;
import net.tangly.ui.asciidoc.AsciiDocField;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Create the form for a simple entity or a full entity. A simple entity has an internal object identifier, an external identifier, a human-readable name, a
 * time interval when the entity is active, and a textual description. A full entity has additionally a set of tags and a set of comments.
 * <p> Entities are immutable instances. Therefore we need to retrieve new values directuly from the form to create an update of an instance.</p>
 *
 * @param <T> type of the entity displayed in the form
 * @param <V> the view owning the form
 */
public abstract class EntityForm<T extends Entity, V extends EntityView<T>> extends ItemForm<T, V> {
    private final AsciiDocField text;
    private final EntityField<T> entity;
    private final One2ManyOwnedField<Comment> comments;
    private final One2ManyOwnedField<Tag> tags;

    protected EntityForm(@NotNull V parent) {
        super(parent);
        text = new AsciiDocField("Text");
        binder().bindReadOnly(text, HasText::text);
        entity = new EntityField<>("Entity");
        if (HasComments.class.isAssignableFrom(entityClass())) {
            comments = new One2ManyOwnedField<>(new CommentsView(parent.domainUi(), parent.mode()));
            binder().bindReadOnly(comments, o -> ((EntityExtended) o).comments());
        } else {
            comments = null;
        }
        if (HasTags.class.isAssignableFrom(entityClass())) {
            tags = new One2ManyOwnedField<>(new TagsView(parent.domainUi(), parent.mode()));
            binder().bindReadOnly(tags, o -> ((EntityExtended) o).tags());
        } else {
            tags = null;
        }
    }

    protected final void initEntityForm() {
        entity.bind(binder());
        FormLayout form = new FormLayout();
        form.add(entity);
        addTabAt("entity", form, 0);
        addTabAt("text", textForm(text), 1);
        if (tags != null) {
            addTabAt("tags", tags, 2);
        }
        if (comments != null) {
            addTabAt("comments", comments, 3);
        }
    }

    /**
     * Duplicate the entity and clears the object identifier field.
     *
     * @see ItemForm#duplicate(Object)
     */
    @Override
    public void duplicate(@NotNull T entity) {
        super.duplicate(entity);
        this.entity.clearOid();
    }

    // region Entity user interface field accessors

    protected long oid() {
        return entity.oid();
    }

    protected String id() {
        return entity.id();
    }

    protected String name() {
        return entity.name();
    }

    protected DateRange dateRange() {
        return entity.dateRange();
    }

    protected String text() {
        return text.getValue();
    }

    protected Collection<Tag> tags() {
        return tags != null ? tags.getValue() : Collections.emptyList();
    }

    protected Collection<Comment> comments() {
        return comments != null ? comments.getValue() : Collections.emptyList();
    }

    // endregion
}
