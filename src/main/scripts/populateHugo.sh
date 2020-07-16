prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

function copy_module() {
  cp $prjDir/net.tangly.$1/readme.* $siteDir/content/$1/
  cp -R $prjDir/net.tangly.$1/src/site/* $siteDir/content/$1/
  cp -R $prjDir/net.tangly.$1/build/docs/javadoc/* $siteDir/static/$1/api-$1
}

cp -R $prjDir/src/site/website/ $siteDir/

# you need to run gradle build on the whole project to generate the javadoc for modules. The directories containing javadoc seems to need unique
# names due to hugo strange behavior.

copy_module bdd
cp $prjDir/net.tangly.bdd/out/bdd-reports/bdd-report.adoc $siteDir/content/bdd/

copy_module bus
copy_module commons
copy_module fsm
copy_module gleam
copy_module orm
copy_module ports
