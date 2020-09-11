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

echo "start server with hugo server --destination public --disableFastRender -> site is accessible under localhost:1313"
