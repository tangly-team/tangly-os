/*
 * Copyright 2006-2021 Marcel Baumann
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

package net.tangly.commons.ui;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.startup.ServletContextListeners;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.*;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

public class EmbeddedJetty {
    private static Server server;

    public static void main(String[] args) throws Exception {
        start(args);
        server.join();
    }

    public static void start(String[] args) throws Exception {

        // detect&enable production mode
        if (isProductionMode()) {
            // fixes https://github.com/mvysny/vaadin14-embedded-jetty/issues/1
            System.out.println("Production mode detected, enforcing");
            System.setProperty("vaadin.productionMode", "true");
        }

        final WebAppContext context = new WebAppContext();
        context.setBaseResource(findWebRoot());
        context.setContextPath("/");
        context.addServlet(VaadinServlet.class, "/*");
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*\\.jar|.*/classes/.*");
        context.setConfigurationDiscovered(true);
        context.getServletContext().setExtendedListenerTypes(true);
        context.addEventListener(new ServletContextListeners());
        WebSocketServerContainerInitializer.initialize(context);
        // fixes IllegalStateException: Unable to configure jsr356 at that stage. ServerContainer is null

        int port = 8080;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        server = new Server(port);
        server.setHandler(context);
        final Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
        server.start();

        System.out.println("\n\n=================================================\n\n" +
            "Please open http://localhost:" + port + " in your browser\n\n" +
            "If you see the 'Unable to determine mode of operation' exception, just kill me and run `./gradlew vaadinPrepareFrontend`\n\n" +
            "=================================================\n\n");
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
        System.err.println("/webapp/ROOT is " + file);

        // Resolve file to directory
        URL webRoot = new URL(url.substring(0, url.length() - 5));
        System.err.println("WebRoot is " + webRoot);
        return Resource.newResource(webRoot);
    }
}
