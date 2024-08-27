/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 *
 */

/**
 * Provides classes for a programmer first behavior driven framework build on the features of Junit 5.
 */
module net.tangly.commons {
    exports net.tangly.commons.generator;
    exports net.tangly.commons.imap;
    exports net.tangly.commons.lang;
    exports net.tangly.commons.lang.exceptions;
    exports net.tangly.commons.lang.functional;
    exports net.tangly.commons.logger;
    exports net.tangly.commons.utilities;

    requires org.apache.logging.log4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.networknt.schema;
    requires jakarta.mail;
    requires org.eclipse.angus.mail;
    requires org.asciidoctor.asciidoctorj.api;
    requires org.asciidoctor.asciidoctorj;
    requires static transitive org.jetbrains.annotations;
}
