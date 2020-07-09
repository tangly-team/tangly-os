/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

// import Vaadin client-router to handle client-side and server-side navigation
import {Router} from '@vaadin/router';

// import Flow module to enable navigation to Vaadin server-side views
import {Flow} from '../../vaadin-16/target/flow-frontend/Flow';

const {serverSideRoutes} = new Flow({
  imports: () => import('../../../vaadin-16/target/frontend/generated-flow-imports')
});

const routes = [
  // for client-side, place routes below (more info https://vaadin.com/docs/v15/flow/typescript/creating-routes.html)

  // for server-side, the next magic line sends all unmatched routes:
  ...serverSideRoutes // IMPORTANT: this must be the last entry in the array
];

// Vaadin router needs an outlet in the index.html page to display views
const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
