/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

CREATE SCHEMA hrorm;

CREATE TABLE hrorm.authors (
    oid  BIGINT PRIMARY KEY,
    name VARCHAR(64)
                           );

CREATE TABLE hrorm.recipes (
    oid    BIGINT PRIMARY KEY,
    name   VARCHAR(64),
    author BIGINT,
    FOREIGN KEY (author) REFERENCES hrorm.authors (oid)
                           );

CREATE TABLE hrorm.ingredients (
    oid    BIGINT PRIMARY KEY,
    name   VARCHAR(64),
    amount BIGINT,
    recipe BIGINT,
    FOREIGN KEY (recipe) REFERENCES hrorm.recipes (oid)

                               );
