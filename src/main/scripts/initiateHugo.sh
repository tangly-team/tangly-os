siteRootDir=/Users/Shared/Projects/
siteName=tangly-os-site
siteDir=$siteRootDir$siteName

cd $siteRootDir
rm -rf $siteName
hugo new site $siteName
cd $siteDir
cd themes
git clone https://github.com/thingsym/hugo-theme-techdoc.git

echo "start server with hugo server -D -> site is accessible under localhost:1313"
