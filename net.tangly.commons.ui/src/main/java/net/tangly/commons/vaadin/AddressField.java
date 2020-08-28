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

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.Validator;
import net.tangly.bus.core.Address;

/**
 * A composite field to update an address immutable entity.
 */

public class AddressField extends CustomField<Address> {
    private final TextField street;
    private final TextField extended;
    private final TextField poBox;
    private final TextField postcode;
    private final TextField locality;
    private final TextField region;
    private final TextField country;

    public AddressField() {
        setLabel("Address");
        street = new TextField("Street", "street");
        extended = new TextField("Extended", "extended");
        poBox = new TextField("PO Box", "postal box");
        postcode = new TextField("Zipcode", "zipcode");
        locality = new TextField("Locality", "locality");
        region = new TextField("Region", "region");
        country = new TextField("Country", "country");
        country.setRequired(true);
        HorizontalLayout horizontalLayout = new HorizontalLayout(street, extended, poBox, postcode, locality, region, country);
        add(horizontalLayout);
    }

    @Override
    protected Address generateModelValue() {
        Address address = super.getValue();
        return (address != null) ? address :
                Address.builder().street(street.getValue()).extended(extended.getValue()).poBox(poBox.getValue()).postcode(postcode.getValue())
                        .locality(locality.getValue()).region(region.getValue()).country(country.getValue()).build();
    }

    public Validator<Address> validator() {
        return Validator.from(e -> e.locality() != null, "Locality number is invalid", ErrorLevel.INFO);
        //      .andThen(Validator.<Address>from(e -> e.postcode != null, "postcode should not be null", ErrorLevel.INFO));
    }

    @Override
    protected void setPresentationValue(Address address) {
        if (address == null) {
            clear();
        } else {
            VaadinUtils.setValue(street, address.street());
            VaadinUtils.setValue(extended, address.extended());
            VaadinUtils.setValue(poBox, address.poBox());
            VaadinUtils.setValue(postcode, address.postcode());
            VaadinUtils.setValue(locality, address.locality());
            VaadinUtils.setValue(region, address.region());
            VaadinUtils.setValue(country, address.country());
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
