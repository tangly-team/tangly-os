package net.tangly.commons.codes;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

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
        JsonCode(@JsonProperty("id") int id, @JsonProperty("code") String code, @JsonProperty("enabled") boolean enabled) {
            super(id, code, enabled);
        }
    }

    @Test
    void testJsonCodeTest() throws IOException {
        CodeType<JsonCode> type = CodeType.of(JsonCode.class,
                List.of(new JsonCode(1, "one", true), new JsonCode(2, "two", true), new JsonCode(3, "three", true), new JsonCode(4, "four", true),
                        new JsonCode(5, "five", true)));
        asserts(type);
        String json = CodeJsonHdl.writeCodeToJson(type);

        type = CodeJsonHdl.readCodesFromJson(type.clazz(), json);
        asserts(type);

    }

    private void asserts(CodeType<JsonCode> type) {
        assertThat(type.codes()).isNotEmpty();
        assertThat(type.codes().size()).isEqualTo(5);
        assertThat(type.activeCodes().size()).isEqualTo(5);
        assertThat(type.inactiveCodes().size()).isEqualTo(0);
        assertThat(type.findCode(1).isPresent()).isTrue();
        assertThat(type.findCode("one").isPresent()).isTrue();
    }
}
