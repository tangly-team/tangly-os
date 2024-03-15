#
# Copyright 2006-2024 Marcel Baumann
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

# This script copies all the files from the project documentation into the Hugo site structure.
# The static website can be generated without accessing any resources onsite the Hugo site folder.

prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName
workshopsDir=$prjDir/src/site/workshops
websiteDir=$prjDir/src/site/website

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName
$siteWorkshopsDir=$siteDir/static/

# copy the documentation of project module into the folders used by Hugo and the theme
function copy_module() {
  mkdir -p $siteDir/static/docs/$1/api-$1
  cp $prjDir/net.tangly.$1/readme.* $siteDir/content/docs/$1/
  cp -R $prjDir/net.tangly.$1/src/site/* $siteDir/content/docs/$1/
  cp -R $prjDir/net.tangly.$1/build/docs/javadoc/* $siteDir/static/docs/$1/api-$1
}

# copy the documentation of bounded domain project module into the folders used by Hugo and the theme
function copy_domain_module() {
  mkdir -p $siteDir/static/docs/domains/$1/api-$1
  cp $prjDir/net.tangly.erp.$1/readme.* $siteDir/content/docs/domains/$1/
  cp -R $prjDir/net.tangly.erp.$1/src/site/* $siteDir/content/docs/domains/$1/
  cp -R $prjDir/net.tangly.erp.$1/build/docs/javadoc/* $siteDir/static/docs/domains/$1/api-$1
}

revealjsDir=https://cdn.jsdelivr.net/npm/reveal.js@4.5.0
siteWorkshopsDir=$siteDir/static/ideas/learnings/workshops/

# copy the pictures and the revealjs slides into the workshops folder in the static part of the site
function copy_workshop () {
    mkdir -p $siteWorkshopsDir/$1/pics
    cp -R $workshopsDir/$1/pics/* $siteWorkshopsDir/$1/pics
    pushd $workshopsDir
    bundle exec asciidoctor-revealjs -r asciidoctor-diagram -r asciidoctor-kroki -a revealjsdir=$revealjsDir $1/$1.adoc
    popd
    mv $workshopsDir/$1/$1.html  $siteWorkshopsDir/$1/
}

# clean-up the directories before copying new data
rm -rf $siteDir/static/*
rm -rf $siteDir/content/*
rm -rf $siteDir/public
mkdir $siteDir/public

# copy whole website to static site structure and tailoring of docsy template
cp -R $websiteDir/assets $siteDir/
cp -R $websiteDir/content $siteDir/
cp -R $websiteDir/layouts $siteDir/
cp -R $websiteDir/static $siteDir/
cp -R $websiteDir/hugo.toml $siteDir/
# copy the references.bib file to the data folder
cp $prjDir/src/main/resources/references.bib $siteDir/data/

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

cp -R $prjDir/net.tangly.erp.shared/src/site/_design $siteDir/content/docs/domains/architecture

copy_workshop agile-scrum
copy_workshop agile-technical-fluency
copy_workshop arc42
copy_workshop c4-uml
copy_workshop ddd

cd $siteDir
rm -v **/.DS_Store
