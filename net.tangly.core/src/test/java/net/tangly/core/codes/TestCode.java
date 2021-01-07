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

package net.tangly.core.codes;

/**
 * Test code for the various unit tests exercising code features.
 */
class TestCode extends CodeImp {
    /**
     * The class as no default constructor therefore the annotations to instruct Jackson to call the constructor with the corresponding arguments.
     *
     * @param id      code identifier of the instance
     * @param code    human readable code text of the instance
     * @param enabled flag indicating if the code is enabled
     */
    TestCode(int id, String code, boolean enabled) {
        super(id, code, enabled);
    }
}
