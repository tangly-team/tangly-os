/*
 * Copyright 2023-2024 Marcel Baumann
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

package net.tangly.web.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks the links of a website to detect dead links. Non-relevant links are filtered out:
 * <dl>
 *     <dt>.xml|.ico|.png|.css|.pdf|jquery|mathjax</dt><dd>Included libraries and PDF files do not need to be checked.</dd>
 *     <dt>#_footnoteref_|#_footnotedef_</dt><dd>footnotes references are not semantic links.</dd>
 *     <dt>en.wikipedia.org|www.amazon.com/dp/|mvnrepository.com|oss.sonatype.org|sourceforge.net</dt><dd>Sites which do not like to be queried.</dd>
 * </dl>
 * <p>A specific link is checked once.</p>
 */
public class LinkChecker {
    public static int LINK_CHECK_TIMEOUT = 1000;
    private static final Logger logger = LogManager.getLogger();

    record Error(String source, String link, String error, int errorCode) {
    }

    record BlogInfo(String link, Year year, List<String> blogReferences, List<String> bookReferences) {
    }

    private String websiteUri;
    private Pattern shouldNotBeValidated;
    private Pattern isApiDocs;
    private final Set<String> validatedLinks;
    private final Set<String> validatedPages;
    private final List<Error> errors;

    public static void main(String[] args) throws IOException {
        try {
            LinkChecker checker = new LinkChecker();
            checker.websiteUri = "https://blog.tangly.net";
            checker.shouldNotBeValidated = Pattern.compile(".*(\\.xml|\\.ico|\\.png|\\.jpg|\\.css|\\.pdf|jquery|mathjax|/#_footnoteref_|#_footnotedef_|" +
                "en\\.wikipedia\\.org|www\\.amazon\\.com/dp/|mvnrepository\\.com|oss\\.sonatype\\.org|sourceforge\\.net|googlemananger|#$)", Pattern.CASE_INSENSITIVE);
            checker.isApiDocs = Pattern.compile(
                "/docs/bdd/api-bdd/|/docs/bib/api-bib/|/docs/core/api-core/|/docs/dev/api-dev/|/docs/facts/api-facts/|/docs/fsm/api-fsm/|/docs/gleam/api-gleam/|" +
                    "/docs/domains/agile/api-agile|/docs/domains/collaborators/api-collaborators|/docs/domains/crm/api-crm|" +
                    "/docs/domains/invoices/api-invoices|/docs/domains/ledger/api-ledger|/docs|/domains/products/api-products", Pattern.CASE_INSENSITIVE);
            checker.parse(checker.websiteUri);
        } catch (Exception e) {
            logger.atError().log(e);
        }
    }

    public void parse(String url) throws IOException {
        validatedPages.add(url);
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

        for (Element src : media) {
            //            out.printf(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
        }
        for (Element link : imports) {
            //            out.printf(" * %s <%s> (%s)", link.tagName(), link.attr("abs:href"), link.attr("rel"));
        }

        for (Element link : links) {
            String linkUrl = link.attr("abs:href");
            boolean validated = shouldLinkBeValidated(linkUrl) ? validateLink(url, linkUrl) : false;
            // if the link is validated and belongs to the website, the associated webpage should be parsed.
            if (validated && shouldBeParsed(linkUrl)) {
                parse(linkUrl);
            }
        }
    }

    public LinkChecker() {
        validatedLinks = new HashSet<>();
        validatedPages = new HashSet<>();
        errors = new ArrayList<>();
    }

    private boolean validateLink(@NotNull String source, @NotNull String url) {
        if (validatedLinks.contains(url)) {
            return true;
        }
        logger.atInfo().log(STR."validate [\{url}] from [\{source}]");
        Error error = null;
        int code = HttpURLConnection.HTTP_NOT_FOUND;
        try {
            URI link = URI.create(url);
            HttpURLConnection connection = (HttpURLConnection) link.toURL().openConnection();
            connection.setConnectTimeout(LINK_CHECK_TIMEOUT);
            connection.setRequestMethod("HEAD");
            code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                connection.setRequestMethod("GET");
                connection.setReadTimeout(LINK_CHECK_TIMEOUT);
                code = connection.getResponseCode();
            }
            error = switch (code) {
                case HttpURLConnection.HTTP_OK -> null;
                case HttpURLConnection.HTTP_NOT_FOUND -> new Error(source, url, "http not found", code);
                case HttpURLConnection.HTTP_MOVED_TEMP -> new Error(source, url, "http moved", code);
                default -> new Error(source, url, "http not ok", code);
            };
        } catch (MalformedURLException e) {
            error = new Error(source, url, "malformed URL exception", code);
        } catch (SocketTimeoutException e) {
            error = new Error(source, url, "socket timeout exception", code);
        } catch (IOException e) {
            error = new Error(source, url, "IO exception", code);
        } catch (IllegalArgumentException e) {
            error = new Error(source, url, "illegal argument exception", code);
        } catch (IllegalStateException e) {
            error = new Error(source, url, "illegal state exception", code);
        }
        if (error != null) {
            errors.add(error);
            logger.atError().log(error);
        } else {
            validatedLinks.add(url);
        }
        return (error == null);
    }

    /**
     * A link should be validated if it is the first time it is checked and it is not excluded.
     *
     * @param url to check for correctness
     * @return true if it should be checked, otherwise false.
     */
    private boolean shouldLinkBeValidated(@NotNull String url) {
        return !shouldNotBeValidated.matcher(url).find() && !isApiDocs.matcher(url).find();
    }

    private boolean shouldBeParsed(@NotNull String url) {
        return !validatedPages.contains(url) && url.contains(websiteUri);
    }
}
