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
import net.tangly.erp.crm.domain.Lead;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.jetbrains.annotations.NotNull;

public class LeadsRest {
    public static final String PREFIX = STR."/rest/\{CrmBoundedDomain.DOMAIN.toLowerCase()}/leads";

    private final CrmBoundedDomain domain;

    LeadsRest(CrmBoundedDomain domain) {
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
        summary = "Get all leads",
        operationId = "getAllLeads",
        path = "/leads",
        methods = HttpMethod.GET,
        tags = {"Leads"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Lead[].class)})
        }
    )
    private void getAll(Context ctx) {
        ctx.json(leads().items());
    }

    @OpenApi(
        summary = "Get an entity by id",
        operationId = "getLeadById",
        path = "/leads/:id",
        methods = HttpMethod.GET,
        tags = {"Leads"},
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Lead.class)})
        }
    )
    private void getById(Context ctx) {
        String id = ctx.pathParam("id");
        leads().findBy(Lead::linkedIn, id).ifPresentOrElse(entity -> ctx.json(entity), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "Create ab lead",
        operationId = "createLead",
        path = "/leads",
        methods = HttpMethod.POST,
        tags = {"Leads"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = Lead.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void create(Context ctx) {
        Lead updated = ctx.bodyAsClass(Lead.class);
        leads().update(updated);
    }

    @OpenApi(
        summary = "Update a lead identified by ID",
        operationId = "updateLeadById",
        path = "/leads/:id",
        methods = HttpMethod.PATCH,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"Leads"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = Lead.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void update(Context ctx) {
        String id = ctx.pathParam("id");
        Lead updated = ctx.bodyAsClass(Lead.class);
        leads().findBy(Lead::linkedIn, id).ifPresentOrElse(entity -> leads().update(updated), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "delete a lead by ID",
        operationId = "deleteLeadById",
        path = "/leads/:id",
        methods = HttpMethod.DELETE,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"Leads"},
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void delete(Context ctx) {
        String id = ctx.pathParam("id");
        leads().findBy(Lead::linkedIn, id).ifPresentOrElse(entity -> leads().delete(entity), () -> ctx.status(404));
    }

    private Provider<Lead> leads() {
        return domain.realm().leads();
    }
}
