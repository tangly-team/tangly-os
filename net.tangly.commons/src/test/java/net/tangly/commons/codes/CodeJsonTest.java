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

package net.tangly.commons.codes;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.tangly.commons.codes.imp.CodeImp;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CodeJsonTest {
    private static class JsonCode extends CodeImp implements Code {
        /**
         * The class as no default constructor therefore the annotations to instruct Jackson to call the constructor with the corresponding arguments.
         *
         * @param id      code identifier of the instance
         * @param code    human readable code text of the instance
         * @param enabled flag indicating if the code is enabled
         */
        JsonCode(int id, String code, boolean enabled) {
            super(id, code, enabled);
        }

        private JsonCode() {
        }
    }

    @Test
    void testJsonCodeTest() throws IOException {
        CodeType<JsonCode> type = build();
        asserts(type);
        String json = CodeJsonHdl.writeCodeToJson(type);

        type = CodeJsonHdl.readCodesFromJson(type.clazz(), json);
        asserts(type);

    }

    private void asserts(CodeType<JsonCode> type) {
        assertThat(type.codes()).isNotEmpty();
        assertThat(type.codes().size()).isEqualTo(6);
        assertThat(type.activeCodes().size()).isEqualTo(5);
        assertThat(type.inactiveCodes().size()).isEqualTo(1);
        assertThat(type.findCode(1).isPresent()).isTrue();
        assertThat(type.findCode("one").isPresent()).isTrue();
    }

    @Test
    void testFindAndEquivalence() {
        CodeType<JsonCode> type = build();
        Optional<JsonCode> code1 = type.findCode(1);
        Optional<JsonCode> code2 = type.findCode("one");
        if (code1.isPresent() && code2.isPresent()) {
            assertThat(code1.get() == code2.get()).isTrue();
            assertThat(code1.get().equals(code2.get())).isTrue();
            assertThat(code1.get().toString()).isEqualTo("{id=1, code=one, enabled=true}");
        }
    }

    private CodeType<JsonCode> build() {
        return CodeType.of(JsonCode.class,
                List.of(new JsonCode(1, "one", true), new JsonCode(2, "two", true), new JsonCode(3, "three", true), new JsonCode(4, "four", true),
                        new JsonCode(5, "five", true), new JsonCode(6, "six", false)));

    }
}
