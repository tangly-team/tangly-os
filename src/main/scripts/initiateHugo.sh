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

prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName
workshopsDir=$prjDir/src/site/workshops


siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName
$siteWorkshopsDir=$siteDir/static/

cd $siteRootDir
rm -rf $siteName
hugo new site $siteName
cd $siteDir

# download docsy theme and the submodules they depend on using npm
cd themes
git clone --branch v0.9.1 https://github.com/google/docsy.git
cd docsy
npm install

# install postcss (https://postcss.org/)) for final generation of site
cd $siteDir
npm install -D autoprefixer
npm install -D postcss
npm install -D postcss-cli

# setup workshops creation and install needed ruby package for asciidoc revealjs
cd $workshopsDir
bundle config --local path .bundle/gems
bundle

# beware that the content uses rouge, bibliography, plantuml, and mermaid to generate highlighted code, references and diagrams
echo "start server with hugo server --destination public --disableFastRender -> site is accessible under localhost:1313 -F"
