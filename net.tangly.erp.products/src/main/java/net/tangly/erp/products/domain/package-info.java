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
 *
 */

/**
 * Provides core abstractions used in any commercial application in need product and project handling.
 * <p>The products package is designed as a bounded domain. All texts shall follow asciidoc conventions. The reason is that asciidoc provides a tool chain
 * to generate PDF, ebook or LateX documents. Markdown environment does not provide currently similar functionalities</p>
 * <p>A product defines a digital product development initiative. A product as a set of contracts financing the development.
 * An assignment defines who works on the product. An effort is a specific timed work item performed by a collaborator and invoiced to a contract budget. An expense is spending on
 * a project not related to a collaborator work such as travel expenses.</p>
 */

package net.tangly.erp.products.domain;
