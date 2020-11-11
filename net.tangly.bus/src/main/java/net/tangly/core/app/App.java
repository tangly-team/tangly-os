/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.core.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class App {
    public static Map<String, String> readConfiguration(Path configurationFile) {
        try (InputStream stream = Files.newInputStream(configurationFile)) {
            Properties properties = new Properties();
            properties.load(stream);
            return properties.keySet().stream().map(Object::toString).collect(Collectors.toMap(e -> e, properties::getProperty));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeConfiguration(Map<String, String> configuration, Path configurationFile) {
        try (OutputStream stream = Files.newOutputStream(configurationFile)) {
            Properties properties = new Properties();
            properties.putAll(configuration);
            properties.store(stream, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
