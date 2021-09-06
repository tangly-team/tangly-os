#
# Copyright 2006-2021 Marcel Baumann
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
# the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
# OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
#

prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

# using the docsy theme means that
# docs is for pages in your site’s Documentation section
# blog is for pages in your site’s Blog
# community is for your site’s Community page

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

# copy whole website to static site structure
cp -R $prjDir/src/site/website/static $siteDir/
cp -R $prjDir/src/site/website/content $siteDir/
cp -R $prjDir/src/site/website/config.toml $siteDir/

# copy site icon to requested location
cp $prjDir/src/site/website/logo.svg $siteDir/assets/icons/

# you need to run gradle build on the whole project to generate the javadoc for modules. The directories containing javadoc seems to need unique names due to
# hugo strange behavior.

copy_module bdd
cp $prjDir/net.tangly.bdd/out/bdd-reports/bdd-report.adoc $siteDir/content/docs/bdd/

copy_module commons
copy_module core
copy_module dev
copy_module fsm
copy_module gleam
copy_module ui

copy_domain_module crm
copy_domain_module invoices
copy_domain_module ledger
copy_domain_module products
copy_domain_module shared
