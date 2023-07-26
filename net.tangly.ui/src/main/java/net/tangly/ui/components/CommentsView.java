/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.core.Comment;
import net.tangly.core.HasComments;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;

/**
 * The comments view is a Crud view with all the comments defined for an object implementing the {@link HasComments}. Edition functions are provided to add, delete, and view
 * individual comments. Update function is not supported because comments are immutable objects. Immutable objects must explicitly be deleted before a new version is added. This
 * approach supports auditing approaches.
 * <p>the filter conditions are defined as follow. You can filter by author, by a string contained in the text, or a time interval during which the comment was created.
 * Therefore to select the range of interest you need to input two dates.</p>
 */
public class CommentsView extends ItemView<Comment> implements HasBindValue<HasComments> {
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";
    private static final String TEXT = "text";
    private static final String CREATED_LABEL = "Created";
    private static final String AUTHOR_LABEL = "Author";
    private static final String TEXT_LABEL = "Text";

    static class CommentFilter extends ItemFilter<Comment> {
        private LocalDate from;
        private LocalDate to;
        private String author;
        private String text;

        public CommentFilter() {
        }

        public void createdRange(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
            refresh();
        }

        public void author(String author) {
            this.author = author;
            refresh();
        }

        public void text(String text) {
            this.text = text;
            refresh();
        }

        @Override
        public boolean test(@NotNull Comment entity) {
            return matches(entity.author(), author) && matches(entity.text(), text);
        }
    }

    static class CommentForm extends ItemForm<Comment, CommentsView> {
        private Binder<Comment> itemBinder;
        private DateTimePicker created;
        private TextField author;
        private TextField text;

        public CommentForm(@NotNull CommentsView parent) {
            super(parent);
            init();
        }

        protected void init() {
            FormLayout fieldsLayout = new FormLayout();
            created = new DateTimePicker(CREATED);
            created.setReadOnly(true);
            author = new TextField(AUTHOR);
            author.setRequired(true);
            text = new TextField(TEXT);
            fieldsLayout.add(created, author, text);
            fieldsLayout.setColspan(text, 3);
            fieldsLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));
            form().add(fieldsLayout, createButtonBar());
            itemBinder = new Binder<>(Comment.class);
            itemBinder.forField(created).bind(Comment::created, null);
            itemBinder.forField(author).bind(Comment::author, null);
            itemBinder.forField(text).bind(Comment::text, null);
        }

        @Override
        public void mode(@NotNull Mode mode) {
            super.mode(mode);
            created.setReadOnly(mode.readonly());
            author.setReadOnly(mode.readonly());
            text.setReadOnly(mode.readonly());
        }

        @Override
        public void value(Comment value) {
            if (value != null) {
                itemBinder.readBean(value);
            }
        }

        @Override
        public void clear() {
            created.clear();
            author.clear();
            text.clear();
        }

        /**
         * Handle the edition or addition of an immutable comment. If the parameter is not null, it is removed from the list before the changed version is added. The updated
         * property values are retrieved from the form. The logic of the item form we inherited from takes care of the provider update to synchronize the user interface grid.
         *
         * @param entity the entity to update or null if it is a created or duplicated instance
         * @return new comment of the entity
         */
        @Override
        protected Comment createOrUpdateInstance(Comment entity) {
            Comment comment = new Comment(author.getValue(), text.getValue());
            HasComments hasComments = parent().hasComments;
            if (entity != null) {
                hasComments.remove(entity);
            }
            hasComments.add(comment);
            return comment;
        }
    }

    private transient HasComments hasComments;

    /**
     * The view is created with an optional entity providing the comments to display. The set of comments can be later updated by calling {@link ItemView#provider(Provider)}.
     *
     * @param entity optional entity with the comment set
     * @param domain optional domain to which the generic entity belongs to
     * @param mode   mode of the component
     */

    public CommentsView(HasComments entity, @NotNull BoundedDomain<?, ?, ?, ?> domain, @NotNull Mode mode) {
        super(Comment.class, domain, ProviderInMemory.of(Objects.nonNull(entity) ? entity.comments() : Collections.emptyList()), new CommentFilter(), mode);
        form = new CommentForm(this);
        this.hasComments = entity;
        init();
    }

    @Override
    public HasComments value() {
        return hasComments;
    }

    @Override
    public void value(HasComments value) {
        if (!Objects.equals(hasComments, value)) {
            this.hasComments = value;
            provider(ProviderInMemory.of(Objects.nonNull(value) ? value.comments() : Collections.emptyList()));
            dataView().refreshAll();
        }
    }

    @Override
    protected void init() {
        setHeight("15em");
        Grid<Comment> grid = grid();
        grid.addColumn(Comment::created).setKey(CREATED).setHeader(CREATED_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Comment::author).setKey(AUTHOR).setHeader(AUTHOR_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Comment::text).setKey(TEXT).setHeader(TEXT_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("25em");

        if (filter() instanceof CommentFilter filter) {
            grid().getHeaderRows().clear();
            HeaderRow headerRow = grid().appendHeaderRow();
            addFilterText(headerRow, AUTHOR, filter::author);
            addFilterText(headerRow, TEXT, filter::text);
        }
        buildMenu();
    }

    private String username() {
        return (VaadinSession.getCurrent() != null) ? (String) VaadinSession.getCurrent().getAttribute("username") : null;
    }
}
