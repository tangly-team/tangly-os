/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.fsm.utilities;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The checker interface defines the contract for all finite state machine syntactic and semantic checkers.
 */
public interface Checker {
    enum Level {

        FATAL("FATAL"), ERROR("ERROR"), WARNING("WARN "), INFO("INFO"), DEBUG("DEBUG"), TRACE("TRACE");

        private final String text;

        Level(String text) {
            this.text = text;
        }

        String getText() {
            return text;
        }
    }

    /**
     * Creates a message for the checker.
     *
     * @param level      the level of the message
     * @param bundle     the bundle containing the localized text
     * @param key        the key to select the localized text
     * @param parameters the parameters of the message
     * @return the formatted localized message
     */
    default String createMessage(Level level, ResourceBundle bundle, String key, Object... parameters) {
        MessageFormat formatter = new MessageFormat("");
        formatter.applyPattern(bundle.getString(key));
        return "%s-%s-%s".formatted(level.getText(), key, formatter.format(parameters));
    }

    /**
     * Creates an error message for the checker.
     *
     * @param bundle     the bundle containing the localized text
     * @param key        the key to select the localized text
     * @param parameters the parameters of the message
     * @return the formatted localized message
     */
    default String createError(ResourceBundle bundle, String key, Object... parameters) {
        return createMessage(Level.ERROR, bundle, key, parameters);
    }
}
