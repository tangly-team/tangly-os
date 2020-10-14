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
