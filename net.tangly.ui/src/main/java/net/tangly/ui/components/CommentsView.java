/*
 * Copyright 2006-2024 Marcel Baumann
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

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.core.Comment;
import net.tangly.core.DateRange;
import net.tangly.core.HasComments;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.asciidoc.AsciiDocField;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A view for a list of objects. The local copy of the list supports adding and removing items. To add items, a provider is passed with the eligible list of items
 * <p>
 * The comments view is a Crud view with all the comments defined for an object implementing the {@link HasComments}. Edition functions are provided to add, delete, and view
 * individual comments. Update function is not supported because comments are immutable objects. Immutable objects must explicitly be deleted before a new version is added. This
 * approach supports auditing approaches.
 * <p>the filter conditions are defined as follow. You can filter by author, by a string contained in the text, or a time interval during which the comment was created.
 * Therefore to select the range of interest you need to input two dates.</p>
 */
public class CommentsView extends ItemView<Comment> {
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";
    private static final String TEXT = "text";
    private static final String CREATED_LABEL = "Created";
    private static final String AUTHOR_LABEL = "Author";
    private static final String TEXT_LABEL = "Text";

    static class CommentFilter extends ItemView.ItemFilter<Comment> {
        private DateRange.DateFilter range;
        private String author;
        private String text;

        public CommentFilter() {
        }

        public void range(@NotNull DateRange range) {
            this.range = new DateRange.DateFilter(range);
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
            return matches(entity.author(), author) && (Objects.isNull(range) || range.test(entity.created().toLocalDate())) && matches(entity.text(), text);
        }
    }

    static class CommentForm extends ItemForm<Comment, CommentsView> {
        DateTimePicker created;
        TextField author;
        AsciiDocField text;

        public CommentForm(@NotNull CommentsView parent) {
            super(parent);
            init();
        }

        @Override
        public void create() {
            super.create();
            created.setValue(LocalDateTime.now());
        }

        @Override
        public void duplicate(@NotNull Comment entity) {
            super.duplicate(entity);
            created.setValue(LocalDateTime.now());
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
            parent().provider().replace(entity, comment);
            return comment;
        }

        private void init() {
            FormLayout layout = new FormLayout();
            created = new DateTimePicker(CREATED);
            created.setReadOnly(true);
            author = new TextField(AUTHOR);
            author.setRequired(true);
            text = new AsciiDocField(TEXT);
            layout.add(created, author, text);
            layout.setColspan(text, 3);
            layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("320px", 2), new FormLayout.ResponsiveStep("500px", 3));
            form().add(layout, createButtonBar());
            binder().forField(created).bind(Comment::created, null);
            binder().forField(author).bind(Comment::author, null);
            binder().forField(text).bind(Comment::text, null);
        }
    }

    public CommentsView(@NotNull BoundedDomain<?, ?, ?> domain, @NotNull Mode mode) {
        super(Comment.class, domain, ProviderInMemory.of(), new CommentFilter(), mode);
        form(() -> new CommentForm(this));
        init();
    }

    private void init() {
        setHeight("15em");
        Grid<Comment> grid = grid();
        grid.addColumn(Comment::created).setKey(CREATED).setHeader(CREATED_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Comment::author).setKey(AUTHOR).setHeader(AUTHOR_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("10em");
        grid.addColumn(Comment::text).setKey(TEXT).setHeader(TEXT_LABEL).setSortable(true).setResizable(true).setFlexGrow(0).setWidth("25em");

        if (filter() instanceof CommentFilter filter) {
            HeaderRow headerRow = createHeaderRow();
            headerRow.getCell(grid.getColumnByKey(CREATED)).setComponent(createDateRangeField(filter::range));
            headerRow.getCell(grid.getColumnByKey(AUTHOR)).setComponent(createTextFilterField(filter::author));
            headerRow.getCell(grid.getColumnByKey(TEXT)).setComponent(createTextFilterField(filter::text));
        }
        buildMenu();
    }

    private String username() {
        return (VaadinSession.getCurrent() != null) ? (String) VaadinSession.getCurrent().getAttribute("username") : null;
    }
}
