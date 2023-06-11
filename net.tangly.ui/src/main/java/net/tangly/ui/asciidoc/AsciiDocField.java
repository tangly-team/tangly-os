/*
 * Copyright 2021-2023 Marcel Baumann
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

package net.tangly.ui.asciidoc;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextArea;
import net.tangly.core.Strings;
import net.tangly.ui.components.VaadinUtils;
import org.asciidoctor.Asciidoctor;

import java.util.HashMap;
import java.util.Objects;

/**
 * Defines a display and edit field for asciidoc text. The preview field shows rendered asciidoc text. The edition field is a simple text area edit field.
 */
@Tag("asciidoc-area")
public class AsciiDocField extends CustomField<String> {
    private static final String EDIT_TAB = "Edit";
    private static final String PREVIEW_TAB = "Preview";
    private final TextArea editField;
    private final Div previewField;
    private final TabSheet tabSheet;
    private final Tab editTab;
    private final Tab previewTab;
    private static final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    public AsciiDocField(String label) {
        if (Objects.nonNull(label)) {
            setLabel(label);
        }
        previewField = new Div();
        editField = new TextArea();
        editField.setHeight("3em");
        editField.setWidth("100%");
        editField.addValueChangeListener(e -> updatePreview(Strings.emptyToNull(e.getValue())));

        tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);
        previewTab = tabSheet.add(PREVIEW_TAB, previewField);
        editTab = tabSheet.add(EDIT_TAB, editField);
        tabSheet.setSelectedTab(previewTab);
        add(tabSheet);
    }

    @Override
    protected String generateModelValue() {
        return editField.getValue();
    }

    @Override
    protected void setPresentationValue(String text) {
        VaadinUtils.setValue(editField, text);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        editTab.setEnabled(!readOnly);
        if (readOnly) {
            tabSheet.setSelectedTab(previewTab);
        }
    }

    @Override
    public void clear() {
        super.clear();
        editField.clear();
    }

    @Override
    public void setValue(String value) {
        VaadinUtils.setValue(editField, value);
        updatePreview(Strings.emptyToNull(value));
    }

    @Override
    public String getValue() {
        return editField.getValue();
    }

    private void updatePreview(String value) {
        Html item = new Html(String.format("<div>%s</div>", asciidoctor.convert(Strings.nullToEmpty(value), new HashMap<String, Object>())));
        previewField.removeAll();
        previewField.add(item);
    }

}
