#
# Copyright 2006-2024 Marcel Baumann
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
# the License at
#
#          https://apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
# OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
#
#

siteRootDir=/Users/Shared/Projects/tangly-os

function upload() {
  cd $1
  gradle publishMavenJavaPublicationToMavenRepository -Pvaadin.productionMode
  cd ..
}

# Uploads need to be sequential to avoid spurious errors on sonatype staging area. It seems it crashes the open parallel connections.
cd $siteRootDir
upload ./net.tangly.apps
upload ./net.tangly.bdd
upload ./net.tangly.cmd
upload ./net.tangly.commons
upload ./net.tangly.core
upload ./net.tangly.dev
upload ./net.tangly.fsm
upload ./net.tangly.gleam
upload ./net.tangly.ui

upload ./net.tangly.erp.agile
upload ./net.tangly.erp.collaborators
upload ./net.tangly.erp.crm
upload ./net.tangly.erp.invoices
upload ./net.tangly.erp.ledger
upload ./net.tangly.erp.products
upload ./net.tangly.erp.ui
