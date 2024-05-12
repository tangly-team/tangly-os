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
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.jetbrains.annotations.NotNull;

public class LegalEntitiesRest {
    public static final String PREFIX = STR."/rest/\{CrmBoundedDomain.DOMAIN.toLowerCase()}/legalentities";

    private final CrmBoundedDomain domain;

    LegalEntitiesRest(CrmBoundedDomain domain) {
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
        summary = "Get all legal entities",
        operationId = "getAllLegalEntities",
        path = "/legalentities",
        methods = HttpMethod.GET,
        tags = {"LegalEntities"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LegalEntity[].class)})
        }
    )
    private void getAll(Context ctx) {
        ctx.json(legalEntities().items());
    }

    @OpenApi(
        summary = "Get an entity by id",
        operationId = "getLegalEntityById",
        path = "/legalentities/:id",
        methods = HttpMethod.GET,
        tags = {"LegalEntities"},
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LegalEntity.class)})
        }
    )
    private void getById(Context ctx) {
        String id = ctx.pathParam("id");
        Provider.findById(legalEntities(), id).ifPresentOrElse(entity -> ctx.json(entity), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "Create legal entity",
        operationId = "createLegalEntity",
        path = "/legalentities",
        methods = HttpMethod.POST,
        tags = {"LegalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = LegalEntity.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void create(Context ctx) {
        LegalEntity updated = ctx.bodyAsClass(LegalEntity.class);
        legalEntities().update(updated);
    }

    @OpenApi(
        summary = "Update a legal entity identified by ID",
        operationId = "updateLegalEntityById",
        path = "/legalentities/:id",
        methods = HttpMethod.PATCH,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"LegalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = LegalEntity.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void update(Context ctx) {
        String id = ctx.pathParam("id");
        LegalEntity updated = ctx.bodyAsClass(LegalEntity.class);
        Provider.findById(legalEntities(), id).ifPresentOrElse(entity -> legalEntities().update(updated), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "delete a legal entity by ID",
        operationId = "deleteLegalEntityById",
        path = "/legalentities/:id",
        methods = HttpMethod.DELETE,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"LegalEntities"},
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void delete(Context ctx) {
        String id = ctx.pathParam("id");
        Provider.findById(legalEntities(), id).ifPresentOrElse(entity -> legalEntities().delete(entity), () -> ctx.status(404));
    }

    private Provider<LegalEntity> legalEntities() {
        return domain.realm().legalEntities();
    }
}
