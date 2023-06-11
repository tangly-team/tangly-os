/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.components;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.core.Address;

/**
 * A composite field to update an immutable entity.
 *
 * @see Address
 */
@Tag("tangly-field-address")
public class AddressField extends CustomField<Address> {
    private final TextField street;
    private final TextField extended;
    private final TextField poBox;
    private final TextField postcode;
    private final TextField locality;
    private final TextField region;
    private final TextField country;
    private final Binder<Address> binder;

    public AddressField() {
        setLabel("Address");
        street = new TextField("Street", "street");
        extended = new TextField("Extended", "extended");
        poBox = new TextField("PO Box", "postal box");
        postcode = new TextField("Zipcode", "zipcode");
        locality = new TextField("Locality", "locality");
        locality.setRequired(true);
        region = new TextField("Region", "region");
        country = new TextField("Country", "country");
        country.setRequired(true);
        HorizontalLayout horizontalLayout = new HorizontalLayout(street, extended, poBox, postcode, locality, region, country);
        add(horizontalLayout);
        binder = new Binder<>(Address.class);
        binder.bindReadOnly(street, Address::street);
        binder.bindReadOnly(extended, Address::extended);
        binder.bindReadOnly(poBox, Address::poBox);
        binder.bindReadOnly(postcode, Address::postcode);
        binder.bindReadOnly(locality, Address::locality);
        binder.bindReadOnly(region, Address::region);
        binder.forField(country).withValidator(country -> country.length() == 2, "ISO country name is two characters").bindReadOnly(Address::country);
    }

    @Override
    protected Address generateModelValue() {
        return Address.builder().street(street.getValue()).extended(extended.getValue()).poBox(poBox.getValue()).postcode(postcode.getValue()).locality(locality.getValue())
            .region(region.getValue()).country(country.getValue()).build();
    }

    @Override
    protected void setPresentationValue(Address address) {
        if (address == null) {
            clear();
        } else {
            binder.readBean(address);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        street.setReadOnly(readOnly);
        extended.setReadOnly(readOnly);
        poBox.setReadOnly(readOnly);
        postcode.setReadOnly(readOnly);
        locality.setReadOnly(readOnly);
        region.setReadOnly(readOnly);
        country.setReadOnly(readOnly);
    }

    @Override
    public void clear() {
        super.clear();
        street.clear();
        extended.clear();
        poBox.clear();
        postcode.clear();
        locality.clear();
        region.clear();
        country.clear();
    }
}
