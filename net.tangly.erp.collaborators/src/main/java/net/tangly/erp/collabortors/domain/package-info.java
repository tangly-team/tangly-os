/*
 * Copyright 2021-2023 Marcel Baumann
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
 * The domain defines the abstractions to compute the salary, social deductions and legal salary documents for collaborators.
 * <ul>
 * <li>Swiss social insurances are defined and computed at company level. Salary is the parameter.The insurance company defines the administrative costs.</li>
 * <li>Swiss accident insurances are defined and computed at company level. Salary and potentially gender are parameters.</li>
 * <li>Siwss pension funds are computed on an individual base. Salary and age are paremeters, only the insurance company knows the exact formula.</li>
 * </ul>
 * The templates and fields of the yearly salary declaration are defined by the Swiss federal government. Templates are updated over time to reflect changes such as the format
 * of the social number.
 */
package net.tangly.erp.collabortors.domain;
