#
# Copyright 2006-2021 Marcel Baumann
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
#  a copy of the License at
#
#          http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
#  under the License.
#

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

cd $siteDir

# guaranty the population of the public folder containing the site to be published
echo Let the server time to build the lunr search index files before stopping the hugo server and publishing the site
hugo --destination public

cd public
git add --all
# commit will push all changes to the git repository and trigger an update of the website
git commit -m "new release of the tangly-team web site"
git push -f origin master
