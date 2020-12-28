package net.tangly.components;

import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.shared.Registration;

/**
 * Grid component where scrolling feature is replaced with a pagination component. The grid provider is a list data provider containing the items to be shown in
 * the current grid page. The data provider is stored locally to retrieve the items to be displayed when a page change occurs.
 *
 * @param <T> type of the entities displayed in the grid
 */
public class PaginatedGrid<T> extends Grid<T> {
    private LitPagination pagination;
    private Component paginationContainer;
    private PaginationLocation paginationLocation;
    private DataProvider<T, ?> dataProvider;

    public PaginatedGrid() {
        init();
    }

    public PaginatedGrid(Class<T> beanType) {
        super(beanType);
        init();
    }

    private void init() {
        pagination = new LitPagination();
        paginationLocation = PaginationLocation.BOTTOM;
        dataProvider = super.getDataProvider();
        setHeightByRows(true);
        pagination.addPageChangeListener(e -> doCalcs(e.newPage()));
        addSortListener(e -> doCalcs(pagination.getPage()));
    }

    /**
     * Returns the data provider associated with the grid.
     *
     * @return data provider of the grid
     */
    public DataProvider<T, ?> dataProvider() {
        return dataProvider;
    }

    /**
     * Sets a container component for the pagination component to be placed within. If a container is set the PaginationLocation will be ignored.
     *
     * @param paginationContainer
     */
    public void setPaginationContainer(Component paginationContainer) {
        this.paginationContainer = paginationContainer;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Span wrapper = new Span(pagination);
        wrapper.getElement().getStyle().set("width", "100%");
        wrapper.getElement().getStyle().set("display", "flex");
        wrapper.getElement().getStyle().set("justify-content", "center");
        if (paginationContainer != null) {
            paginationContainer.getElement().insertChild(0, wrapper.getElement());
        } else {
            getParent().ifPresent(p -> {
                int indexOfChild = p.getElement().indexOfChild(this.getElement());
                //this moves the pagination element below the grid
                if (paginationLocation == PaginationLocation.BOTTOM) {
                    indexOfChild++;
                }
                p.getElement().insertChild(indexOfChild, wrapper.getElement());
            });
        }
        doCalcs(0);
    }

    private void doCalcs(int newPage) {
        int offset = (newPage > 0) ? (newPage - 1) * this.getPageSize() : 0;
        InnerQuery query = new InnerQuery<>(offset);
        pagination.getTotal(dataProvider.size(query));
        super.setDataProvider(DataProvider.fromStream(dataProvider.fetch(query)));
    }

    public void refreshPaginator() {
        if (pagination != null) {
            pagination.pageSize(getPageSize());
            pagination.setPage(1);
            if (dataProvider != null) {
                doCalcs(pagination.getPage());
            }
            pagination.refresh();
        }
    }

    @Override
    public void setPageSize(int pageSize) {
        super.setPageSize(pageSize);
        refreshPaginator();
    }


    public int page() {
        return pagination.getPage();
    }

    public void page(int page) {
        pagination.setPage(page);
    }

    /**
     * @return the location of the pagination element
     */
    public PaginationLocation paginationLocation() {
        return paginationLocation;
    }

    /**
     * setter of pagination location
     *
     * @param paginationLocation either PaginationLocation.TOP or PaginationLocation.BOTTOM
     */
    public void paginationLocation(PaginationLocation paginationLocation) {
        this.paginationLocation = paginationLocation;
    }

    @Override
    public void setHeightByRows(boolean heightByRows) {
        super.setHeightByRows(true);
    }

    /**
     * Sets the count of the pages displayed before or after the current page.
     *
     * @param size
     */
    public void paginatorSize(int size) {
        pagination.setPage(1);
        pagination.paginatorSize(size);
        pagination.refresh();
    }

    /**
     * Sets the texts they are displayed on the paginator. This method is useful when localization of the component is applicable.
     *
     * @param pageText the text to display for the `Page` term in the Paginator
     * @param ofText   the text to display for the `of` term in the Paginator
     */
    public void paginatorTexts(String pageText, String ofText) {
        pagination.pageText(pageText);
        pagination.ofText(ofText);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        Objects.requireNonNull(dataProvider, "DataProvider shoul not be null!");
        if (!Objects.equals(this.dataProvider, dataProvider)) {
            this.dataProvider = dataProvider;
            this.dataProvider.addDataProviderListener(event -> {
                refreshPaginator();
            });
            refreshPaginator();
        }
    }

    /**
     * change visibility of pagination component
     *
     * @param visibility boolean to change visibility
     */
    public void paginationVisibility(boolean visibility) {
        pagination.setVisible(visibility);
    }

    /**
     * Adds a ComponentEventListener to be notified with a PageChangeEvent each time the selected page changes.
     *
     * @param listener to be added
     * @return registration to unregister the listener from the component
     */
    public Registration addPageChangeListener(ComponentEventListener<LitPagination.PageChangeEvent> listener) {
        return pagination.addPageChangeListener(listener);
    }

    /**
     * Enumeration to define the location of the element relative to the grid
     **/
    public enum PaginationLocation {TOP, BOTTOM}

    private class InnerQuery<F> extends Query<T, F> {
        InnerQuery(int offset) {
            super(offset, PaginatedGrid.this.getPageSize(), getDataCommunicator().getBackEndSorting(), getDataCommunicator().getInMemorySorting(), null);
        }

        InnerQuery(int offset, List<QuerySortOrder> sortOrders, SerializableComparator<T> serializableComparator) {
            super(offset, PaginatedGrid.this.getPageSize(), sortOrders, serializableComparator, null);
        }
    }
}
