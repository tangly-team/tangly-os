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

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.PasswordField;
import net.tangly.core.Entity;
import net.tangly.core.crm.NaturalEntity;
import net.tangly.erp.Erp;
import net.tangly.erp.crm.domain.Subject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static net.tangly.erp.crm.ui.CmdChangePassword.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("IntegrationTest")
class CmdChangePasswordTest extends CrmTest {
    @Test
    void testChangePasswordCorrectly() {
        var subject = createSubject();
        Erp.instance().crmBoundedDomain().realm().subjects().update(subject);
        var cmd = new CmdChangePassword(Erp.instance().crmBoundedDomain(), subject);
        assertThat(cmd).isNotNull();
        assertThat(subject.authenticate("old-password")).isTrue();
        cmd.execute();
        Component form = form(cmd);
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CURRENT_PASSWORD)), "old-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withText(EXECUTE)));
        assertThat(subject.authenticate("new-password")).isTrue();
    }

    @Test
    void testChangePasswordWrongly() {
        var subject = createSubject();
        Erp.instance().crmBoundedDomain().realm().subjects().update(subject);
        var cmd = new CmdChangePassword(Erp.instance().crmBoundedDomain(), subject);
        assertThat(cmd).isNotNull();
        assertThat(subject.authenticate("old-password")).isTrue();
        cmd.execute();
        Component form = form(cmd);
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CURRENT_PASSWORD)), "dummy-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withText(EXECUTE)));
        assertThat(subject.authenticate("old-password")).isTrue();
        assertThat(subject.authenticate("new-password")).isFalse();
    }

    @Test
    void testChangePasswordCanceled() {
        var subject = createSubject();
        Erp.instance().crmBoundedDomain().realm().subjects().update(subject);
        var cmd = new CmdChangePassword(Erp.instance().crmBoundedDomain(), subject);
        assertThat(cmd).isNotNull();
        assertThat(subject.authenticate("old-password")).isTrue();
        cmd.execute();
        Component form = form(cmd);
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CURRENT_PASSWORD)), "dummy-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withText(CANCEL)));
        assertThat(subject.authenticate("old-password")).isTrue();
        assertThat(subject.authenticate("new-password")).isFalse();
    }

    private Subject createSubject() {
        var person = new NaturalEntity(Entity.UNDEFINED_OID);
        person.firstname("John");
        person.name("Doe");
        var subject = new Subject(Entity.UNDEFINED_OID);
        subject.user(person);
        subject.id("john-doe");
        subject.newPassword("old-password");
        return subject;
    }
}
