siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

cd $siteDir
git clone --no-checkout https://marcel-baumann@bitbucket.org/tangly-team/tangly-team.bitbucket.io.git public
hugo

cd public
git add --all
# commit will push all changes to the git repository and trigger an update of the website
git commit -m "new release of the tangly-team web site"
git push -f origin master
