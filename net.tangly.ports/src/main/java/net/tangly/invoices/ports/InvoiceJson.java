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

package net.tangly.invoices.ports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.BankConnection;
import net.tangly.bus.invoices.Article;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceItem;
import net.tangly.bus.invoices.InvoiceLegalEntity;
import net.tangly.bus.invoices.InvoiceLine;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.bus.invoices.Subtotal;
import net.tangly.commons.logger.EventData;
import net.tangly.commons.utilities.JsonUtilities;
import net.tangly.gleam.model.JsonArray;
import net.tangly.gleam.model.JsonEntity;
import net.tangly.gleam.model.JsonField;
import net.tangly.gleam.model.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceJson implements InvoiceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceJson.class);
    private static final String COMPONENT = "net.tangly.ports";
    private final RealmInvoices realm;

    public InvoiceJson(@NotNull RealmInvoices realm) {
        this.realm = realm;
    }

    @Override
    public void exports(@NotNull Invoice invoice, @NotNull Path path, @NotNull Map<String, Object> properties) {
        JsonEntity<Invoice> entity = createJsonInvoice();
        JSONObject invoiceJson = entity.exports(invoice);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(invoiceJson.toString(4));
            EventData.log(EventData.EXPORT, COMPONENT, EventData.Status.SUCCESS, "Invoice exported to JSON file", Map.of("filename", path, "entity", invoice));
        } catch (IOException e) {
            EventData.log(EventData.EXPORT, COMPONENT, EventData.Status.FAILURE, "Invoice exported to JSON file", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    public Invoice imports(@NotNull Path path, @NotNull Map<String, Object> properties) {
        JsonEntity<Invoice> entity = createJsonInvoice();
        Invoice invoice = null;
        if (JsonUtilities.isValid(path, "invoice-schema.json")) {
            try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
                JSONObject jsonInvoice = new JSONObject(new JSONTokener(in));
                invoice = entity.imports(jsonInvoice);
                if (!invoice.isValid()) {
                    logger.atWarn().log("Invoice {} is invalid", invoice.name());
                }
                EventData.log(EventData.IMPORT, COMPONENT, EventData.Status.SUCCESS, "Invoice imported", Map.of("filename", path, "entity", invoice));
            } catch (IOException e) {
                EventData.log(EventData.IMPORT, COMPONENT, EventData.Status.FAILURE, "Error during import of JSON file", Map.of("filename", path), e);
                throw new UncheckedIOException(e);
            }
        } else {
            EventData.log(EventData.IMPORT, COMPONENT, EventData.Status.FAILURE, "Invalid JSON schema of JSON file", Map.of("filename", path));
        }
        return invoice;
    }

    public JsonEntity<Invoice> createJsonInvoice() {
        JsonEntity<InvoiceLegalEntity> jsonLegalEntity = createJsonLegalEntity();
        JsonEntity<Address> jsonAddress = createJsonAddress();
        JsonEntity<BankConnection> jsonBankConnection = createJsonBankConnection();
        List<JsonField<Invoice, ?>> fields =
                List.of(JsonProperty.ofString("id", Invoice::id, Invoice::id), JsonProperty.ofString("name", Invoice::name, Invoice::name),
                        JsonProperty.ofString("text", Invoice::text, Invoice::text),
                        JsonProperty.ofString("contractId", Invoice::contractId, Invoice::contractId),
                        JsonProperty.ofType("invoicingEntity", Invoice::invoicingEntity, Invoice::invoicingEntity, jsonLegalEntity),
                        JsonProperty.ofType("invoicingAddress", Invoice::invoicingAddress, Invoice::invoicingAddress, jsonAddress),
                        JsonProperty.ofType("invoicingConnection", Invoice::invoicingConnection, Invoice::invoicingConnection, jsonBankConnection),
                        JsonProperty.ofType("invoicedEntity", Invoice::invoicedEntity, Invoice::invoicedEntity, jsonLegalEntity),
                        JsonProperty.ofType("invoicedAddress", Invoice::invoicedAddress, Invoice::invoicedAddress, jsonAddress),
                        JsonProperty.ofLocalDate("deliveryDate", Invoice::deliveryDate, Invoice::deliveryDate),
                        JsonProperty.ofLocalDate("invoiceDate", Invoice::invoicedDate, Invoice::invoicedDate),
                        JsonProperty.ofLocalDate("dueDate", Invoice::dueDate, Invoice::dueDate),
                        JsonProperty.ofCurrency("currency", Invoice::currency, Invoice::currency),
                        JsonProperty.ofLocale("locale", Invoice::locale, Invoice::locale),
                        JsonProperty.ofString("paymentConditions", Invoice::paymentConditions, Invoice::paymentConditions), createPositions());
        return JsonEntity.of(fields, Invoice::new);
    }

    public static JsonEntity<BankConnection> createJsonBankConnection() {
        Function<JSONObject, BankConnection> imports = object -> {
            BankConnection connection = new BankConnection(JsonField.get("iban", object), JsonField.get("bic", object), JsonField.get("institute", object));
            return (connection.isValid()) ? connection : null;
        };
        List<JsonField<BankConnection, ?>> fields =
                List.of(JsonProperty.ofString("iban", BankConnection::iban, null), JsonProperty.ofString("bic", BankConnection::bic, null),
                        JsonProperty.ofString("institute", BankConnection::institute, null));
        return JsonEntity.of(fields, imports);
    }

    public static JsonEntity<Address> createJsonAddress() {
        Function<JSONObject, Address> imports =
                object -> new Address(JsonField.get("street", object), JsonField.get("extended", object), JsonField.get("poBox", object),
                        JsonField.get("postCode", object), JsonField.get("locality", object), JsonField.get("region", object),
                        JsonField.get("country", object));
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
        return JsonEntity.of(fields, this::importProduct);
    }

    public JsonEntity<InvoiceLegalEntity> createJsonLegalEntity() {
        Function<JSONObject, InvoiceLegalEntity> imports =
                object -> new InvoiceLegalEntity(JsonField.get("id", object), JsonField.get("name", object), JsonField.get("vatNr", object));
        List<JsonField<InvoiceLegalEntity, ?>> fields =
                List.of(JsonProperty.ofString("id", InvoiceLegalEntity::id, null), JsonProperty.ofString("name", InvoiceLegalEntity::name, null),
                        JsonProperty.ofString("vatNr", InvoiceLegalEntity::vatNr, null));
        return JsonEntity.of(fields, imports);
    }

    public JsonEntity<InvoiceItem> createJsonInvoiceItem() {
        Function<JSONObject, InvoiceItem> imports =
                object -> new InvoiceItem(object.getInt("position"), importProduct((JSONObject) object.get("product")), object.getString("text"),
                        object.getBigDecimal("quantity"));

        List<JsonField<InvoiceItem, ?>> fields = List.of(JsonProperty.ofInt("position", InvoiceItem::position, null),
                JsonProperty.ofType("product", InvoiceItem::article, null, createJsonArticle()), JsonProperty.ofString("text", InvoiceItem::text, null),
                JsonProperty.ofBigDecimal("quantity", InvoiceItem::quantity, null));
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

    public Article importProduct(JSONObject object) {
        String id = JsonField.get("id", object);
        return realm.articles().items().stream().filter(o -> o.id().equals(id)).findAny().orElse(null);
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
