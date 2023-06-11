/*
 * Copyright 2023 Marcel Baumann
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
import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Creates the form for a simple entity or a full entity. A simple entity has an internal object identifier, an external identifier, a human-readable name, a time interval when the
 * entity is active, and a textual description. A full entity has additionally a set of tags and a set of comments.
 *
 * @param <T> type of the entity displayed in the form
 * @param <V> the view owning the form
 */
public abstract class EntityForm<T extends HasOid & HasId & HasName & HasTimeInterval & HasText, V extends EntityView<T>> extends ItemForm<T, V> {
    static class HasTagsAndComments<T extends HasTags & HasComments> {
        private BindableComponent<CommentsView, HasComments> commentsView;
        private BindableComponent<TagsView, HasTags> tagsView;

        void init(@NotNull EntityForm<?, ?> form, @NotNull Binder<T> binder) {
            tagsView = new BindableComponent<>(new TagsView(null, form.parent().registry(), form.parent().mode()), form.parent().mode());
            tagsView.bind(binderCast(binder), tagsView.component().mode().readonly());
            commentsView = new BindableComponent<>(new CommentsView(null, form.parent().mode()), form.parent().mode());
            commentsView.bind(binderCast(binder), commentsView.component().mode().readonly());
            form.addTabAt("tags", tagsView, 1);
            form.addTabAt("comments", commentsView, 2);
        }

        void value(T value) {
            if (value != null) {
                tagsView.setPresentationValue(value);
                commentsView.setPresentationValue(value);
            } else {
                clear();
            }
        }

        void mode(@NotNull ItemView.Mode mode) {
            tagsView.component().mode(mode);
            commentsView.component().mode(mode);
            tagsView.setReadOnly(mode.readonly());
            commentsView.setReadOnly(mode.readonly());
        }

        void clear() {
            commentsView.clear();
            tagsView.clear();
        }

        private <T> Binder<T> binderCast(@NotNull Binder<?> binder) {
            return (Binder<T>) binder;
        }
    }

    protected EntityField<T> entity;
    protected final Optional<HasTagsAndComments<?>> hasTagsAndComments;

    public EntityForm(@NotNull V parent) {
        this(parent, true);
    }

    public EntityForm(@NotNull V parent, boolean hasTagsAndComments) {
        super(parent);
        this.hasTagsAndComments = Optional.ofNullable(hasTagsAndComments ? new HasTagsAndComments<>() : null);
    }

    @Override
    public void mode(@NotNull ItemView.Mode mode) {
        entity.setReadOnly(mode.readonly());
        hasTagsAndComments.ifPresent(o -> o.mode(ItemView.Mode.propagated(mode)));
    }

    @Override
    public void value(T value) {
        if (value != null) {
            binder().readBean(value);
            hasTagsAndComments.ifPresent(o -> o.value(toHasTagsAndComments(value)));
        }
    }

    @Override
    public void clear() {
        entity.clear();
        hasTagsAndComments.ifPresent(HasTagsAndComments::clear);
    }

    protected void init() {
        entity = new EntityField<>();
        entity.bind(binder(), true);

        FormLayout form = new FormLayout();
        form.add(entity);
        addTabAt("entity", form, 0);
        hasTagsAndComments.ifPresent(o -> o.init(this, binderCast()));
    }

    protected <T> Binder<T> binderCast() {
        return (Binder<T>) binder();
    }

    <U extends HasTags & HasComments> U toHasTagsAndComments(T value) {
        return (U) value;
    }

    protected T createOrUpdateInstance(T entity, Function<Long, T> supplier) throws ValidationException {
        T updatedEntity = Objects.nonNull(entity) ? entity : supplier.apply(fromBinder(HasOid.OID));
        binder().writeBean(updatedEntity);
        return updatedEntity;
    }
}
