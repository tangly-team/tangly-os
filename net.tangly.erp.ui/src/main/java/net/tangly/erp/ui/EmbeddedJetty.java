/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.ui;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.startup.ServletContextListeners;
import net.tangly.erp.Erp;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Entry point to start the application as regular Java SE application with an embedded Jetty server.
 * The application parameters are:
 * <dl>
 *     <dt>port</dt>
 *     <dd>The parameter defines the listening port of the Jetty embedded server.</dd>
 *     <dt>mode</dt>
 *     <dd>The parameter defines the mode of the application.
 *     The application is either using an in-memory data or has a persistent storage where import data can be provided and updated stored in the database.</dd>
 * </dl>
 */
public class EmbeddedJetty {
    private static final Logger logger = LogManager.getLogger();
    private static Server server;
    private static int port =8080;
    private static boolean isInMemory = true;

    public static void main(@NotNull String[] args) throws Exception {
        final String contextRoot = "/erp";

        if (isProductionMode()) {
            logger.info("Production mode detected, enforcing");
            System.setProperty("vaadin.productionMode", "true");
        }
        logger.info("command line arguments are {}", args);
        parse(args);
        logger.info("port is {} and inMemoryMode is {}", port, isInMemory);
        final WebAppContext context = new WebAppContext();
        context.setBaseResource(findWebRoot());
        context.setContextPath(contextRoot);
        context.addServlet(VaadinServlet.class, "/*");
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*\\.jar|.*/classes/.*");
        context.setConfigurationDiscovered(true);
        context.getServletContext().setExtendedListenerTypes(true);
        context.addEventListener(new ServletContextListeners());

        server = new Server(port);
        if (isInMemory) {
            Erp.inMemoryErp();
        } else {
            Erp.propertiesConfiguredErp();
        }
        server.setHandler(context);
        server.start();
    }

    public static void stop() throws Exception {
        server.stop();
        server = null;
    }

    private static boolean isProductionMode() {
        final String probe = "META-INF/maven/com.vaadin/flow-server-production-mode/pom.xml";
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(probe) != null;
    }

    @NotNull
    private static Resource findWebRoot() throws MalformedURLException {
        // don't look up directory as a resource, it's unreliable: https://github.com/eclipse/jetty.project/issues/4173#issuecomment-539769734
        // instead we'll look up the /webapp/ROOT and retrieve the parent folder from that.
        final URL file = EmbeddedJetty.class.getResource("/webapp/ROOT");
        if (file == null) {
            throw new IllegalStateException("Invalid state: the resource /webapp/ROOT doesn't exist, has webapp been packaged in as a resource?");
        }
        final String url = file.toString();
        if (!url.endsWith("/ROOT")) {
            throw new RuntimeException("Parameter url: invalid value " + url + ": doesn't end with /ROOT");
        }
        logger.atDebug().log("/webapp/ROOT is {}", file);

        // Resolve file to directory
        URL webRoot = new URL(url.substring(0, url.length() - 5));
        logger.atDebug().log("WebRoot is {}", webRoot);
        return Resource.newResource(webRoot);
    }

    private static Options options() {
        var options = new Options();
        options.addOption(Option.builder("p").longOpt("port").type(Integer.TYPE).argName("port").hasArg().desc("listening port of the embedded server").build());
        options.addOption(Option.builder("m").longOpt("mode").argName("mode").hasArg().desc("mode of the application").build());
        return options;
    }

    private static void parse(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options(), args);
            port = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : 8080;
            isInMemory = (line.hasOption("m")) ? Boolean.parseBoolean(line.getOptionValue("m")) : true;
        } catch (NumberFormatException | ParseException e) {
            logger.atError().log("Parsing failed.  Reason: {}", e.getMessage());
        }
    }
}
