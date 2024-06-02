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

package net.tangly.apps.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.PasswordField;
import net.tangly.app.Application;
import net.tangly.app.ui.CmdChangePassword;
import net.tangly.core.domain.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static net.tangly.app.ui.CmdChangePassword.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("IntegrationTest")
@Disabled
class CmdChangePasswordTest extends AppsTest {
    @Test
    void testChangePasswordCorrectly() {
        var user = createUser();
        Application.instance().apps().realm().users().update(user);
        var cmd = new CmdChangePassword(Application.instance().apps(), user);
        assertThat(cmd).isNotNull();
        assertThat(user.authenticate("old-password")).isTrue();
        cmd.execute();
        Component form = form(cmd);
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CURRENT_PASSWORD)), "old-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withText(EXECUTE)));
        assertThat(user.authenticate("new-password")).isTrue();
    }

    @Test
    void testChangePasswordWrongly() {
        var user = createUser();
        Application.instance().apps().realm().users().update(user);
        var cmd = new CmdChangePassword(Application.instance().apps(), user);
        assertThat(cmd).isNotNull();
        assertThat(user.authenticate("old-password")).isTrue();
        cmd.execute();
        Component form = form(cmd);
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CURRENT_PASSWORD)), "dummy-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withText(EXECUTE)));
        assertThat(user.authenticate("old-password")).isTrue();
        assertThat(user.authenticate("new-password")).isFalse();
    }

    @Test
    void testChangePasswordCanceled() {
        var user = createUser();
        Application.instance().apps().realm().users().update(user);
        var cmd = new CmdChangePassword(Application.instance().apps(), user);
        assertThat(cmd).isNotNull();
        assertThat(user.authenticate("old-password")).isTrue();
        cmd.execute();
        Component form = form(cmd);
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CURRENT_PASSWORD)), "dummy-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withLabel(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withText(CANCEL)));
        assertThat(user.authenticate("old-password")).isTrue();
        assertThat(user.authenticate("new-password")).isFalse();
    }

    private User createUser() {
        String salt = User.newSalt();
        String password = User.encryptPassword("old-password", salt);
        return new User("john-doe", password, salt, true, null, Collections.emptyList(), "john.doe@gmail.com");
    }
}
