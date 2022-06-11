/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ports;

import net.tangly.core.crm.LegalEntity;
import net.tangly.core.crm.NaturalEntity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Note that JAXB annotations like @XmlRootElement are required only if XML support is needed in addition to JSON. JSON is supported through the Jackson
 * library.
 * <p>The PUT, GET, POST and DELETE methods are typical used in REST based architectures.
 * The following table gives an explanation of these operations.</p>
 * <dl>
 * <dt>GET</dt><dd>defines a reading access of the resource without side-effects. The resource is never changed via a GET request, e.g., the request has no
 * side effects (idempotent).</dd>
 * <dt>PUT</dt><dd> creates a new resource. It must also be idempotent.</dd>
 * <dt>DELETE</dt><dd> removes the resources. The operations are idempotent. They can get repeated without leading to different results.</dd>
 * <dt>POST</dt><dd> updates an existing resource or creates a new resource.</dd>
 * </dl>
 */
@Path("/crm")
public class CrmRest {
    @GET()
    @Path("/legalentities")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LegalEntity> legalEntities() {
        return Collections.emptyList();
    }

    @Path("/legalentities/{id}")
    public LegalEntity legalEntity(@PathParam("id") String id) {
        return null;
    }

    @GET()
    @Path("/naturalentities")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NaturalEntity> naturalEntities() {
        return Collections.emptyList();
    }

    @Path("/naturalentities/{id}")
    public NaturalEntity naturalEntity(@PathParam("id") String id) {
        return null;
    }
}
