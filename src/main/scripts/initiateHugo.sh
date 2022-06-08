#
# Copyright 2006-2022 Marcel Baumann
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

# initiate public folder with git to enable optional completion step and publishing in bitbucket
git clone --no-checkout git@bitbucket.org:tangly-team/tangly-team.bitbucket.io.git public

# download docsy theme and the submodules they depend on
cd themes
git clone https://github.com/google/docsy.git
cd docsy
git submodule update --init --recursive

# install postcss (https://postcss.org/)) for final generation of site
cd $siteDir
sudo npm install -D autoprefixer
sudo npm install -D postcss-cli
sudo npm install -D postcss

echo "start server with hugo server --destination public --disableFastRender -> site is accessible under localhost:1313"
