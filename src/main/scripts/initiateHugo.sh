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

# download docsy theme and the submodules they depend on using npm
cd themes
git clone --branch v0.5.1 https://github.com/google/docsy.git
cd docsy
npm install

# install postcss (https://postcss.org/)) for final generation of site
cd $siteDir
sudo npm install -D autoprefixer
sudo npm install -D postcss-cli
sudo npm install -D postcss

# initiate empty public folder with git to enable optional completion step and publishing in our pages repository (GitHub, GitLab, orBitBucket)
git clone --no-checkout git@github.com:tangly-team/tangly-team.github.io.git public

echo "start server with hugo server --destination public --disableFastRender -> site is accessible under localhost:1313"
