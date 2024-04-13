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

package net.tangly.app.api;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.SecurityConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@WebServlet(name = "AppJavalinServlet", urlPatterns = {"/rest/*"})
public class AppJavalinServlet extends HttpServlet {
    private final Javalin javalin;

    public AppJavalinServlet() {
        javalin = Javalin.createStandalone();
        registerEndPoints(javalin);
    }

    public void registerEndPoints(Javalin javalin) {
        javalin.get("/rest/domain", AppJavalinServlet::helloWorld);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        javalin.javalinServlet().service(req, resp);
    }

    @OpenApi(
        summary = "Hello World",
        operationId = "helloWorld",
        path = "/domain",
        methods = HttpMethod.GET,
        tags = {"domain"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = String.class)})
        }
    )
    private static void helloWorld(Context ctx) {
        ctx.result("Hello REST services!");
    }

    private static void create(JavalinConfig config) {
        String deprecatedDocsPath = "/swagger-docs";

        OpenApiContact openApiContact = new OpenApiContact();
        openApiContact.setName("API Support");
        openApiContact.setUrl("https://www.example.com/support");
        openApiContact.setEmail("support@example.com");

        OpenApiLicense openApiLicense = new OpenApiLicense();
        openApiLicense.setName("Apache 2.0");
        openApiLicense.setIdentifier("Apache-2.0");

        OpenApiInfo openApiInfo = new OpenApiInfo();
        openApiInfo.setTitle("Awesome App");
        openApiInfo.setSummary("App summary");
        openApiInfo.setDescription("App description goes right here");
        openApiInfo.setTermsOfService("https://example.com/tos");
        openApiInfo.setContact(openApiContact);
        openApiInfo.setLicense(openApiLicense);
        openApiInfo.setVersion("1.0.0");

        OpenApiServerVariable portServerVariable = new OpenApiServerVariable();
        portServerVariable.setValues(new String[]{"7070", "8080"});
        portServerVariable.setDefault("8080");
        portServerVariable.setDescription("Port of the server");

        OpenApiServerVariable basePathServerVariable = new OpenApiServerVariable();
        basePathServerVariable.setValues(new String[]{"v1"});
        basePathServerVariable.setDefault("v1");
        basePathServerVariable.setDescription("Base path of the server");

        OpenApiServer openApiServer = new OpenApiServer();
        openApiServer.setUrl("https://example.com:{port}/{basePath}");
        openApiServer.setDescription("Server description goes here");
        openApiServer.addVariable("port", portServerVariable);
        openApiServer.addVariable("basePath", basePathServerVariable);

        OpenApiServer[] servers = new OpenApiServer[]{openApiServer};

        OpenApiConfiguration openApiConfiguration = new OpenApiConfiguration();
        openApiConfiguration.setInfo(openApiInfo);
        openApiConfiguration.setServers(servers);
        openApiConfiguration.setDocumentationPath(deprecatedDocsPath); // by default it's /openapi
        // Based on official example: https://swagger.io/docs/specification/authentication/oauth2/
        openApiConfiguration.setSecurity(new SecurityConfiguration(
            Map.ofEntries(
                entry("BasicAuth", new BasicAuth()),
                entry("BearerAuth", new BearerAuth()),
                entry("ApiKeyAuth", new ApiKeyAuth()),
                entry("CookieAuth", new CookieAuth("JSESSIONID")),
                entry("OpenID", new OpenID("https://example.com/.well-known/openid-configuration"))
            ),
            List.of(
                new Security(
                    "oauth2",
                    List.of(
                        "write_pets",
                        "read_pets"
                    )
                )
            )
        ));
        config.plugins.register(new OpenApiPlugin(openApiConfiguration));

        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setDocumentationPath(deprecatedDocsPath);
        config.plugins.register(new SwaggerPlugin(swaggerConfiguration));
    }
}
