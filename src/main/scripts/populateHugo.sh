prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

cp -R $prjDir/src/site/website/ $siteDir/

# you need to run gradle build on the whole project to generate the javadoc for modules. The directories containing javadoc seems to need unique
# names due to hugo strange behavior.

cp $prjDir/net.tangly.fsm/readme.* $siteDir/content/fsm/
cp -R $prjDir/net.tangly.fsm/src/site/* $siteDir/content/fsm/
cp -R $prjDir/net.tangly.fsm/build/docs/javadoc/* $siteDir/static/fsm/api-fsm

cp $prjDir/net.tangly.bdd/readme.* $siteDir/content/bdd/
cp -R $prjDir/net.tangly.bdd/src/site/* $siteDir/content/bdd/
# find out why buld is not working with gradle
cp $prjDir/net.tangly.bdd/out/bdd-reports/bdd-report.adoc $siteDir/content/bdd/
cp -R $prjDir/net.tangly.bdd/build/docs/javadoc/* $siteDir/static/bdd/api-bdd

cp $prjDir/net.tangly.bus/readme.* $siteDir/content/bus/
cp -R $prjDir/net.tangly.bus/src/site/* $siteDir/content/bus/
cp -R $prjDir/net.tangly.bus/build/docs/javadoc/* $siteDir/static/bus/api-bus

