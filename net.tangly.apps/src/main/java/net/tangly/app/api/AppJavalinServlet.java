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

import com.fasterxml.jackson.databind.node.TextNode;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.SecurityComponentConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.tangly.app.Application;

import java.io.IOException;

@WebServlet(name = "AppJavalinServlet", urlPatterns = {"/rest/*"})
public class AppJavalinServlet extends HttpServlet {
    public record RestConfiguration(String openApiName,
                                    String openApiUrl,
                                    String openApiEmail,
                                    String openApiLicense,
                                    String openApiLicenseIdentifier,
                                    String openApiServerUrl,
                                    String openApiServerDescription,
                                    String openApiTermsOfService,
                                    String openApiServerVersion) {
    }

    private final Javalin javalin;

    public AppJavalinServlet() {
        javalin = Javalin.createStandalone(AppJavalinServlet::create);
        Application.instance().tenants().stream().flatMap(o -> o.boundedDomainRests().values().stream()).forEach(o -> o.registerEndPoints(javalin));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        javalin.javalinServlet().service(req, resp);
    }

    private static void create(JavalinConfig config) {
        String docsPath = "/rest/openapi.json";

        OpenApiPluginConfiguration openApiConfiguration = new OpenApiPluginConfiguration()
            .withDocumentationPath(docsPath)
            .withDefinitionConfiguration((version, definition) -> definition
                .withOpenApiInfo(openApiInfo -> {
                    OpenApiContact openApiContact = new OpenApiContact();
                    openApiContact.setName("API Support");
                    openApiContact.setUrl("https://www.example.com/support");
                    openApiContact.setEmail("support@example.com");

                    OpenApiLicense openApiLicense = new OpenApiLicense();
                    openApiLicense.setName("Apache 2.0");
                    openApiLicense.setIdentifier("Apache-2.0");

                    openApiInfo.setDescription("Application REST API");
                    openApiInfo.setTermsOfService("https://example.com/tos");
                    openApiInfo.setContact(openApiContact);
                    openApiInfo.setLicense(openApiLicense);
                })
                .withServer(openApiServer -> {
                    openApiServer.setUrl(("http://localhost:{port}/{basePath}/" + version + "/"));
                    openApiServer.setDescription("Server description goes here");
                    openApiServer.addVariable("port", "8080", new String[]{"8080"}, "Port of the server");
                    openApiServer.addVariable("basePath", "rest", new String[]{"rest"}, "Base uri of the server");
                })
                // Based on official example: https://swagger.io/docs/specification/authentication/oauth2/
                .withSecurity(new SecurityComponentConfiguration()
                    .withSecurityScheme("BasicAuth", new BasicAuth())
                    .withSecurityScheme("BearerAuth", new BearerAuth())
                    .withSecurityScheme("ApiKeyAuth", new ApiKeyAuth())
                    .withSecurityScheme("CookieAuth", new CookieAuth("JSESSIONID"))
                    .withSecurityScheme("OpenID", new OpenID("https://example.com/.well-known/openid-configuration"))
                    .withSecurityScheme("OAuth2", new OAuth2("This API uses OAuth 2 with the implicit grant flow.")
                        .withFlow(new ImplicitFlow("https://api.example.com/oauth2/authorize")
                            .withScope("read_pets", "read your pets")
                            .withScope("write_pets", "modify pets in your account")
                        )
                        .withFlow(new ClientCredentials("https://api.example.com/credentials/authorize"))
                    )
                    .withGlobalSecurity(new Security("OAuth2")
                        .withScope("write_pets")
                        .withScope("read_pets"))
                )
                .withDefinitionProcessor(content -> { // you can add whatever you want to this document using your favourite json api
                    content.set("test", new TextNode("Value"));
                    return content.toPrettyString();
                })
            );
        config.plugins.register(new OpenApiPlugin(openApiConfiguration));

        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setUiPath("/rest/swagger");
        swaggerConfiguration.setDocumentationPath(docsPath);
        config.plugins.register(new SwaggerPlugin(swaggerConfiguration));
    }
}
