/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import java.util.Optional;

/**
 * Mixin indicating the class has location data. A location is either a GIS position, a Plus Code, or a postal address.
 */
public interface HasLocation {
    /**
     * A GIS location indicating with two decimal degree values.
     */
    record GeoPosition(double longitude, double latitude) {
        public static GeoPosition of(double longitude, double latitude) {
            return new GeoPosition(longitude, latitude);
        }
    }

    /**
     * A Plus code location defined as a Plus Code value.
     */
    record PlusCode(String code) {
        public static PlusCode of(String code) {
            return new PlusCode(code);
        }
    }

    default Optional<Address> address() {
        return Optional.empty();
    }

    default Optional<GeoPosition> position() {
        return Optional.empty();
    }

    default Optional<PlusCode> plusCode() {
        return Optional.empty();
    }

    default boolean hasAddress() {
        return address().isPresent();
    }

    default boolean hasPosition() {
        return position().isPresent();
    }

    default boolean hasPlusCode() {
        return plusCode().isPresent();
    }
}
