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

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import net.tangly.core.HasOid;
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.jetbrains.annotations.NotNull;

public class LegalEntitiesRest {
    record LegalEntityView(String id, String name, String text) {
        public static LegalEntityView of(@NotNull LegalEntity entity) {
            return new LegalEntityView(entity.id(), entity.name(), entity.text());
        }

        public LegalEntity update(@NotNull LegalEntity entity) {
            entity.name(name());
            entity.text(text());
            return entity;
        }
    }

    public static final String PREFIX = "/rest/%s/legal-entities".formatted(CrmBoundedDomain.DOMAIN.toLowerCase());

    private final CrmBoundedDomain domain;

    LegalEntitiesRest(CrmBoundedDomain domain) {
        this.domain = domain;
    }

    public void registerEndPoints(@NotNull Javalin javalin) {
        javalin.get(PREFIX, this::getAll);
        javalin.get("%s/id".formatted(PREFIX), this::getById);
        javalin.put(PREFIX, this::create);
        javalin.patch("%s/id".formatted(PREFIX), this::update);
        javalin.delete("%s/id".formatted(PREFIX), this::delete);
    }

    @OpenApi(
        summary = "Get all legal entities",
        operationId = "getAllLegalEntities",
        path = "/customers/legal-entities",
        methods = HttpMethod.GET,
        tags = {"LegalEntities"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LegalEntityView[].class)})
        }
    )
    private void getAll(Context ctx) {
        ctx.json(legalEntities().items().stream().map(o -> LegalEntityView.of(o)).toList());
    }

    @OpenApi(
        summary = "Get an entity by id",
        operationId = "getLegalEntityById",
        path = "/customers/legal-entities/:id",
        methods = HttpMethod.GET,
        tags = {"LegalEntities"},
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = LegalEntityView.class)})
        }
    )
    private void getById(Context ctx) {
        String id = ctx.pathParam("id");
        Provider.findById(legalEntities(), id).ifPresentOrElse(o -> ctx.json(LegalEntityView.of(o)), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "Create legal entity",
        operationId = "createLegalEntity",
        path = "/customers/legal-entities",
        methods = HttpMethod.POST,
        tags = {"LegalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = LegalEntityView.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400"),
            @OpenApiResponse(status = "404")
        }
    )
    private void create(Context ctx) {
        LegalEntityView updated = ctx.bodyAsClass(LegalEntityView.class);
        legalEntities().update(updated.update(new LegalEntity(HasOid.UNDEFINED_OID)));
    }

    @OpenApi(
        summary = "Update a legal entity identified by ID",
        operationId = "updateLegalEntityById",
        path = "/customers/legal-entities/:id",
        methods = HttpMethod.PATCH,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"LegalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = LegalEntityView.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400"),
            @OpenApiResponse(status = "404")
        }
    )
    private void update(Context ctx) {
        String id = ctx.pathParam("id");
        LegalEntityView updated = ctx.bodyAsClass(LegalEntityView.class);
        Provider.findById(legalEntities(), id).ifPresentOrElse(o -> legalEntities().update(updated.update(o)), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "delete a legal entity by ID",
        operationId = "deleteLegalEntityById",
        path = "/customers/legal-entities/:id",
        methods = HttpMethod.DELETE,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"LegalEntities"},
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400"),
            @OpenApiResponse(status = "404")
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
