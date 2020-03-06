cd $siteDir
git clone --no-checkout https://marcel-baumann@bitbucket.org/tangly-team/tangly-team.bitbucket.io.git public
hugo

cd public
git add --all
git commit -m "new release of the tangly-team web site"
git push -f origin master
