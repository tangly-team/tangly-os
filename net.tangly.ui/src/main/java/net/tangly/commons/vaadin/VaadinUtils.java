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

package net.tangly.commons.vaadin;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.Tag;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.LegalEntity;
import org.jetbrains.annotations.NotNull;

public final class VaadinUtils {
    public static final DecimalFormat FORMAT = new DecimalFormat("###,##0.00", new DecimalFormatSymbols(Locale.US));

    private VaadinUtils() {
    }

    public static TextField createTextField(String label, String placeholder) {
        return createTextField(label, placeholder, false, true);
    }

    public static TextField createTextField(String label, String placeholder, boolean readonly) {
        return createTextField(label, placeholder, readonly, true);
    }

    public static TextField createTextField(String label, String placeholder, boolean readonly, boolean enabled) {
        TextField field = new TextField(label, placeholder);
        field.setReadOnly(readonly);
        field.setEnabled(enabled);
        return field;
    }

    public static DatePicker createDatePicker(String label) {
        return createDatePicker(label, false, true);
    }

    public static DatePicker createDatePicker(String label, boolean readonly, boolean enabled) {
        DatePicker field = new DatePicker(label);
        field.setLocale(Locale.forLanguageTag("sv-SE"));
        field.setReadOnly(readonly);
        field.setEnabled(enabled);
        return field;
    }

    public static <C extends HasValue<?, T>, T> void setValue(@NotNull C component, T value) {
        if ((value != null)) {
            component.setValue(value);
        } else {
            component.clear();
        }
    }

    public static void setResponsiveSteps(@NotNull FormLayout layout) {
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("25em", 1), new FormLayout.ResponsiveStep("32em", 2), new FormLayout.ResponsiveStep("40em", 3));
    }

    public static <E> void initialize(@NotNull Grid<E> grid) {
        grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        grid.setVerticalScrollingEnabled(true);
        grid.setWidthFull();
    }

    public static void setAttribute(Component component, String attribute, Object value) {
        component.getUI().ifPresent(o -> o.getSession().setAttribute(attribute, value));
    }

    public static Object getAttribute(Component component, String attribute) {
        return component.getUI().isPresent() ? component.getUI().get().getSession().getAttribute(attribute) : null;
    }

    public static BigDecimal toBigDecimal(String value) {
        return (value == null) ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public static String format(BigDecimal value) {
        return FORMAT.format(value);
    }

    public static <T> ComponentRenderer<Span, T> coloredRender(Function<T, BigDecimal> getter, NumberFormat numberFormat) {
        return new ComponentRenderer<>((T e) -> {
            BigDecimal v = getter.apply(e);
            return switch (BigDecimal.ZERO.compareTo(v)) {
                case -1 -> new Span(numberFormat.format(v));
                case 0 -> new Span();
                case 1 -> {
                    Span s = new Span(numberFormat.format(v));
                    s.getElement().getStyle().set("color", "red");
                    yield s;
                }
                default -> new Span("");
            };
        });
    }

    @SafeVarargs
    public static <T extends HasValue> void readOnly(CrudForm.Operation operation, T... components) {
        readOnly(Crud.Mode.readOnly(Crud.of(operation)), components);
    }

    @SafeVarargs
    public static <T extends HasValue> void readOnly(boolean readOnly, T... components) {
        Arrays.stream(components).forEach(o -> o.setReadOnly(readOnly));
    }

    public static <T extends HasEnabled & HasValue> void configureId(CrudForm.Operation operation, T component) {
        switch (operation) {
            case VIEW, UPDATE, DELETE -> {
                component.setReadOnly(true);
                component.setEnabled(false);
            }
            case CREATE -> {
                component.setReadOnly(false);
                component.setEnabled(true);
            }
        }

    }

    public static <T extends HasEnabled & HasValue> void configureOid(CrudForm.Operation operation, T component) {
        component.setReadOnly(false);
        component.setEnabled(false);
    }

    public static <T extends HasTags> ComponentRenderer<Anchor, T> linkedInComponentRenderer(Function<HasTags, String> linkedInUrl) {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            Tag tag = item.findBy(CrmTags.CRM_IM_LINKEDIN).orElse(null);
            String linkedInRef = (tag != null) ? tag.value() : null;
            anchor.setText(linkedInRef);
            anchor.setHref((linkedInRef != null) ? linkedInUrl.apply(item) : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static <T extends HasTags> ComponentRenderer<Anchor, T> urlComponentRenderer(String tagName) {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            Tag tag = item.findBy(tagName).orElse(null);
            String url = (tag != null) ? tag.value() : null;
            anchor.setText(url);
            anchor.setHref((url != null) ? "https://" + url : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static <T extends LegalEntity> ComponentRenderer<Anchor, T> zefixComponentRenderer() {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            anchor.setText(item.id());
            if ((item.id() != null) && item.id().startsWith("CHE-")) {
                anchor.setHref(CrmTags.organizationZefixUrl(item));
                anchor.setTarget("_blank");
            }
            return anchor;
        });
    }

    public static List<LocalDate> quarterLegends(LocalDate from, LocalDate to) {
        return List.of(LocalDate.parse("2015-12-31"), LocalDate.parse("2016-03-31"), LocalDate.parse("2016-06-30"), LocalDate.parse("2016-09-30"),
            LocalDate.parse("2016-12-31"), LocalDate.parse("2017-03-31"), LocalDate.parse("2017-06-30"), LocalDate.parse("2017-09-30"),
            LocalDate.parse("2017-12-31"), LocalDate.parse("2018-03-31"), LocalDate.parse("2018-06-30"), LocalDate.parse("2018-09-30"),
            LocalDate.parse("2018-12-31"), LocalDate.parse("2019-03-31"), LocalDate.parse("2019-06-30"), LocalDate.parse("2019-09-30"),
            LocalDate.parse("2019-12-31"), LocalDate.parse("2020-03-31"), LocalDate.parse("2020-06-30"), LocalDate.parse("2020-09-30"),
            LocalDate.parse("2020-12-31"));
    }

}
