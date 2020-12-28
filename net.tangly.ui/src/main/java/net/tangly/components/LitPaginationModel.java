package net.tangly.components;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Synchronize;

/**
 * Utility interface used to define lit-pagination properties
 */
interface LitPaginationModel extends HasElement {

    /**
     * Returns the selected page on the paginator.
     *
     * @return page
     */
    @Synchronize("page-change")
    default int getPage() {
        return Integer.parseInt(getElement().getProperty("page", "0"));
    }

    /**
     * Selects the page on the paginator.
     *
     * @param page to select
     */
    default void setPage(int page) {
        getElement().setProperty("page", Integer.toString(page));
    }

    /**
     * Returns the total number of items.
     *
     * @return total
     */
    @Synchronize("change")
    default int getTotal() {
        return Integer.parseInt(getElement().getProperty("total", "0"));
    }

    /**
     * Sets the total number of items from which number of pages gets calculated.
     *
     * @param total total number of items
     */
    default void getTotal(int total) {
        getElement().setProperty("total", Integer.toString(total));
    }


    /**
     * Sets the maximum number of items a page should display in order to calculated the number of pages.
     *
     * @param limit maximum number of items per page
     */
    default void getLimit(int limit) {
        getElement().setProperty("limit", Integer.toString(limit));
    }

    /**
     * Gets the actual number of items set to be displayed on for each page.
     *
     * @return maximum number of items per page
     */
    @Synchronize("change")
    default int getLimit() {
        return Integer.parseInt(getElement().getProperty("limit", "0"));
    }

    /**
     * Sets the count of the pages displayed before or after the current page.
     *
     * @param size count of pages
     */
    default void size(int size) {
        getElement().setProperty("size", Integer.toString(size));
    }

    /**
     * Sets the text to display for the `Page` term in the paginator.
     *
     * @param text text for the page term
     */
    default void pageText(String text) {
        getElement().setProperty("text", text);
    }

    /**
     * Sets the text to display for the `of` term in the paginator.
     *
     * @param text text of the of term
     */
    default void ofText(String text) {
        getElement().setProperty("text", text);
    }
}
