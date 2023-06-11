/*
 * Copyright 2022-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collaborators;

import io.javalin.Javalin;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.collabortors.ports.CollaboratorsEntities;
import net.tangly.erp.collabortors.ports.CollaboratorsHdl;
import net.tangly.erp.collabortors.services.CollaboratorsBoundedDomain;
import net.tangly.erp.collabortors.services.CollaboratorsBusinessLogic;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class RestErpService {
    public static final int PORT = 8080;
    public CollaboratorsBoundedDomain domain;

    RestErpService() {
        domain = ofCollaboratorsDomain(null);
    }

    public static void main(String[] arguments) {
        var app = Javalin.create(config -> {
                             final String docsPath = "/openapi";

                             OpenApiConfiguration openApiConfiguration = new OpenApiConfiguration();
                             openApiConfiguration.getInfo().setTitle("tangly ERP");
                             openApiConfiguration.setDocumentationPath(docsPath);
                             config.plugins.register(new OpenApiPlugin(openApiConfiguration));

                             SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
                             swaggerConfiguration.setUiPath("/swagger");
                             swaggerConfiguration.setDocumentationPath(docsPath);
                             config.plugins.register(new SwaggerPlugin(swaggerConfiguration));

                             ReDocConfiguration reDocConfiguration = new ReDocConfiguration();
                             reDocConfiguration.setUiPath("/redoc");
                             reDocConfiguration.setDocumentationPath(docsPath);
                             config.plugins.register(new ReDocPlugin(reDocConfiguration));
                         })
                         .get("/", ctx -> ctx.result("Hello World"))
                         .start(PORT);
    }

    private CollaboratorsBoundedDomain ofCollaboratorsDomain(@NotNull String databases) {
        var registry = new TypeRegistry();
        var realm = (databases == null) ? new CollaboratorsEntities() : new CollaboratorsEntities(Path.of(databases, CollaboratorsBoundedDomain.DOMAIN));
        return new CollaboratorsBoundedDomain(realm, new CollaboratorsBusinessLogic(realm), new CollaboratorsHdl(realm), null, registry);
    }
}

class Collaborators implements CrudHandler {

    @Override
    public void create(@NotNull Context context) {

    }

    @Override
    public void delete(@NotNull Context context, @NotNull String s) {

    }

    @Override
    public void getAll(@NotNull Context context) {

    }

    @Override
    public void getOne(@NotNull Context context, @NotNull String s) {

    }

    @Override
    public void update(@NotNull Context context, @NotNull String s) {

    }
}
