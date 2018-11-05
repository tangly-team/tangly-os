/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.crm;

import java.io.Serializable;

/**
 * Describes a bank connection with IBAN account number, BIC identification and name of the institute. The class is mmutable.
 */
public class BankConnection implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String iban;
    private final String bic;
    private final String institute;

    public BankConnection(String iban, String bic, String institute) {
        this.iban = iban;
        this.bic = bic;
        this.institute = institute;
    }

    public String iban() {
        return iban;
    }

    public String bic() {
        return bic;
    }

    public String institute() {
        return institute;
    }
}
