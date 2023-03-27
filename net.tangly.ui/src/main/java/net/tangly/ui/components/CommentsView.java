/*
 * Copyright 2006-2023 Marcel Baumann
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

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.core.Comment;
import net.tangly.core.HasComments;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;

/**
 * The comments view is a Crud view with all the comments defined for an object implementing the {@link HasComments}. Edition functions are provided to add,
 * delete, and view individual comments. Update function is not supported because comments are immutable objects. Immutable objects must be explicitly be
 * deleted before a new version is added. This approach supports auditing approaches.
 */
public class CommentsView extends EntityView<Comment> {
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";
    private static final String TEXT = "text";
    private static final String CREATED_LABEL = "Created";
    private static final String AUTHOR_LABEL = "Author";
    private static final String TEXT_LABEL = "Text";
    private transient HasComments hasComments;
    private CommentFilter entityFilter;

    public CommentsView(HasComments entity) {
        super(Comment.class, ProviderInMemory.of(Objects.nonNull(entity) ? entity.comments() : Collections.emptyList()), true);
        form = new CommentForm(this);
        this.hasComments = entity;
        init();
    }

    public void fill(HasComments entity) {
        this.hasComments = entity;
        provider(ProviderInMemory.of(Objects.nonNull(entity) ? entity.comments() : Collections.emptySet()));
    }

    @Override
    protected void init() {
        Grid<Comment> grid = grid();
        grid.setPageSize(5);
        grid.addColumn(Comment::created).setKey("created").setHeader("Created").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
            .setFrozen(true);
        grid.addColumn(Comment::author).setKey("author").setHeader("Author").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Comment::text).setKey("text").setHeader("Text").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);

        entityFilter = new CommentFilter(dataView());
        grid().getHeaderRows().clear();
        HeaderRow headerRow = grid().appendHeaderRow();
//        addFilter(headerRow, CREATED, CREATED_LABEL, entityFilter::created);
        addFilter(headerRow, AUTHOR, AUTHOR_LABEL, entityFilter::author);
        addFilter(headerRow, TEXT, TEXT_LABEL, entityFilter::text);
        initMenu();
    }

    static class CommentFilter extends EntityView.EntityFilter<Comment> {
        private LocalDate from;
        private LocalDate to;
        private String author;
        private String text;

        public CommentFilter(@NotNull GridListDataView<Comment> dataView) {
            super(dataView);
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
            return matches(entity.author(), author) && matches(entity.text(), text) && HasTimeInterval.isActive(entity.created().toLocalDate(), from, to);
        }
    }

    static class CommentForm extends EntityView.EntityForm<Comment> {
        protected Binder<Comment> binder;
        protected DateTimePicker created;
        protected TextField author;
        protected TextField text;

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

            form().add(fieldsLayout, createButtonsBar());
            binder = new Binder<>(Comment.class);
            binder.forField(created).bind(Comment::created, null);
            binder.forField(author).bind(Comment::author, null);
            binder.forField(text).bind(Comment::text, null);
        }

        @Override
        protected void mode(@NotNull Mode mode) {
            created.setReadOnly(mode.readonly());
            author.setReadOnly(mode.readonly());
            text.setReadOnly(mode.readonly());
        }

        @Override
        public void fill(Comment entity) {
            if (entity != null) {
                binder.readBean(entity);
            }
        }

        @Override
        protected void clear() {
            created.clear();
            author.clear();
            text.clear();
        }

        @Override
        protected Comment createOrUpdateInstance(Comment entity) {
            return new Comment(author.getValue(), text.getValue());

        }
    }

    private String username() {
        return (VaadinSession.getCurrent() != null) ? (String) VaadinSession.getCurrent().getAttribute("username") : null;
    }
}
