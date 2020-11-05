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

package net.tangly.bus.crm;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import net.tangly.core.QualifiedEntityImp;
import net.tangly.bus.gravatar.Gravatar;
import net.tangly.bus.gravatar.GravatarImage;
import net.tangly.bus.gravatar.GravatarRating;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the abstraction of a registered subject entitled to use the application and associated services. The id of the subject is the human readable unique
 * identifier.
 */
public class Subject extends QualifiedEntityImp {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private NaturalEntity user;
    private String gravatarEmail;
    private String passwordSalt;
    private String passwordHash;
    private String gmailUsername;
    private String gmailPassword;
    private transient byte[] avatar;

    public Subject() {
    }

    public static String encryptPassword(@NotNull String password, @NotNull String salt) throws Exception {
        int derivedKeyLength = 160;
        int iterations = 20_000;
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] encBytes = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(encBytes);
    }

    public static String newSalt() throws Exception {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public NaturalEntity user() {
        return user;
    }

    public void user(NaturalEntity user) {
        this.user = user;
    }

    public String gravatarEmail() {
        return gravatarEmail;
    }

    public void gravatarEmail(String gravatarEmail) {
        this.gravatarEmail = gravatarEmail;
    }

    public String passwordSalt() {
        return passwordSalt;
    }

    public void passwordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public void passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String gmailUsername() {
        return gmailUsername;
    }

    public void gmailUsername(String gmailUsername) {
        this.gmailUsername = gmailUsername;
    }

    public String gmailPassword() {
        return gmailPassword;
    }

    public void gmailPassword(String gmailPassword) {
        this.gmailPassword = gmailPassword;
    }

    public void newPassword(String password) throws Exception {
        passwordSalt(Subject.newSalt());
        passwordHash(Subject.encryptPassword("aeon", passwordSalt()));

    }

    public boolean authenticate(@NotNull String password) throws Exception {
        return encryptPassword(password, passwordSalt()).equals(passwordHash());
    }

    public byte[] avatar() {
        if (avatar == null) {
            Gravatar gravatar = new Gravatar();
            avatar = gravatar.avatar(gravatarEmail, 200, GravatarRating.GENERAL_AUDIENCES, GravatarImage.GRAVATAR_ICON);
        }
        return avatar;
    }
}
