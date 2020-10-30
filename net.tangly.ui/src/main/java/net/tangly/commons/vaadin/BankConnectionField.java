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
import net.tangly.bus.core.BankConnection;

/**
 * A composite field to update an immutable bonk connection entity.
 *
 * @see BankConnection
 */
public class BankConnectionField extends CustomField<BankConnection> {
    private final TextField iban;
    private final TextField institute;
    private final TextField bic;

    public BankConnectionField() {
        setLabel("Bank Connection");
        iban = new TextField("IBAN", "iban number");
        iban.setRequired(true);
        institute = new TextField("Bank", "institute name");
        bic = new TextField("BIC", "bic");
        HorizontalLayout horizontalLayout = new HorizontalLayout(iban, institute, bic);
        add(horizontalLayout);
    }

    public Validator<BankConnection> validator() {
        return Validator.from(e -> e.iban() != null, "IBAN number is invalid", ErrorLevel.ERROR);
        //         .andThen(Validator.<BankConnection>from(e -> e.bic() != null, "bic should not be null", ErrorLevel.INFO));
        //         .andThen(Validator.<BankConnection>from(e -> e.institute() != null, "institute should not be null", ErrorLevel.INFO));
    }

    @Override
    protected BankConnection generateModelValue() {
        return new BankConnection(iban.getValue(), institute.getValue(), bic.getValue());
    }

    @Override
    protected void setPresentationValue(BankConnection connection) {
        if (connection == null) {
            clear();
        } else {
            VaadinUtils.setValue(iban, connection.iban());
            VaadinUtils.setValue(institute, connection.institute());
            VaadinUtils.setValue(bic, connection.bic());
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        iban.setReadOnly(readOnly);
        institute.setReadOnly(readOnly);
        bic.setReadOnly(readOnly);
    }

    @Override
    public void clear() {
        iban.clear();
        institute.clear();
        bic.clear();
    }
}
