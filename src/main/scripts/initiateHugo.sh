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


cd $siteRootDir
rm -rf $siteName
hugo new site $siteName
cd $siteDir

mkdir assets
cd assets
mkdir icons
cd ..

# initiate public folder with git to enable optional completion step and publishing in bitbucket
git clone --no-checkout https://marcel-baumann@bitbucket.org/tangly-team/tangly-team.bitbucket.io.git public

cd themes

# download docsy themes and the submodules it depends on
git clone https://github.com/google/docsy.git
cd docsy
git submodule update --init --recursive

# create the layout for the ideas folder under content so that the files are processed as a clone of docs folder
cp -R ./layouts/docs ./layouts/ideas
cp -R ./layouts/docs ./layouts/expertise

# improve copyright in the footer and comments in blogs
cp $prjDir/src/site/website/docsy/layouts/blog/* $siteDir/themes/docsy/layouts/blog
cp $prjDir/src/site/website/docsy/layouts/partials/* $siteDir/themes/docsy/layouts/partials
cp $prjDir/src/site/website/docsy/assets/scss/* $siteDir/themes/docsy/assets/scss

cp $prjDir/src/site/website/docsy/layouts/shortcodes/* $siteDir/themes/docsy/layouts/shortcodes

# install postcss (https://postcss.org/)) for final generation of site
cd $siteDir
sudo npm install -D autoprefixer
sudo npm install -D postcss-cli
sudo npm install -D postcss

echo "start server with hugo server --destination public --disableFastRender -> site is accessible under localhost:1313"
