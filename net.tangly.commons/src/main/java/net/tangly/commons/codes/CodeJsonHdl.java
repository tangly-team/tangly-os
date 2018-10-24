package net.tangly.commons.codes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;
import java.util.List;

public class CodeJsonHdl {
    public static <T extends Code> CodeType<T> readCodesFromJson(Class<T> clazz, String json) throws IOException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JavaType itemType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return CodeType.of(clazz, mapper.readValue(json, itemType));
    }

    public static <T extends Code> String writeCodeToJson(CodeType<T> type) throws IOException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(type.codes());
    }
}
