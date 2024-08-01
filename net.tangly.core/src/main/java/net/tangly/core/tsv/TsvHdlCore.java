/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.core.tsv;

import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import net.tangly.core.DateRange;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class TsvHdlCore {
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String TEXT = "text";
    public static final String LOCALE = "locale";
    private static final String STREET = "street";
    private static final String EXTENDED = "extended";
    private static final String POSTCODE = "postcode";
    private static final String LOCALITY = "locality";
    private static final String POBOX = "pobox";
    private static final String REGION = "region";
    private static final String COUNTRY = "country";
    private static final String IBAN = "iban";
    private static final String BIC = "bic";
    private static final String INSTITUTE = "institute";

    private TsvHdlCore() {
    }

    public static TsvEntity<BankConnection> createTsvBankConnection() {
        Function<CSVRecord, BankConnection> imports = (CSVRecord csv) -> BankConnection.of(TsvEntity.get(csv, IBAN),
            TsvEntity.get(csv, BIC), TsvEntity.get(csv, INSTITUTE));
        List<TsvProperty<BankConnection, ?>> fields = List.of(TsvProperty.ofString("iban", BankConnection::iban),
            TsvProperty.ofString("bic", BankConnection::bic),
            TsvProperty.ofString(INSTITUTE, BankConnection::institute));
        return TsvEntity.of(BankConnection.class, fields, imports);
    }

    public static TsvEntity<Address> createTsvAddress() {
        Function<CSVRecord, Address> imports = TsvHdlCore::ofAddress;
        List<TsvProperty<Address, ?>> fields = List.of(TsvProperty.ofString(STREET, Address::street), TsvProperty.ofString(EXTENDED, Address::extended),
            TsvProperty.ofString(POBOX, Address::poBox), TsvProperty.ofString(POSTCODE, Address::postcode), TsvProperty.ofString(LOCALITY, Address::locality),
            TsvProperty.ofString(REGION, Address::region), TsvProperty.ofString(COUNTRY, Address::country));
        return TsvEntity.of(Address.class, fields, imports);
    }

    public static TsvEntity<DateRange> createTsvDateRange() {
        Function<CSVRecord, DateRange> imports = (CSVRecord csv) -> DateRange.of(ofDate(csv, FROM_DATE), ofDate(csv, TO_DATE));
        List<TsvProperty<DateRange, ?>> fields = List.of(TsvProperty.ofDate(FROM_DATE, DateRange::from, null), TsvProperty.ofDate(TO_DATE, DateRange::to, null));
        return TsvEntity.of(DateRange.class, fields, imports);
    }

    public static Address ofAddress(CSVRecord csv) {
        return (Objects.isNull(TsvEntity.get(csv, LOCALITY)) || Objects.isNull(TsvEntity.get(csv, COUNTRY))) ? null :
            new Address(TsvEntity.get(csv, STREET), TsvEntity.get(csv, EXTENDED), TsvEntity.get(csv, POBOX), TsvEntity.get(csv, POSTCODE), TsvEntity.get(csv, LOCALITY),
                TsvEntity.get(csv, REGION), TsvEntity.get(csv, COUNTRY));
    }

    public static LocalDate ofDate(CSVRecord csv, String field) {
        String value = TsvEntity.get(csv, field);
        return (value == null) ? null : LocalDate.parse(value);
    }
}
