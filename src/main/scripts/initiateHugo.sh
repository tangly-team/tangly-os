baseDir=/Users/Shared/Projects
tanglyDir=/Users/Shared/Projects/tangly-os
siteName=tangly-os-site
siteDir=/Users/Shared/Projects/tangly-os-site

cd $baseDir
hugo new site $siteName
cd $siteName
cd themes
git clone https://github.com/thingsym/hugo-theme-techdoc.git

cp -R $tanglyDir/src/site/website/ $siteDir/
cp $tanglyDir/net.tangly.fsm/README.* $siteDir/content/fsm/
cp $tanglyDir/net.tangly.fsm/src/site/* $siteDir/content/fsm/
cp $tanglyDir/net.tangly.fsm/src/site/ADR/* $siteDir/content/fsm/adr
echo "start server with hugo server -D"
