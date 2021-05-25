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

/**
 * This event is sent when an Image has completed loading into the browser.
 */
public class ImageLoadEvent extends ComponentEvent<Canvas> {
    protected String src;

    public ImageLoadEvent(Canvas source, boolean fromClient, String src) {
        super(source, fromClient);
        this.src = src;
    }

    /**
     * @return the image resource path
     */
    public String getSrc() {
        return src;
    }
}
