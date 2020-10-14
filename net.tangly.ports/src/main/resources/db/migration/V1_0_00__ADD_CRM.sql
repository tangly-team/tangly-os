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

CREATE SCHEMA crm;

CREATE TABLE crm.naturalEntities (
    oid       BIGINT PRIMARY KEY,
    id        VARCHAR(64) NOT NULL UNIQUE,
    name      VARCHAR(64),
    firstname VARCHAR(64),
    lastname  varchar(64),
    fromDate  DATE,
    toDate    DATE,
    text      CLOB,
    tags      CLOB
                                 );

CREATE TABLE crm.legalEntities (
    oid      BIGINT PRIMARY KEY,
    id       VARCHAR(64) NOT NULL UNIQUE,
    name     VARCHAR(64),
    fromDate DATE,
    toDate   DATE,
    text     CLOB,
    tags     CLOB
                               );

CREATE TABLE crm.employees (
    oid             BIGINT PRIMARY KEY,
    id              VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(64),
    title           VARCHAR(64),
    fromDate        DATE,
    toDate          DATE,
    text            CLOB,
    tags            CLOB,
    personOid       BIGINT      NOT NULL,
    organizationOid BIGINT      NOT NULL,
    FOREIGN KEY (personOid) REFERENCES crm.naturalEntities (oid),
    FOREIGN KEY (organizationOid) REFERENCES crm.legalEntities (oid)
                           );

CREATE TABLE crm.contracts (
    oid              BIGINT PRIMARY KEY,
    id               VARCHAR(64) NOT NULL UNIQUE,
    name             VARCHAR(64),
    amountWithoutVat DECIMAL(12, 2),
    fromDate         DATE,
    toDate           DATE,
    text             CLOB,
    sellerOid        BIGINT      NOT NULL,
    selleeOid        BIGINT      NOT NULL,
    tags             CLOB,
    FOREIGN KEY (sellerOid) REFERENCES crm.legalEntities (oid),
    FOREIGN KEY (selleeOid) REFERENCES crm.legalEntities (oid)
                           );

CREATE TABLE crm.interactions (
    oid         BIGINT PRIMARY KEY,
    id          VARCHAR(64) NOT NULL UNIQUE,
    name        VARCHAR(64),
    fromDate    DATE,
    toDate      DATE,
    code        INT,
    legalEntity BIGINT      NOT NULL,
    potential   DECIMAL(5, 2),
    probability DECIMAL(5, 2),
    text        CLOB
                              );

CREATE TABLE crm.activities (
    oid      BIGINT PRIMARY KEY,
    id       VARCHAR(64) NOT NULL UNIQUE,
    name     VARCHAR(64),
    fromDate DATE,
    toDate   DATE,
    code     INT,
    durationInMinutes INT,
    details CLOB
    text     CLOB
                            );

