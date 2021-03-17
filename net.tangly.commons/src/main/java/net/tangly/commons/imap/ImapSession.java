/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.imap;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.MessageIDTerm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImapSession implements Closeable {
    private static final String GMAIL_IMAP_SERVER = "imap.googlemail.com";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Store store;
    private Session session;
    private IMAPFolder allMail;

    public ImapSession(@NotNull String username, @NotNull String password) {
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect(GMAIL_IMAP_SERVER, username, password);
            allMail = (IMAPFolder) store.getFolder("[Gmail]/All Mail");
            allMail.open(Folder.READ_ONLY);
        } catch (MessagingException e) {
            logger.atError().setCause(e).log("Error when creating an IMAP session");
        }
    }

    public List<Message> messages(@NotNull String id) {
        MessageIDTerm messageIDTerm = new MessageIDTerm(id);
        List<Message> messages = null;
        try {
            Message[] items = allMail.search(messageIDTerm);
            messages = Arrays.asList(items);
        } catch (MessagingException e) {
            logger.atError().setCause(e).log("Error when searching messages");
        }
        return messages;
    }

    @Override
    public void close() {
        try {
            allMail.close();
        } catch (MessagingException e) {
            logger.atError().setCause(e).log("Error when closing folder");
        }
    }

    public List<Object> select(MimeMultipart multipart, String mimeType) {
        List<Object> parts = new ArrayList<>();
        try {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType(mimeType)) {
                    parts.add(bodyPart.getContent());
                }
                if (bodyPart.getContent() instanceof MimeMultipart part) {
                    parts.addAll(select(part, mimeType));
                }
            }
        } catch (MessagingException | IOException e) {
            logger.atError().setCause(e).log("Error when selecting MIME multiparts");
        }
        return parts;
    }

    public List<Object> selectTextParts(MimeMultipart multipart) {
        return select(multipart, "TEXT/PLAIN");
    }

    public List<Object> selectTextPartsFrom(String messageId) {
        List<Object> parts = null;
        List<Message> messages = messages(messageId);
        if (!messages.isEmpty()) {
            Message msg = messages.get(0);
            try {
                parts = selectTextParts((MimeMultipart) msg.getContent());
            } catch (IOException | MessagingException e) {
                logger.atError().setCause(e).log("Error when selecting MIME multiparts");
            }
        } else {
            parts = Collections.emptyList();
        }
        return parts;
    }
}
