#
# Copyright 2006-2023 Marcel Baumann
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
# the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
# OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
#
#

prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName
websiteDir=$prjDir/src/site/website

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

# copy the documentation of project module into the folders used by Hugo and the theme
function copy_module() {
  cp $prjDir/net.tangly.$1/readme.* $siteDir/content/docs/$1/
  cp -R $prjDir/net.tangly.$1/src/site/* $siteDir/content/docs/$1/
  cp -R $prjDir/net.tangly.$1/build/docs/javadoc/* $siteDir/static/docs/$1/api-$1
}

# copy the documentation of bounded domain project module into the folders used by Hugo and the theme
function copy_domain_module() {
  cp $prjDir/net.tangly.erp.$1/readme.* $siteDir/content/docs/domains/$1/
  cp -R $prjDir/net.tangly.erp.$1/src/site/* $siteDir/content/docs/domains/$1/
  cp -R $prjDir/net.tangly.erp.$1/build/docs/javadoc/* $siteDir/static/docs/domains/$1/api-$1
}

# copy whole website to static site structure and tailoring of docsy template
cp -R $websiteDir/assets $siteDir/
cp -R $websiteDir/content $siteDir/
cp -R $websiteDir/layouts $siteDir/
cp -R $websiteDir/static $siteDir/
cp -R $websiteDir/config.toml $siteDir/

# you need to run gradle build on the whole project to generate the javadoc for modules. The directories containing javadoc seems to need unique names due to
# hugo strange behavior.

copy_module bdd
cp $prjDir/net.tangly.bdd/build/bdd-reports/bdd-report.adoc $siteDir/content/docs/bdd/

copy_module commons
copy_module core
copy_module dev
copy_module fsm
cp $prjDir/net.tangly.fsm/src/test/resources/files/* $siteDir/content/docs/fsm/examples/

copy_module gleam
copy_module ui

copy_domain_module agile
copy_domain_module apps
copy_domain_module collaborators
copy_domain_module crm
copy_domain_module invoices
copy_domain_module ledger
copy_domain_module products
copy_domain_module shared
copy_domain_module ui

cd $siteDir
rm -v **/.DS_Store
