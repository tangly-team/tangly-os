/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.markdown;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import net.tangly.ui.components.VaadinUtils;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Tag("markdown-area")
public class MarkdownField extends CustomField<String> {
    private final TextArea input = new TextArea();
    private final Div writeView = new Div(input);
    private final Div previewView = new Div();
    private final Tab writeTab = new Tab("Write");
    private final Tab previewTab = new Tab("Preview");
    private final Tabs tabs = new Tabs(writeTab, previewTab);
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public MarkdownField(String label) {
        this();
        setLabel(label);
    }

    public MarkdownField() {
        input.setWidth("100%");
        writeView.setVisible(false);
        previewView.setVisible(true);
        tabs.addThemeVariants(TabsVariant.LUMO_SMALL);
        tabs.setSelectedTab(previewTab);
        add(tabs, writeView, previewView);
        tabs.addSelectedChangeListener(event -> {
            if (tabs.getSelectedTab().getLabel().equals("Preview")) {
                activatePreview();
            } else {
                writeView.setVisible(true);
                previewView.setVisible(false);
            }
        });
    }

    @Override
    protected String generateModelValue() {
        return input.getValue();
    }

    @Override
    protected void setPresentationValue(String text) {
        VaadinUtils.setValue(input, text);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        writeTab.setEnabled(!readOnly);
        if (readOnly) {
            activatePreview();
        }
    }

    @Override
    public void clear() {
        super.clear();
        input.clear();
    }

    @Override
    public void setValue(String value) {
        VaadinUtils.setValue(input, value);
        addMarkdown(value);
    }

    @Override
    public String getValue() {
        return input.getValue();
    }

    private void addMarkdown(String value) {
        Html item = new Html(String.format("<div>%s</div>", parseMarkdown(StringUtil.nullSafeString(value))));
        previewView.removeAll();
        previewView.add(item);
    }

    private String parseMarkdown(String value) {
        return renderer.render(parser.parse(value));
    }

    private void activatePreview() {
        writeView.setVisible(false);
        previewView.setVisible(true);
        addMarkdown(getValue().isEmpty() ? "*Nothing to preview*" : getValue());
    }
}
