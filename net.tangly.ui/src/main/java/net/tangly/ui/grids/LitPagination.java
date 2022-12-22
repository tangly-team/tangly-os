/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.grids;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

@Tag("lit-pagination")
@NpmPackage(value = "@polymer/paper-button", version = "^3.0.1")
@NpmPackage(value = "@polymer/iron-iconset-svg", version = "^3.0.1")
@NpmPackage(value = "@polymer/paper-icon-button", version = "^3.0.2")
@NpmPackage(value = "lit-element", version = "^2.2.1")
@NpmPackage(value = "lit-html", version = "^1.1.2")
@JsModule("./src/lit-pagination.js")
class LitPagination extends Component implements LitPaginationModel {
    public LitPagination() {
        this.getTotal(2);
        pageSize(1);
        size(1);
    }

    /**
     * Gets the size of the page, that is number of elements a page could display.
     *
     * @return the size of the page
     * @see #pageSize()
     */
    public int pageSize() {
        return getLimit();
    }

    /**
     * Sets the page size, that is number of items a page could display.
     *
     * @param size new page size
     * @see #pageSize(int)
     */
    public void pageSize(int size) {
        getLimit(size);
    }

    /**
     * Sets the count of the pages displayed before or after the current page.
     *
     * @param size count of pages
     */
    public void paginatorSize(int size) {
        size(size);
    }

    public void refresh() {
        this.getElement().executeJs("$0.observePageCount($1,$2,$3)", this, this.getPage(), this.pageSize(), this.getTotal());
    }

    /**
     * Adds a ComponentEventListener to be notified with a PageChangeEvent each time the selected page changes.
     *
     * @param listener to be added
     * @return registration to unregister the listener from the component
     */
    protected Registration addPageChangeListener(ComponentEventListener<PageChangeEvent> listener) {
        return super.addListener(PageChangeEvent.class, listener);
    }

    /**
     * A page change event specialized class to be fired each time the selected page on the paginator changes.
     */
    @DomEvent("page-change")
    public static class PageChangeEvent extends ComponentEvent<LitPagination> {
        private final int newPage;
        private final int oldPage;

        public PageChangeEvent(LitPagination source, boolean fromClient, @EventData("event.detail.newPage") int newPage,
                               @EventData("event.detail.oldPage") int oldPage) {
            super(source, fromClient);
            this.newPage = newPage;
            this.oldPage = oldPage;
        }

        public int newPage() {
            return newPage;
        }

        public int oldPage() {
            return oldPage;
        }
    }
}
