/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.invoices.artifacts;

import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.JsonUtilities;
import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import net.tangly.core.EmailAddress;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceItem;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import net.tangly.erp.invoices.domain.InvoiceLine;
import net.tangly.erp.invoices.domain.Subtotal;
import net.tangly.erp.invoices.services.InvoicesRealm;
import net.tangly.gleam.model.JsonArray;
import net.tangly.gleam.model.JsonEntity;
import net.tangly.gleam.model.JsonField;
import net.tangly.gleam.model.JsonProperty;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InvoiceJson implements InvoiceGenerator {
    private static final Logger logger = LogManager.getLogger();
    private static final String COMPONENT = "net.tangly.ports";
    private final InvoicesRealm realm;

    public InvoiceJson(@NotNull InvoicesRealm realm) {
        this.realm = realm;
    }

    @Override
    public void exports(@NotNull Invoice invoice, @NotNull Path path, @NotNull Map<String, Object> properties) {
        JsonEntity<Invoice> entity = createJsonInvoice();
        var invoiceJson = entity.exports(invoice);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(invoiceJson.toString(4));
            EventData.log(EventData.EXPORT, COMPONENT, EventData.Status.SUCCESS, "Invoice exported to JSON", Map.ofEntries(Map.entry("filename", path), Map.entry("entity", invoice)));
        } catch (IOException e) {
            EventData.log(EventData.EXPORT, COMPONENT, EventData.Status.FAILURE, "Invoice exported to JSON", Map.ofEntries(Map.entry("filename", path)), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Import a JSON invoice into the domain.
     *
     * @param reader reader for the character stream of the JSON file. JSON files are always character based. Reader is closed upon use.
     * @param source name of the JSON source
     * @return the newly created domain invoice object
     */
    public Invoice imports(@NotNull Reader reader, @NotNull String source) {
        JsonEntity<Invoice> entity = createJsonInvoice();
        Invoice invoice = null;
        try {
            String jsonString = IOUtils.toString(reader);
            reader.close();
            if (JsonUtilities.isValid(new StringReader(jsonString), "invoice-schema.json")) {
                var jsonInvoice = new JSONObject(new JSONTokener(new StringReader(jsonString)));
                invoice = entity.imports(jsonInvoice);
                if (!invoice.check()) {
                    logger.atInfo().log("Invoice {} is invalid", invoice.name());
                }
                EventData.log(EventData.IMPORT, COMPONENT, EventData.Status.SUCCESS, "Invoice imported", Map.ofEntries(Map.entry("filename", source), Map.entry("entity", invoice)));
            } else {
                EventData.log(EventData.IMPORT, COMPONENT, EventData.Status.FAILURE, "Invalid JSON schema file", Map.ofEntries(Map.entry("filename", source)));
            }
        } catch (Exception e) {
            EventData.log(EventData.IMPORT, COMPONENT, EventData.Status.FAILURE, "Error during import of JSON", Map.ofEntries(Map.entry("filename", source)), e);
        }

        return invoice;
    }

    public JsonEntity<Invoice> createJsonInvoice() {
        JsonEntity<InvoiceLegalEntity> jsonLegalEntity = createJsonLegalEntity();
        JsonEntity<BankConnection> jsonBankConnection = createJsonBankConnection();
        List<JsonField<Invoice, ?>> fields =
            List.of(JsonProperty.ofString("id", Invoice::id, Invoice::id), JsonProperty.ofString("name", Invoice::name, Invoice::name),
                JsonProperty.ofString("text", Invoice::text, Invoice::text), JsonProperty.ofString("contractId", Invoice::contractId, Invoice::contractId),
                JsonProperty.ofType("invoicingEntity", Invoice::invoicingEntity, Invoice::invoicingEntity, jsonLegalEntity),
                JsonProperty.ofType("invoicingConnection", Invoice::invoicingConnection, Invoice::invoicingConnection, jsonBankConnection),
                JsonProperty.ofType("invoicedEntity", Invoice::invoicedEntity, Invoice::invoicedEntity, jsonLegalEntity),
                JsonProperty.ofLocalDate("deliveryDate", Invoice::deliveryDate, Invoice::deliveryDate),
                JsonProperty.ofLocalDate("invoiceDate", Invoice::date, Invoice::date), JsonProperty.ofLocalDate("dueDate", Invoice::dueDate, Invoice::dueDate),
                JsonProperty.ofLocalDate("paidDate", Invoice::paidDate, Invoice::paidDate),
                JsonProperty.ofCurrency("currency", Invoice::currency, Invoice::currency), JsonProperty.ofLocale("locale", Invoice::locale, Invoice::locale),
                JsonProperty.ofString("paymentConditions", Invoice::paymentConditions, Invoice::paymentConditions), createPositions());
        return JsonEntity.of(fields, Invoice::new);
    }

    public JsonEntity<InvoiceLegalEntity> createJsonLegalEntity() {
        JsonEntity<Address> jsonAddress = createJsonAddress();
        JsonProperty<InvoiceLegalEntity, EmailAddress> jsonEmail = ofEmailAddress("email", InvoiceLegalEntity::email, null);
        Function<JSONObject, InvoiceLegalEntity> imports =
            o -> new InvoiceLegalEntity(JsonField.string(o, "id"), JsonField.string(o, "name"), JsonField.string(o, "vatNr"),
                jsonAddress.imports(o.getJSONObject("address")), jsonEmail.convertFromJson(o));
        List<JsonField<InvoiceLegalEntity, ?>> fields =
            List.of(JsonProperty.ofString("id", InvoiceLegalEntity::id, null), JsonProperty.ofString("name", InvoiceLegalEntity::name, null),
                JsonProperty.ofString("vatNr", InvoiceLegalEntity::vatNr, null), JsonProperty.ofType("address", InvoiceLegalEntity::address, null, jsonAddress),
                jsonEmail);
        return JsonEntity.of(fields, imports);
    }

    public static <T> JsonProperty<T, EmailAddress> ofEmailAddress(String property, Function<T, EmailAddress> getter, BiConsumer<T, EmailAddress> setter) {
        return JsonProperty.of(property, getter, setter, o -> o.has(property) ? EmailAddress.of(o.getString(property)) : null,
            (u, o) -> o.put(property, u.text()));
    }

    public static JsonEntity<BankConnection> createJsonBankConnection() {
        Function<JSONObject, BankConnection> imports =
            o -> BankConnection.of(JsonField.string(o, "iban"), JsonField.string(o, "bic"), JsonField.string(o, "institute"));
        List<JsonField<BankConnection, ?>> fields =
            List.of(JsonProperty.ofString("iban", BankConnection::iban, null), JsonProperty.ofString("bic", BankConnection::bic, null),
                JsonProperty.ofString("institute", BankConnection::institute, null));
        return JsonEntity.of(fields, imports);
    }

    public static JsonEntity<Address> createJsonAddress() {
        Function<JSONObject, Address> imports =
            o -> new Address(JsonField.string(o, "street"), JsonField.string(o, "extended"), JsonField.string(o, "poBox"), JsonField.string(o, "postCode"),
                JsonField.string(o, "locality"), JsonField.string(o, "region"), JsonField.string(o, "country"));
        List<JsonField<Address, ?>> fields =
            List.of(JsonProperty.ofString("street", Address::street, null), JsonProperty.ofString("extended", Address::extended, null),
                JsonProperty.ofString("poBox", Address::poBox, null), JsonProperty.ofString("postCode", Address::postcode, null),
                JsonProperty.ofString("locality", Address::locality, null), JsonProperty.ofString("region", Address::region, null),
                JsonProperty.ofString("country", Address::country, null));
        return JsonEntity.of(fields, imports);
    }

    public JsonEntity<Article> createJsonArticle() {
        List<JsonField<Article, ?>> fields = List.of(JsonProperty.ofString("id", Article::id, null), JsonProperty.ofString("description", Article::text, null),
            JsonProperty.ofBigDecimal("unitPrice", Article::unitPrice, null), JsonProperty.ofString("unit", Article::unit, null),
            JsonProperty.ofBigDecimal("vatRate", Article::vatRate, null));
        return JsonEntity.of(fields, this::importArticle);
    }

    public JsonEntity<InvoiceItem> createJsonInvoiceItem() {
        final String ARTICLE = "article";
        Function<JSONObject, InvoiceItem> imports =
            o -> new InvoiceItem(o.getInt("position"), importArticle((JSONObject) o.get(ARTICLE)), o.getString("text"), o.getBigDecimal("quantity"));

        List<JsonField<InvoiceItem, ?>> fields =
            List.of(JsonProperty.ofInt("position", InvoiceItem::position, null), JsonProperty.ofType(ARTICLE, InvoiceItem::article, null, createJsonArticle()),
                JsonProperty.ofString("text", InvoiceItem::text, null), JsonProperty.ofBigDecimal("quantity", InvoiceItem::quantity, null));
        return JsonEntity.of(fields, imports);
    }

    public JsonEntity<Subtotal> createJsonSubtotal() {
        BiFunction<JSONObject, Object, Subtotal> imports = (object, entity) -> {
            Invoice invoice = (Invoice) entity;
            List<InvoiceLine> lines = new ArrayList<>();
            JSONArray itemsPosition = object.getJSONArray("items");
            for (Object value : itemsPosition) {
                int position = (Integer) value;
                lines.add(invoice.lines().stream().filter(o -> position == o.position()).findAny().orElseThrow());
            }
            return new Subtotal(object.getInt("position"), object.getString("text"), lines);
        };
        BiFunction<Subtotal, Object, JSONObject> exports = (subtotal, entity) -> {
            JSONObject subtotalJson = new JSONObject();
            subtotalJson.put("position", (subtotal != null) ? subtotal.position() : null);
            subtotalJson.put("text", (subtotal != null) ? subtotal.text() : null);
            JSONArray itemsPosition = new JSONArray();
            if (subtotal != null) {
                subtotal.items().forEach(o -> itemsPosition.put(o.position()));
            }
            subtotalJson.put("items", itemsPosition);
            return subtotalJson;
        };
        return JsonEntity.of(imports, exports);
    }

    public Article importArticle(@NotNull JSONObject object) {
        return realm.articles().items().stream().filter(o -> o.id().equals(JsonField.string(object, "id"))).findAny().orElse(null);
    }

    public JsonArray<Invoice, InvoiceLine> createPositions() {
        JsonEntity<Subtotal> jsonSubtotal = createJsonSubtotal();
        JsonEntity<InvoiceItem> jsonInvoiceItem = createJsonInvoiceItem();
        Function<JSONObject, JsonEntity<?>> importSelector = o -> {
            if (o.has("items")) {
                return jsonSubtotal;
            } else {
                return jsonInvoiceItem;
            }
        };
        Function<Object, JsonEntity<?>> exportSelector = o -> {
            if (o instanceof InvoiceItem) {
                return jsonInvoiceItem;
            } else if (o instanceof Subtotal) {
                return jsonSubtotal;
            } else {
                return null;
            }
        };
        return new JsonArray<>("items", Invoice::lines, Invoice::add, importSelector, exportSelector);
    }
}
