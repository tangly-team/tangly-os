/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.tsv;

import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import net.tangly.core.HasId;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class TsvHdlCore {
    public static final String ID = HasId.ID;
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String FROM_DATE = "fromDate";
    public static final String TO_DATE = "toDate";
    public static final String TEXT = "text";
    private static final String STREET = "street";
    private static final String POSTCODE = "postcode";
    private static final String LOCALITY = "locality";
    private static final String REGION = "region";
    private static final String COUNTRY = "country";
    private static final String IBAN = "iban";
    private static final String BIC = "bic";
    private static final String INSTITUTE = "institute";

    public static TsvEntity<BankConnection> createTsvBankConnection() {
        Function<CSVRecord, BankConnection> imports = (CSVRecord csv) -> BankConnection.of(TsvEntity.get(csv, IBAN), TsvEntity.get(csv, BIC), TsvEntity.get(csv, INSTITUTE));
        List<TsvProperty<BankConnection, ?>> fields =
            List.of(TsvProperty.ofString("iban", BankConnection::iban, null), TsvProperty.ofString("bic", BankConnection::bic, null),
                TsvProperty.ofString(INSTITUTE, BankConnection::institute, null));
        return TsvEntity.of(BankConnection.class, fields, imports);
    }

    public static TsvEntity<Address> createTsvAddress() {
        Function<CSVRecord, Address> imports = (CSVRecord csv) -> (Objects.isNull(TsvEntity.get(csv, LOCALITY)) || Objects.isNull(TsvEntity.get(csv, COUNTRY))) ? null :
            Address.builder().street(TsvEntity.get(csv, STREET)).postcode(TsvEntity.get(csv, POSTCODE)).locality(TsvEntity.get(csv, LOCALITY)).region(TsvEntity.get(csv, REGION))
                   .country(TsvEntity.get(csv, COUNTRY)).build();
        List<TsvProperty<Address, ?>> fields =
            List.of(TsvProperty.ofString(STREET, Address::street, null), TsvProperty.ofString("extended", Address::extended, null),
                TsvProperty.ofString(POSTCODE, Address::postcode, null), TsvProperty.ofString(LOCALITY, Address::locality, null),

                TsvProperty.ofString(REGION, Address::region, null), TsvProperty.ofString(COUNTRY, Address::country, null));
        return TsvEntity.of(Address.class, fields, imports);
    }
}