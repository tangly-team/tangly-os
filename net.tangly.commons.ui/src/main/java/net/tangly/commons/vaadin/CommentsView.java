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

package net.tangly.commons.vaadin;

import java.time.LocalDateTime;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.HasComments;
import org.jetbrains.annotations.NotNull;

/**
 * The comments view is a Crud view with all the comments defined for an entity. Edition functions are provided to add, delete, and view individual
 * comments. Update function is not supported because comments are immutable objects. Immutable objects must be explicitly be deleted before an new
 * version can be added. This approach supports auditing approaches.
 */
public class CommentsView extends Crud<Comment> implements CrudForm<Comment>, CrudActionsListener<Comment> {
    private final transient HasComments hasItems;
    private DateTimePicker created;
    private TextField author;
    private TextArea text;


    public CommentsView(@NotNull HasComments entity) {
        super(Comment.class, Mode.IMMUTABLE, CommentsView::defineGrid, new ListDataProvider<>(entity.comments()));
        initialize(this, this);
        this.hasItems = entity;
    }

    public static void defineGrid(@NotNull Grid<Comment> grid) {
        initialize(grid);
        grid.addColumn(Comment::created).setKey("created").setHeader("Created").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
                .setFrozen(true);
        grid.addColumn(Comment::author).setKey("author").setHeader("Author").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Comment::text).setKey("text").setHeader("Text").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
    }

    @Override
    public FormLayout createForm(Operation operation, Comment entity) {
        boolean readonly = Operation.isReadOnly(operation);

        created = new DateTimePicker("Created");
        created.setValue(LocalDateTime.now());
        created.setReadOnly(true);

        author = CrudForm.createTextField("Author", "author", readonly, true);
        author.setRequired(true);

        text = new TextArea("Text");
        text.setHeight("8em");
        text.getStyle().set("colspan", "4");
        text.setReadOnly(readonly);
        text.setRequired(true);

        if (readonly) {
            Binder<Comment> binder = new Binder<>(Comment.class);
            binder.bind(created, Comment::created, null);
            binder.bind(author, Comment::author, null);
            binder.bind(text, Comment::text, null);
            binder.readBean(entity);
        }

        FormLayout form = new FormLayout(created, author, new HtmlComponent("br"), text);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3)
        );
        return form;
    }

    @Override
    public Comment formCompleted(Operation operation, Comment entity) {
        switch (operation) {
            case CREATE:
                return new Comment(created.getValue(), author.getValue(), text.getValue());
            case DELETE:
                break;
        }
        return entity;
    }

    @Override
    public void entityAdded(DataProvider<Comment, ?> dataProvider, Comment entity) {
        hasItems.add(entity);

    }

    @Override
    public void entityDeleted(DataProvider<Comment, ?> dataProvider, Comment entity) {
        hasItems.remove(entity);
    }
}
