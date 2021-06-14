/*
 * Copyright 2021 Marcel Baumann
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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

@Tag("markdown-area")
public class MarkdownArea extends Composite<Div> {

    private TextArea input = new TextArea();
    private Link link = new Link("Markdown", "https://vaadin.com/markdown-guide");
    private Label label = new Label(" supported");
    private Div writeView = new Div(input, link, label);
    private Div previewView = new Div();
    private Tab writeTab = new Tab("Write");
    private Tab previewTab = new Tab("Preview");
    private Tabs tabs = new Tabs(writeTab, previewTab);
    Parser parser = Parser.builder().build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();

    public MarkdownArea() {
        init();
    }

    public MarkdownArea(String text) {
        if (!text.isBlank()) {
            setValue(text);
        }
        init();
    }

    private void init() {
        input.setWidth("100%");
        link.setTarget("_blank");
        previewView.setVisible(false);
        getContent().add(tabs, writeView, previewView);
        tabs.addSelectedChangeListener(event -> {
            if (tabs.getSelectedTab().getLabel().equals("Preview")) {
                writeView.setVisible(false);
                previewView.setVisible(true);
                String text = getValue().isEmpty() ? "*Nothing to preview*" : getValue();
                addMarkdown(text);
            } else {
                writeView.setVisible(true);
                previewView.setVisible(false);
            }
        });
    }

    private void addMarkdown(String value) {
        String html = String.format("<div>%s</div>", parseMarkdown(StringUtil.nullSafeString(value)));
        Html item = new Html(html);
        previewView.removeAll();
        previewView.add(item);
    }

    private String parseMarkdown(String value) {
        Node text = parser.parse(value);
        return renderer.render(text);
    }

    public void setValue(String value) {
        input.setValue(value);
    }

    public String getValue() {
        return input.getValue();
    }

    public TextArea getInput() {
        return input;
    }

    public void setMarkdownLink(String markdownLink) {
        link.setHref(markdownLink);
    }

}
