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

package net.tangly.core.crm;

/**
 * Defines the tag categories for tags accordingly to the VCard RFC. T
 * <p>he "work" and "home" values act like tags.  The "work" value implies that the property is
 * related to an individual's work place, while the "home" value implies that the property is related to an individual's personal life.  When neither "work" nor
 * "home" is present, it is implied that the property is related to both an individual's work place and personal life in the case that the KIND property's value
 * is "individual", or to none in other cases.</p>
 */
public enum VcardType {
    home, work, mobile
}

