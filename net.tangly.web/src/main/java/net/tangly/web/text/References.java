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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class References {
    private final Map<String, Integer> books;
    private final Map<String, Integer> articles;
    private String websiteUri;
    private final Pattern isBookReference;
    private final Pattern isInternalBlogReference;

    References() {
        books = new HashMap<>();
        articles = new HashMap<>();
        websiteUri = "blog.tangly.net";
        isInternalBlogReference = Pattern.compile(STR."\{websiteUri}blog");
        isBookReference = Pattern.compile("www.amazon.com/dp/");
    }

    boolean isBookReference(String url) {
        return isBookReference.matcher(url).find();
    }

    void addBookReference(String url) {
        books.compute(url, (k, v) -> (v == null) ? 1 : v + 1);
    }

    boolean isInternalBlogReference(String url) {
        return isInternalBlogReference.matcher(url).find();
    }

    void addInternalBlogReference(String url) {
        articles.compute(url, (k, v) -> (v == null) ? 1 : v + 1);
    }
}
