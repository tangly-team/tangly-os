/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

import { css, html, LitElement } from 'lit';
import {customElement} from 'lit/decorators.js';

import '@polymer/paper-button/paper-button';
import '@polymer/paper-icon-button/paper-icon-button';
import '@polymer/iron-iconset-svg';

class LitPagination extends LitElement{

    static get styles() {
        return css`
        :host {
            display: block;
            font-size: 14px;
        }

        div.paginator-page-container {
            display: block;
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-center-justified;
            @apply --layout-center-center;
        }

        :host paper-button {
            margin: 0px 4px;
            padding: 2px 8px;
            /*margin: 0px;*/
            position: relative;
            min-width: 30px;

        }

        :host paper-button {
            color: var(--primary-text-color);
            color: var(--lumo-body-text-color);
            color: var(--material-body-text-color);
            background-color: transparent;
            border-radius: 16px;
        }

        :host span {
            margin: 0px 4px;
        }
    `;
      }

    static get properties() {
        return {
            /** Per-page limit of the elements. */
            limit: {
                type: Number,
                reflect: true,
                attribute: true
            },
            /** Total count of the elements. */
            total: {
                type: Number,
                reflect: true,
                attribute: true
            },
            /** Current page. */
            page: {
                type: Number,
                reflect: true,
                attribute: true
            },
            /** Count of the pages displayed before or after the current page. */
            size: {
                type: Number,
                reflect: true,
                attribute: true
            },
            /** Number of paginated pages. */
            pages: {
                type: Number
            },
            /** Has pages before the current page. */
            hasBefore: {
                type: Boolean
            },
            /** Has pages after the current page. */
            hasNext: {
                type: Boolean
            },
            /** Has pages. */
            hasPages: {
                type: Boolean
            },
            /** Displayed page elements */
            items: {
                type: Array
            },
            pageText: {
                type: String
            },
            ofText: {
                type: String
            }

        };
    }

    constructor(){
        super();
        this.limit = 2;
        this.page = 1;
        this.size = 2;
        this.items = {};
        this.total = 20;
        this.hasBefore = this.computeBefore(this.page, this.pages);
        this.hasNext = this.computeNext(this.page, this.pages);
        this.hasPages = this.computeHasPage(this.items.size, this.total);
        this.pageText = "Page";
        this.ofText = "of"
    }

    set page(val){
        let oldVal = this._page;
        this._page = val;
        this.requestUpdate('page', oldVal);
        this.onPageChange(this._page, oldVal);
        this.observePageCount(this._page, this.limit, this.total);

        this.hasBefore = this.computeBefore(val, this.pages);
        this.hasNext = this.computeNext(val, this.pages);

        this.updateNavigationButtonsState()
    }

    get page(){
        return this._page;
    }

    set limit(val){
        let oldVal = this._limit;
        this._limit = val;
        this.requestUpdate('limit', oldVal);
        this.observePageCount(this.page, this._limit, this.total);
    }

    get limit(){
        return this._limit;
    }

    set total(val){
        let oldVal = this._total;
        this._total = val;
        this.requestUpdate('total', oldVal);
        this.observePageCount(this.page, this.limit, this._total);
    }

    get total(){
        return this._total;
    }

    set size(val){
        let oldVal = this._size;
        this._size = val;
        this.requestUpdate('size', oldVal);
    }

    get size(){
        return this._size;
    }

    render(){
        return html`
        <iron-iconset-svg name="pagination-icons" size="24">
            <svg>
                <defs>
                    <g id="fast-forward">
                        <path d="M4 18l8.5-6L4 6v12zm9-12v12l8.5-6L13 6z"></path>
                    </g>
                    <g id="fast-rewind">
                        <path d="M11 18V6l-8.5 6 8.5 6zm.5-6l8.5 6V6l-8.5 6z"></path>
                    </g>
                    <g id="navigate-before">
                        <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"></path>
                    </g>
                    <g id="navigate-next">
                        <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"></path>
                    </g>
                </defs>
            </svg>
        </iron-iconset-svg>
        <div class="paginator-page-container" ?hidden="${!this.hasPage}">
            <paper-icon-button id="fastRewindId" icon="pagination-icons:fast-rewind" @click="${event => this.onBegin()}"></paper-icon-button>
            <paper-icon-button id="navigateBeforeId" icon="pagination-icons:navigate-before" @click="${event => this.onBefore()}"></paper-icon-button>
            <span>${this.pageText}</span>
            ${this.items.map(item => html`
                <paper-button
                        raised="${!this.isCurrent(item, this.page)}"
                        ?disabled="${this.isCurrent(item, this.page)}"
                        @click="${event => this.onChange(item)}">
                    ${item}
                </paper-button>
            `)}

            <span>${this.ofText}</span>
            ${this.pages}
            <paper-icon-button id="navigateNextId" icon="pagination-icons:navigate-next" @click=${event => this.onNext()}></paper-icon-button>
            <paper-icon-button id="fastForwardId" icon="pagination-icons:fast-forward" @click=${event => this.onEnd()}></paper-icon-button>
        </div>
        `
    }

    updated(changedProperties) {
        this.updateNavigationButtonsState();
    }


    computeBefore(page, pages) {
        return page > 1;
    }

    computeNext(page, pages) {
        return page < pages;
    }

    computeHasPage(itemsLength, total) {
        return itemsLength < total;
    }

    observePageCount(page, limit, total) {
        if (limit && total) {
            this.pages = parseInt(Math.ceil(parseFloat(total) / parseFloat(limit)));
        }

        if (page && limit && total) {
            let items = [];
            let firstIndex = this._firstIndex(page, this.size);
            let lastIndex = this._lastIndex(page, this.size);
            for (var num = firstIndex; num <= lastIndex; num++) {
                items.push(num);
            }
            this.items = items;
        }
    }

    onPageChange(newValue, oldValue){
        this.dispatchEvent(new CustomEvent('page-change', {detail: {newPage: newValue, oldPage:oldValue}}));
    }

    updateNavigationButtonsState() {
        if(this.shadowRoot) {
            let fast_rewind_button = this.shadowRoot.getElementById('fastRewindId');
            let navigate_before_button = this.shadowRoot.getElementById('navigateBeforeId');
            let navigate_next_button = this.shadowRoot.getElementById('navigateNextId');
            let fast_forward_button = this.shadowRoot.getElementById('fastForwardId');

            if (fast_rewind_button && navigate_before_button && navigate_next_button && fast_forward_button) {
                if (this.hasNext) {
                    navigate_next_button.removeAttribute('disabled');
                    fast_forward_button.removeAttribute('disabled');
                } else {
                    navigate_next_button.setAttribute('disabled', 'disabled');
                    fast_forward_button.setAttribute('disabled', 'disabled');
                }

                if (this.hasBefore) {
                    fast_rewind_button.removeAttribute('disabled');
                    navigate_before_button.removeAttribute('disabled');
                } else {
                    fast_rewind_button.setAttribute('disabled', 'disabled');
                    navigate_before_button.setAttribute('disabled', 'disabled');
                }
            }
        }
    }

    _firstIndex(page, size) {
        let index = page - size;
        if (index < 1) {
            return 1;
        } else {
            return index;
        }
    }

    _lastIndex(page, size) {
        let index = parseInt(page) + parseInt(size);
        if (index > this.pages) {
            return this.pages;
        } else {
            return index;
        }
    }

    isCurrent(index, page) {
        return index === page;
    }

    onChange(item) {
        this.page = item;
        this.requestUpdate();
    }

    onBefore(event) {
        this.page = this.page > 1 ? this.page - 1 : 1;
    }

    onNext(event) {
        this.page = this.page < this.pages ? parseInt(this.page) + 1 : this.pages;
    }

    onBegin(event) {
        this.page = 1;
    }

    onEnd(event) {
        this.page = this.pages;
    }
}

customElements.define('lit-pagination', LitPagination);
