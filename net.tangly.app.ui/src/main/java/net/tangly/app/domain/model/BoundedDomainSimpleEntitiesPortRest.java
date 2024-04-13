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

package net.tangly.app.domain.model;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class BoundedDomainSimpleEntitiesPortRest {
    static final String PREFIX = STR."/rest/\{BoundedDomainSimpleEntities.DOMAIN}/";

    public void register(Javalin javalin) {
        javalin.get(STR."\{PREFIX}entitiesOne", BoundedDomainSimpleEntitiesPortRest::getAllEntitiesOne);
        javalin.get(STR."\{PREFIX}entitiesOne/{id}", BoundedDomainSimpleEntitiesPortRest::getAllEntitiesOne);
        javalin.put(STR."\{PREFIX}entitiesOne", BoundedDomainSimpleEntitiesPortRest::getAllEntitiesOne);
        javalin.delete(STR."\{PREFIX}entitiesOne/{id}", BoundedDomainSimpleEntitiesPortRest::getAllEntitiesOne);
    }

    private static void getAllEntitiesOne(Context ctx) {
    }

    private static void getEntityOneById(Context ctx) {
        ctx.pathParam("id");
    }

    private static void createEntityOne(Context ctx) {
    }

    private static void deleteEntityOne(Context ctx) {
        ctx.pathParam("id");
    }
}
