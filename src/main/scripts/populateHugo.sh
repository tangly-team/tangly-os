prjRootDir=/Users/Shared/Projects/
prjName=tangly-os
prjDir=$prjRootDir$prjName

siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

# using the docsy theme means that
# docs is for pages in your site’s Documentation section
# blog is for pages in your site’s Blog
# community is for your site’s Community page

# copy the documentation of project module into the folders used by Hugo and the theme
function copy_module() {
  # content folder contains all the site dynamic content such as markdown and asciidoc documents
  cp $prjDir/net.tangly.$1/readme.* $siteDir/content/docs/$1/
  cp -R $prjDir/net.tangly.$1/src/site/* $siteDir/content/docs/$1/
  # static folder contains all the site static content such as JavaDoc html for components
  cp -R $prjDir/net.tangly.$1/build/docs/javadoc/* $siteDir/static/docs/$1/api-$1
}

# copy whole website to static site structure
cp -R $prjDir/src/site/website/ $siteDir/

# copy site icon to requested location
cp $prjDir/src/site/website/logo.svg $siteDir/assets/icons/

# you need to run gradle build on the whole project to generate the javadoc for modules. The directories containing javadoc seems to need unique names due to
# hugo strange behavior.

copy_module bdd
cp $prjDir/net.tangly.bdd/out/bdd-reports/bdd-report.adoc $siteDir/content/docs/bdd/

copy_module bus
copy_module commons
copy_module core
copy_module fsm
copy_module gleam
copy_module ports
copy_module ui

