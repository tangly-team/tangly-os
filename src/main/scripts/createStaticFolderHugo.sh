#
# Copyright 202-2024 Marcel Baumann
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

# The script creates the static folders used by Hugo to generate the static website.
# This is necessary because git prune all empty directories.

prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName
websiteStaticDir=$prjDir/src/site/website/static

mkdir -p $websiteStaticDir/docs/PA/api-apps
mkdir -p $websiteStaticDir/docs/bdd/api-bdd
mkdir -p $websiteStaticDir/docs/XMS/api-cmd
mkdir -p $websiteStaticDir/docs/commons/api-commons
mkdir -p $websiteStaticDir/docs/core/api-core
mkdir -p $websiteStaticDir/docs/dev/api-dev
mkdir -p $websiteStaticDir/docs/fsm/api-fsm
mkdir -p $websiteStaticDir/docs/gleam/api-gleam
mkdir -p $websiteStaticDir/docs/ui/api-ui
mkdir -p $websiteStaticDir/docs/ui/api-web

mkdir -p $websiteStaticDir/domains/agile/api-agile
mkdir -p $websiteStaticDir/domains/collaborators/api-collaborators
mkdir -p $websiteStaticDir/domains/crm/api-crm
mkdir -p $websiteStaticDir/domains/invoices/api-invoices
mkdir -p $websiteStaticDir/domains/ledger/api-ledger
mkdir -p $websiteStaticDir/domains/products/api-products
mkdir -p $websiteStaticDir/domains/ui/api-ui

mkdir -p $websiteStaticDir/ideas/agilearchitecture/agile-architecture-scene-images
mkdir -p $websiteStaticDir/ideas/architecturecodescene/architecture-code-scene-images
