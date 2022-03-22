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

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.startup.ServletContextListeners;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jetbrains.annotations.NotNull;

public class EmbeddedJetty {
    private static final Logger logger = LogManager.getLogger();
    private static Server server;

    public static void main(String[] args) throws Exception {
        if (isProductionMode()) {
            // fixes https://github.com/mvysny/vaadin14-embedded-jetty/issues/1
            logger.atInfo().log("Production mode detected, enforcing");
            System.setProperty("vaadin.productionMode", "true");
        }
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        final WebAppContext webAppContext = new WebAppContext();
        webAppContext.setBaseResource(findWebRoot());
        webAppContext.setContextPath("/");
        webAppContext.addServlet(VaadinServlet.class, "/erp/*");

//        ServletHolder jerseyServlet = webAppContext.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/webapi/*");
//        jerseyServlet.setInitOrder(0);
//        jerseyServlet.setInitParameter("jersey.config.server.provider.packages","net.tangly.erp.crm.ports;io.swagger.jaxrs.json;io.swagger.jaxrs.listing");

//         Setup Swagger servlet
//        ServletHolder swaggerServlet = context.addServlet(DefaultJaxrsConfig.class, "/swagger-core");
//        swaggerServlet.setInitOrder(2);
//        swaggerServlet.setInitParameter("api.version", "1.0.0");


        webAppContext.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*\\.jar|.*/classes/.*");
        webAppContext.setConfigurationDiscovered(true);
        // OWASP mitigating the Most Common XSS attack using HttpOnly
        webAppContext.getSessionHandler().setHttpOnly(true);
        webAppContext.getServletContext().setExtendedListenerTypes(true);
        webAppContext.addEventListener(new ServletContextListeners());

        WebSocketServerContainerInitializer.initialize(webAppContext);
        // fixes IllegalStateException: Unable to configure jsr356 at that stage. ServerContainer is null

        int port = (args.length >= 1) ? Integer.parseInt(args[0]) : 8080;
        server = new Server(port);
        server.setHandler(webAppContext);
        final Configuration.ClassList classes = Configuration.ClassList.setServerDefault(server);
        classes.addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
        server.start();

        logger.atInfo().log("""
            =================================================
            Please open http://localhost:" + port + " in your preferred browser
            If you see the 'Unable to determine mode of operation' exception, just kill me and run `./gradlew vaadinPrepareFrontend`
            =================================================
            """);
        server.join();
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
}
