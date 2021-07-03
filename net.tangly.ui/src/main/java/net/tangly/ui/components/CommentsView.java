/*
 * Copyright 2006-2021 Marcel Baumann
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

import java.time.LocalDateTime;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.VaadinSession;
import net.tangly.core.Comment;
import net.tangly.core.HasComments;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.ui.markdown.MarkdownArea;
import org.jetbrains.annotations.NotNull;

/**
 * The comments view is a Crud view with all the comments defined for an object implementing the {@link HasComments}. Edition functions are provided to add,
 * delete, and view individual comments. Update function is not supported because comments are immutable objects. Immutable objects must be explicitly be
 * deleted before an new version can be added. This approach supports auditing approaches.
 */
public class CommentsView extends EntitiesView<Comment> {
    private final transient HasComments hasComments;
    private final DateTimePicker created;
    private final TextField author;
    private final MarkdownArea text;

    public CommentsView(@NotNull Mode mode, @NotNull HasComments entity) {
        super(Comment.class, mode, ProviderInMemory.of(entity.comments()));
        this.hasComments = entity;
        created = new DateTimePicker("Created");
        created.setRequiredIndicatorVisible(true);
        created.setReadOnly(true);
        author = VaadinUtils.createTextField("Author", "author");
        author.setRequired(true);
        author.setReadOnly(true);
        text = new MarkdownArea();
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<Comment> grid = grid();
        grid.setPageSize(5);
        grid.addColumn(Comment::created).setKey("created").setHeader("Created").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
            .setFrozen(true);
        grid.addColumn(Comment::author).setKey("author").setHeader("Author").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Comment::text).setKey("text").setHeader("Text").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        addAndExpand(grid(), gridButtons());
    }

    @Override
    protected FormLayout fillForm(@NotNull Operation operation, Comment entity, FormLayout form) {
        boolean readonly = operation.isReadOnly();
        text.setHeight("8em");
        text.setReadOnly(readonly);
        form.add(created, author, new HtmlComponent("br"), text);
        form.setColspan(text, 2);
        if (readonly) {
            Binder<Comment> binder = new Binder<>(Comment.class);
            binder.bind(created, Comment::created, null);
            binder.bind(author, Comment::author, null);
            binder.bind(text, Comment::text, null);
            binder.readBean(entity);
        } else {
            created.setValue(LocalDateTime.now());
            author.setValue(username());
            text.setValue(entity.text());
        }
        return form;
    }

    @Override
    protected Comment updateOrCreate(Comment entity) {
        return Comment.of(created.getValue(), author.getValue(), text.getValue());
    }

    @Override
    public Comment formCompleted(@NotNull Operation operation, Comment entity) {
        return switch (operation) {
            case CREATE -> {
                Comment comment = updateOrCreate(entity);
                hasComments.add(comment);
                yield comment;
            }
            case UPDATE -> {
                Comment comment = updateOrCreate(entity);
                hasComments.remove(entity);
                hasComments.add(comment);
                yield comment;
            }
            case DELETE -> {
                hasComments.remove(entity);
                yield entity;
            }
            default -> entity;
        };
    }

    private String username() {
        return (VaadinSession.getCurrent() != null) ? (String) VaadinSession.getCurrent().getAttribute("username") : null;
    }
}
