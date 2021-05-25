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

package net.tangly.ui.app.domain;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jetbrains.annotations.NotNull;

public abstract class CmdDialog extends Dialog implements Cmd {
    private final String width;

    public CmdDialog(@NotNull String width) {
        this.width = width;
    }

    @Override
    public void execute() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);
        layout.add(form());
        add(layout);
        this.setWidth(width);
        open();
    }

    protected abstract FormLayout form();
}
