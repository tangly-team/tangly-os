/*
 * Copyright 2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TagHelper {
    public static final String NAMESPACE = "namespace";
    public static final String NAME = "name";
    public static final String TYPE = "class";
    public static final String KIND = "valueKinds";
    private TagHelper() {
    }

    /**
     * Utility method to read all tag type values of type string from a JSON file using the org.json library.
     *
     * @param path uri to the JSON file containing the code values
     * @return tag type list
     * @throws IOException if a file access error occurred
     */
    public static List<TagType> build(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Iterator<Object> iter = new JSONArray(new JSONTokener(reader)).iterator();
            List<TagType> tagTypes = new ArrayList<>();
            while (iter.hasNext()) {
                JSONObject value = (JSONObject) iter.next();
                var type = value.get(TYPE);
                if ("String".equals(type)) {
                    TagType<String> tagType = TagType.ofString(value.getString(NAMESPACE), value.getString(NAME),
                        TagType.ValueKinds.valueOf(value.getString(KIND)));
                    tagTypes.add(tagType);
                }
            }
            return tagTypes;
        }
    }
}
