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

package net.tangly.erp.crm.rest;

import com.vaadin.base.devserver.themeeditor.messages.ErrorResponse;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import net.tangly.core.crm.NaturalEntity;
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.jetbrains.annotations.NotNull;

public class NaturalEntitiesRest {
    public static final String PREFIX = STR."/rest/\{CrmBoundedDomain.DOMAIN.toLowerCase()}/naturalentities";
    private final CrmBoundedDomain domain;

    NaturalEntitiesRest(CrmBoundedDomain domain) {
        this.domain = domain;
    }

    public void registerEndPoints(@NotNull Javalin javalin) {
        javalin.get(PREFIX, this::getAll);
        javalin.get(STR."\{PREFIX}/{id}", this::getById);
        javalin.put(PREFIX, this::create);
        javalin.patch(STR."\{PREFIX}/{id}", this::update);
        javalin.delete(STR."\{PREFIX}/{id}", this::delete);
    }

    @OpenApi(
        summary = "Get all natural entities",
        operationId = "getAllNaturalEntities",
        path = "/naturelentities",
        methods = HttpMethod.GET,
        tags = {"NaturalEntities"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = NaturalEntity[].class)})
        }
    )
    private void getAll(Context ctx) {
        ctx.json(naturalEntities().items());
    }

    @OpenApi(
        summary = "Get a natural entity by id",
        operationId = "getNaturalEntityById",
        path = "/naturalentities/:id",
        methods = HttpMethod.GET,
        tags = {"NaturalEntities"},
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = NaturalEntity.class)})
        }
    )
    private void getById(Context ctx) {
        String id = ctx.pathParam("id");
        Provider.findById(naturalEntities(), id).ifPresentOrElse(entity -> ctx.json(entity), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "Create a natural entity",
        operationId = "createNaturalEntity",
        path = "/naturalentities",
        methods = HttpMethod.POST,
        tags = {"NaturalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NaturalEntity.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void create(Context ctx) {
        NaturalEntity updated = ctx.bodyAsClass(NaturalEntity.class);
        naturalEntities().update(updated);
    }

    @OpenApi(
        summary = "Update a natural entity identified by ID",
        operationId = "updateNaturalEntityById",
        path = "/naturalentities/:id",
        methods = HttpMethod.PATCH,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"NaturalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NaturalEntity.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void update(Context ctx) {
        String id = ctx.pathParam("id");
        NaturalEntity updated = ctx.bodyAsClass(NaturalEntity.class);
        Provider.findById(naturalEntities(), id).ifPresentOrElse(entity -> naturalEntities().update(updated), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "delete a natural entity by ID",
        operationId = "deletaNaturalEntityById",
        path = "/naturalentities/:id",
        methods = HttpMethod.DELETE,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"NaturalEntities"},
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void delete(Context ctx) {
        String id = ctx.pathParam("id");
        Provider.findById(naturalEntities(), id).ifPresentOrElse(entity -> naturalEntities().delete(entity), () -> ctx.status(404));
    }

    private Provider<NaturalEntity> naturalEntities() {
        return domain.realm().naturalEntities();
    }
}
