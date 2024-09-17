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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserManualView extends HorizontalLayout implements View {
    public static final String USER_MANUAL = "user-manual.html";
    private final BoundedDomainUi<?> domain;

    public UserManualView(BoundedDomainUi<?> domain) {
        this.domain = domain;
        try {

            Path path = Path.of(domain.domain().directory().resources(domain.name()), USER_MANUAL);
            if (!Files.exists(path)) {
                path = Paths.get(getClass().getClassLoader().getResource(USER_MANUAL).toURI());
            }
            String html = Files.readString(path);
            add(new Html(html));
            setSizeFull();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean readonly() {
        return true;
    }

    @Override
    public void readonly(boolean readonly) {
    }

    @Override
    public void refresh() {
    }
}
