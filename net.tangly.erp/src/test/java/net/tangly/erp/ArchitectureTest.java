/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.erp;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "net.tangly.erp")
public class ArchitectureTest {
    @ArchTest
    public static final ArchRule ruleCrmIf = noClasses().that().resideInAPackage("net.tangly.commons.erp.crm").should().dependOnClassesThat()
            .resideInAPackage("net.tanglu.erp.crm.ports");
    @ArchTest
    public static final ArchRule ruleLedgerIf = noClasses().that().resideInAPackage("net.tangly.commons.erp.ledger").should().dependOnClassesThat()
            .resideInAPackage("net.tanglu.erp.ledger.ports");
}
