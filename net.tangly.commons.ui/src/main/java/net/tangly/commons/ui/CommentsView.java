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

package net.tangly.commons.ui;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.HasComments;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.BiPredicate;

public class CommentsView extends GridFormView<Comment> {
    private final transient HasComments entity;
    private LocalDateTime createdDateTime;
    private DatePicker createdDate;
    private TextField createdTime;
    private TextField author;
    private TextArea text;

    public CommentsView(HasComments entity) {
        super(true, "Filter by text...");
        this.entity = entity;
        updateFilteredItems(null);
    }

    @Override
    protected Collection<Comment> items() {
        return entity.comments();
    }

    @Override
    protected BiPredicate<Comment, String> filter() {
        return (o, pattern) -> o.text().contains(pattern);
    }

    @Override
    protected Grid<Comment> createGrid() {
        Grid<Comment> grid = new Grid<>();
        grid.addColumn(Comment::created).setHeader("Created").setSortable(true);
        grid.addColumn(Comment::author).setHeader("Author").setSortable(true);
        grid.addColumn(Comment::text).setHeader("Text");
        return grid;
    }

    @Override
    protected Comment saveItem() {
        Comment updatedItem = Comment.of(createdDateTime, author.getValue(), text.getValue());
        entity.remove(selectedItem);
        entity.add(updatedItem);
        return updatedItem;
    }

    @Override
    protected void detailItem() {
    }

    @Override
    protected Comment deleteItem() {
        entity.remove(selectedItem);
        return selectedItem;
    }

    @Override
    protected void addItem() {
        createdDateTime = LocalDateTime.now();
        createdDate.setValue(createdDateTime.toLocalDate());
        createdTime.setValue((createdDateTime.toLocalTime().toString()));
        createdDate.setEnabled(false);
        createdTime.setEnabled(false);
        author.setEnabled(true);
        text.setEnabled(true);
    }

    @Override
    protected FormLayout createForm() {
        FormLayout form = new FormLayout();
        createdDate = new DatePicker("Creation Date");
        createdDate.setEnabled(false);
        createdTime = new TextField("Creation Time");
        createdTime.setEnabled(false);
        author = new TextField("Author");
        author.setEnabled(false);
        text = new TextArea("Text");
        text.setEnabled(false);
        text.setHeight("8em");
        form.add(createdDate, createdTime, author, text, createButtons());
        return form;
    }

    @Override
    protected void selectItemDetails(Comment item) {
        this.selectedItem = item;
        createdDate.setEnabled(false);
        createdTime.setEnabled(false);
        if (item == null) {
            createdDate.setValue(createdDate.getEmptyValue());
            createdTime.setValue(createdTime.getEmptyValue());
            author.setValue(author.getEmptyValue());
            author.setEnabled(false);
            text.setValue(text.getEmptyValue());
            text.setEnabled(false);
        } else {
            createdDate.setValue(selectedItem.created().toLocalDate());
            createdTime.setValue((selectedItem.created().toLocalTime().toString()));
            author.setValue(selectedItem.author());
            author.setEnabled(false);
            text.setValue(selectedItem.text());
            text.setEnabled(true);
        }
    }
}
