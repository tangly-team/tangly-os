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
import net.tangly.core.Address;
import net.tangly.core.EmailAddress;
import net.tangly.core.HasOid;
import net.tangly.core.PhoneNr;
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.domain.GenderCode;
import net.tangly.erp.crm.domain.NaturalEntity;
import net.tangly.erp.crm.domain.VcardType;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.jetbrains.annotations.NotNull;

public class NaturalEntitiesRest {
    record NaturalEntityView(String id, String name, String firstname, GenderCode gender, String text, String homeEmail, String phoneHome, String phoneMobile, Address address) {
        public static NaturalEntityView of(@NotNull NaturalEntity entity) {
            return new NaturalEntityView(entity.id(), entity.name(), entity.firstname(), entity.gender(), entity.text(),
                entity.email(VcardType.home).map(EmailAddress::text).orElse(null),
                entity.phoneHome().map(PhoneNr::number).orElse(null), entity.phoneMobile().map(PhoneNr::number).orElse(null), entity.address().orElse(null));
        }

        public NaturalEntity update(@NotNull NaturalEntity entity) {
            entity.name(name());
            entity.firstname(firstname());
            entity.gender(gender());
            entity.text(text());
            entity.email(VcardType.home, homeEmail());
            entity.phoneNr(VcardType.home, phoneHome());
            entity.phoneNr(VcardType.mobile, phoneMobile());
            entity.address(VcardType.home, address());
            return entity;
        }
    }

    public static final String PREFIX = STR."/rest/\{CrmBoundedDomain.DOMAIN.toLowerCase()}/natural-entities";
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
        path = "/natural-entities",
        methods = HttpMethod.GET,
        tags = {"NaturalEntities"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = NaturalEntityView[].class)})
        }
    )
    private void getAll(Context ctx) {
        ctx.json(naturalEntities().items().stream().map(o -> NaturalEntityView.of(o)).toList());
    }

    @OpenApi(
        summary = "Get a natural entity by his or her private email",
        operationId = "getNaturalEntityByEmail",
        path = "/natural-entities/:email",
        methods = HttpMethod.GET,
        tags = {"NaturalEntities"},
        pathParams = {
            @OpenApiParam(name = "email", required = true, type = String.class, description = "The private email address")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = NaturalEntityView.class)})
        }
    )
    private void getById(Context ctx) {
        String email = ctx.pathParam("email");
        naturalEntities().findBy(NaturalEntity::privateEmail, email).ifPresentOrElse(o -> ctx.json(NaturalEntityView.of(o)), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "Create a natural entity",
        operationId = "createNaturalEntity",
        path = "/natural-entities",
        methods = HttpMethod.POST,
        tags = {"NaturalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NaturalEntityView.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void create(Context ctx) {
        NaturalEntityView updated = ctx.bodyAsClass(NaturalEntityView.class);
        naturalEntities().update(updated.update(new NaturalEntity(HasOid.UNDEFINED_OID)));
    }

    @OpenApi(
        summary = "Update a natural entity identified by ID",
        operationId = "updateNaturalEntityById",
        path = "/natural-entities/:id",
        methods = HttpMethod.PATCH,
        pathParams = {
            @OpenApiParam(name = "id", required = true, type = String.class, description = "The entity identifier")
        },
        tags = {"NaturalEntities"},
        requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NaturalEntityView.class)}),
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void update(Context ctx) {
        String id = ctx.pathParam("id");
        NaturalEntityView updated = ctx.bodyAsClass(NaturalEntityView.class);
        Provider.findById(naturalEntities(), id).ifPresentOrElse(o -> naturalEntities().update(updated.update(o)), () -> ctx.status(404));
    }

    @OpenApi(
        summary = "delete a natural entity by email",
        operationId = "deletaNaturalEntityByEmail",
        path = "/natural-entities/:email",
        methods = HttpMethod.DELETE,
        pathParams = {
            @OpenApiParam(name = "email", required = true, type = String.class, description = "The entity private email address")
        },
        tags = {"NaturalEntities"},
        responses = {
            @OpenApiResponse(status = "204"),
            @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
            @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
        }
    )
    private void delete(Context ctx) {
        String email = ctx.pathParam("email");
        naturalEntities().findBy(NaturalEntity::privateEmail, email).ifPresentOrElse(o -> naturalEntities().delete(o), () -> ctx.status(404));
    }

    private Provider<NaturalEntity> naturalEntities() {
        return domain.realm().naturalEntities();
    }
}
