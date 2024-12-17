/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.web.blog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;

public class Blogs {
    private static final Pattern yearPattern = Pattern.compile("\\/\\d\\d\\d\\d\\/");
    private static final String line = "| %4d | %3d | %3d | %#2.2f | %3d | %#2.2f";
    private static final String summary = "| Total | %3s | | | | ";
    private static final String BLOG_URL = "https://blog.tangly.net/blog";
    private static final String AMAZON_DP_URL = "/www.amazon.com/dp/";

    private final String blogUrl;
    private final List<Blog> blogs;

    public static void main(String[] args) {
        Blogs blogs = new Blogs(BLOG_URL);
        blogs.blogReferences();
        blogs.computeStatistics();
    }

    Blogs(String blogUrl) {
        this.blogUrl = blogUrl;
        blogs = new ArrayList<>();
    }

    private void computeStatistics() {
        var blogsPerYears = blogs.stream().collect(groupingBy(Blog::year));
        blogsPerYears.keySet().stream().forEachOrdered(year -> {
            int nrOfBlogs = blogsPerYears.get(year).size();
            int nrOfReferencedBlogs = blogsPerYears.get(year).stream().mapToInt(blog -> blog.referencedBlogs().size()).sum();
            int nrOfBibliographyReferences = blogsPerYears.get(year).stream().mapToInt(blog -> blog.bibliographyReferences().size()).sum();
            System.out.println(line.formatted(year, nrOfBlogs, nrOfReferencedBlogs, (float) nrOfReferencedBlogs / nrOfBlogs, nrOfBibliographyReferences,
                (float) nrOfBibliographyReferences / nrOfBlogs));
        });
        System.out.println(summary.formatted(blogs.size()));
        System.out.println();
        long nrOfUniqueBookCitations = blogs.stream().flatMap(o -> o.bibliographyReferences().stream()).distinct().count();
        System.out.println("Over the years, we cited %d different books in our articles.".formatted(nrOfUniqueBookCitations));
    }

    /**
     * Extracts the reference to the blog posts from the blog post content and references to bibliography entries.
     * The pattern for the blog reference is <em>/blog/YEAR/</em>.
     * The pattern to a referenced blog is <em>../../YEAR/</em>.
     * The pattern to a referenced bibliography entry is <em>/www.amazon.com/dp/<NUMBER></em>.
     */
    private void blogReferences() {
        try {
            Document blogsPage = Jsoup.connect(blogUrl).get();
            Elements blogs = blogsPage.getElementsByClass("td-sidebar-nav__section-title td-sidebar-nav__section without-child");
            for (Element blog : blogs) {
                blog.select("a[href]").forEach(a -> {
                    String href = a.attr("href");
                    if (href.startsWith("/blog/")) {
                        processBlog(blogUrl + href.substring(5));
                    }
                });

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processBlog(String blogUrl) {
        Matcher matcher = yearPattern.matcher(blogUrl);
        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group().substring(1, 5));
            try {
                List<String> referencedBlogs = new ArrayList<>();
                List<String> bibliographyReferences = new ArrayList<>();
                Document blogPage = Jsoup.connect(blogUrl).get();
                Elements references = blogPage.select("a[href]");
                references.forEach(a -> {
                    String href = a.attr("href");
                    if (href.startsWith("../../")) {
                        String referencedBlog = blogUrl + href.substring(6);
                        referencedBlogs.add(referencedBlog);
                    } else if (href.contains(AMAZON_DP_URL)) {
                        String bilbiographyReference = href;
                        bibliographyReferences.add(bilbiographyReference);
                    }
                });
                blogs.add(new Blog(blogUrl, year, bibliographyReferences, referencedBlogs));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
