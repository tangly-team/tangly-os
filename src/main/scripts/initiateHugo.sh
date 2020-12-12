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

mkdir assets
cd assets
mkdir icons
cd ..

# initiate public folder with git to enable optional completion step and publishing in bitbucket
git clone --no-checkout https://marcel-baumann@bitbucket.org/tangly-team/tangly-team.bitbucket.io.git public

cd themes

# download docsy themes and the submodules it depends on
git clone https://github.com/google/docsy.git
cd docsy
git submodule update --init --recursive

# create the layout for the ideas folder under content so that the files are processed
cp -R ./layouts/docs ./layouts/ideas

# improve copyright in the footer and taxonomy display
cp $prjDir/src/site/website/docsy/layouts/_default/* $siteDir/themes/docsy/layouts/_default
cp $prjDir/src/site/website/docsy/layouts/blog/* $siteDir/themes/docsy/layouts/blog
cp $prjDir/src/site/website/docsy/layouts/partials/* $siteDir/themes/docsy/layouts/partials

# install postcss for final generation of site
cd $siteDir
sudo npm install -D --save autoprefixer
sudo npm install -D --save postcss-cli
sudo npm install -D --save postcss

echo "start server with hugo server --destination public --disableFastRender -> site is accessible under localhost:1313"
