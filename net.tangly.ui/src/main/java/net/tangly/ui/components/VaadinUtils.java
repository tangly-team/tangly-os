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

package net.tangly.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;
import net.tangly.core.EmailAddress;
import net.tangly.core.HasMutableTags;
import net.tangly.core.Strings;
import net.tangly.core.Tag;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

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
        var field = new TextField(label, placeholder);
        field.setReadOnly(readonly);
        field.setEnabled(enabled);
        return field;
    }

    public static TextArea createTextArea(String label, String placeholder, boolean readonly, boolean enabled) {
        var field = new TextArea(label, placeholder);
        field.setReadOnly(readonly);
        field.setEnabled(enabled);
        return field;
    }

    public static <T extends Enum<T>> Select<T> createSelectFor(Class<T> clazz, String label) {
        Select<T> component = new Select<>();
        component.setLabel(label);
        component.setItemLabelGenerator(T::name);
        component.setItems(clazz.getEnumConstants());
        return component;
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

    public static void set3ResponsiveSteps(@NotNull FormLayout layout) {
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("25em", 1), new FormLayout.ResponsiveStep("32em", 2), new FormLayout.ResponsiveStep("40em", 3));
    }

    public static void set1ResponsiveSteps(@NotNull FormLayout layout) {
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("25em", 1));
    }

    public static <E> void initialize(@NotNull Grid<E> grid) {
        grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        grid.setMinHeight("5em");
        grid.setHeightFull();
        grid.setWidthFull();
    }

    public static <E> Grid.Column<E> addColumn(@NotNull Grid<E> grid, @NotNull ValueProvider<E, ?> getter, @NotNull String key, @NotNull String header) {
        return grid.addColumn(getter).setKey(key).setHeader(header).setAutoWidth(true).setResizable(true).setSortable(true);
    }

    public static <E> Grid.Column<E> addColumn(@NotNull Grid<E> grid, @NotNull Renderer<E> renderer, @NotNull String key, @NotNull String header) {
        return grid.addColumn(renderer).setKey(key).setHeader(header).setAutoWidth(true).setResizable(true).setSortable(true);
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

    public static <T> ComponentRenderer<Anchor, T> linkedInComponentRenderer(Function<T, String> property, boolean isOrganization) {
        final String ORGANIZATION_LINKEDIN = "https://www.linkedin.com/company/";
        final String PERSON_LINKEDIN = "https://www.linkedin.com/in/";
        return new ComponentRenderer<>(e -> {
            Anchor anchor = new Anchor();
            String linkedInUrl = property.apply(e);
            String linkedInRef = Strings.isNullOrBlank(linkedInUrl) ? null : (isOrganization ? ORGANIZATION_LINKEDIN : PERSON_LINKEDIN) + linkedInUrl;
            anchor.setText(linkedInUrl);
            anchor.setHref(Objects.nonNull(linkedInRef) ? linkedInRef : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static <T> ComponentRenderer<Anchor, T> emailAddressComponentRenderer(Function<T, EmailAddress> property) {
        return new ComponentRenderer<>(e -> {
            Anchor anchor = new Anchor();
            EmailAddress address = property.apply(e);
            String email = Objects.nonNull(address) ? address.text() : "";
            anchor.setText(email);
            anchor.setHref(Objects.nonNull(email) ? STR."mailto:\{email}" : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static <T> ComponentRenderer<Anchor, T> urlComponentRenderer(Function<T, String> getter) {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            String url = getter.apply(item);
            anchor.setText(url);
            anchor.setHref(Objects.nonNull(url) ? STR."https://\{url}" : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static <T extends HasMutableTags> ComponentRenderer<Anchor, T> urlComponentRenderer(String tagName) {
        return new ComponentRenderer<>(item -> {
            Anchor anchor = new Anchor();
            Tag tag = item.findBy(tagName).orElse(null);
            String url = (tag != null) ? tag.value() : null;
            anchor.setText(url);
            anchor.setHref((url != null) ? STR."https://\{url}" : "");
            anchor.setTarget("_blank");
            return anchor;
        });
    }

    public static Dialog createDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(false);
        dialog.setResizable(true);
        return dialog;
    }

    public static List<LocalDate> quarterLegends(LocalDate from, LocalDate to) {
        return List.of(LocalDate.parse("2015-12-31"), LocalDate.parse("2016-03-31"), LocalDate.parse("2016-06-30"), LocalDate.parse("2016-09-30"), LocalDate.parse("2016-12-31"),
            LocalDate.parse("2017-03-31"), LocalDate.parse("2017-06-30"), LocalDate.parse("2017-09-30"), LocalDate.parse("2017-12-31"), LocalDate.parse("2018-03-31"),
            LocalDate.parse("2018-06-30"), LocalDate.parse("2018-09-30"), LocalDate.parse("2018-12-31"), LocalDate.parse("2019-03-31"), LocalDate.parse("2019-06-30"),
            LocalDate.parse("2019-09-30"), LocalDate.parse("2019-12-31"), LocalDate.parse("2020-03-31"), LocalDate.parse("2020-06-30"), LocalDate.parse("2020-09-30"),
            LocalDate.parse("2020-12-31"));
    }

}
