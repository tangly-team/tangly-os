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

package net.tangly.core.crm;

/**
 * Defines the photography of a natural entity. Pictures are powerful helpers for personal interactions.
 * @param data binary data of the photography
 */
public record Photo(byte[] data) {
    public static Photo of(byte[] data) {
        return new Photo(data);
    }
}