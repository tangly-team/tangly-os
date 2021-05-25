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

package net.tangly.ui.canvases.events;

import com.vaadin.flow.component.ComponentEvent;
import net.tangly.ui.canvases.Canvas;

public class MouseEvent extends ComponentEvent<Canvas> {
    private int offsetX;
    private int offsetY;
    private int button;

    public MouseEvent(Canvas source, boolean fromClient, int clientX, int clientY, int button) {
        super(source, fromClient);
        this.offsetX = clientX;
        this.offsetY = clientY;
        this.button = button;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }
}
