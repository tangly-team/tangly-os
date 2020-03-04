siteDir=/Users/Shared/Projects/tangly-os-site

cp $siteDir/fsm-design-* $siteDir/static/fsm/design/
cp $siteDir/fsm-userGuideFsm-* $siteDir/static/fsm/userguidefsm/

cd $siteDir
git clone --no-checkout https://marcel-baumann@bitbucket.org/tangly-team/tangly-team.bitbucket.io.git public
asciidoctor

cd public
git add --all
git commit -m "new release of the tangly-team web site"
git push -f origin master
