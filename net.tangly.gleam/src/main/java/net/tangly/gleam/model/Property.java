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

package net.tangly.gleam.model;

public interface Property {
    enum PropertyMode {READ_ONLY, READ_WRITE, WRITE_ONLY}

    enum PropertyKind {PRIMITIVE, EMBEDDED_ONE_TO_ONE, ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE}

    String name();

    String description();

    <T> Class<T> type();

    boolean isHidden();

    boolean isMandatory();

    boolean isPrimaryKey();

    boolean isConstant();

    boolean isEmbedded();

    int columnSize();
}
