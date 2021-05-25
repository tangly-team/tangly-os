/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.products;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "net.tangly.erp.products")
class ArchitectureTest {
    static final String SERVICE = "Service";
    static final String PORTS = "Ports";
    static final String DOMAIN = "Domain";

    @ArchTest
    static final ArchRule layersRule =
        layeredArchitecture().layer(DOMAIN).definedBy("..domain..").layer(SERVICE).definedBy("..services..").layer(PORTS).definedBy("..ports..")
            .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(SERVICE, PORTS).whereLayer(SERVICE).mayOnlyBeAccessedByLayers(PORTS).whereLayer(PORTS)
            .mayNotBeAccessedByAnyLayer();
}



