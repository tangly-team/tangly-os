/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

/**
 * The paginated grid adds pagination feature to the Vaadin 14+ Grid component. To do so, it extends the existing Vaadin 14 Grid and creates a custom
 * PaginatedGrid
 *
 * <p>>An internal LitPagination component is created which handles the pagination and navigation through pages. To use the pagination feature on the grid you
 * just have to use @link{net.tangly.components.grids.PaginatedGrid} component instead of the standard Vaadin 14+ Grid as follows:</p
 *
 * <pre>
 * @code{
 *  PaginatedGrid<Address> grid = new PaginatedGrid<>();
 *
 * 	grid.addColumn(Address::getId).setHeader("ID");
 * 	grid.addColumn(Address::getCountry).setHeader("Country").setSortable(true);
 * 	grid.addColumn(Address::getState).setHeader("State").setSortable(true);
 * 	grid.addColumn(Address::getName).setHeader("Name").setSortable(true);
 * 	grid.addColumn(Address::getAddress).setHeader("Address").setSortable(true);
 *
 * 	grid.setItems(dataProvider.getItems());
 *
 * 	// Sets the max number of items to be rendered on the grid for each page
 * 	grid.setPageSize(16);
 *
 * 	// Sets how many pages should be visible on the pagination before and/or after the current selected page
 * 	grid.setPaginatorSize(5);
 *    }
 * </pre>
 * <p>If you want a specific page to be selected on the pagination:</p>
 * <pre>
 * @code {
 * 	grid.setPage(4);
 * }
 * </pre>
 */
package net.tangly.components.grids;
