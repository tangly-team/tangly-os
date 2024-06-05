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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import net.tangly.core.*;
import net.tangly.ui.asciidoc.AsciiDocField;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Creates the form for a simple entity or a full entity. A simple entity has an internal object identifier, an external identifier, a human-readable name, a time interval when the
 * entity is active, and a textual description. A full entity has additionally a set of tags and a set of comments.
 *
 * @param <T> type of the entity displayed in the form
 * @param <V> the view owning the form
 */
public abstract class MutableEntityForm<T extends MutableEntity, V extends EntityView<T>> extends ItemForm<T, V> {
    private final Function<Long, T> supplier;
    private final AsciiDocField text;
    private final EntityField<T> entity;
    private final One2ManyOwnedField<Comment> comments;
    private final One2ManyOwnedField<Tag> tags;

    protected MutableEntityForm(@NotNull V parent, Function<Long, T> supplier) {
        super(parent);
        this.supplier = supplier;
        text = new AsciiDocField("Text");
        binder().bind(text, HasMutableText::text, HasMutableText::text);
        entity = new EntityField<>("Entity");
        if (HasMutableComments.class.isAssignableFrom(entityClass())) {
            comments = new One2ManyOwnedField<>(new CommentsView(parent.domainUi(), parent.mode()));
        } else {
            comments = null;
        }
        if (HasMutableTags.class.isAssignableFrom(entityClass())) {
            tags = new One2ManyOwnedField<>(new TagsView(parent.domainUi(), parent.mode()));
        } else {
            tags = null;
        }
    }

    protected final void initEntityForm() {
        entity.bindMutable(binder());

        FormLayout form = new FormLayout();
        form.add(entity);
        addTabAt("entity", form, 0);
        addTabAt("text", textForm(text), 1);
        if (tags != null) {
            binderCast().bind(tags, o -> ((MutableEntityExtended) o).tags(), (o, v) -> ((MutableEntityExtended) o).tags(tags.generateModelValue()));
            addTabAt("tags", tags, 2);
        }
        if (comments != null) {
            binderCast().bind(comments, o -> ((MutableEntityExtended) o).comments(), (o, v) -> ((MutableEntityExtended) o).comments(comments.generateModelValue()));
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

    @Override
    protected T createOrUpdateInstance(T entity) throws ValidationException {
        T updatedEntity = Objects.nonNull(entity) ? entity : supplier().apply(HasOid.UNDEFINED_OID);
        binder().writeBean(updatedEntity);
        return updatedEntity;
    }

    protected Function<Long, T> supplier() {
        return supplier;
    }

    @SuppressWarnings("unchecked")
    protected <U> Binder<U> binderCast() {
        return (Binder<U>) binder();
    }
}
