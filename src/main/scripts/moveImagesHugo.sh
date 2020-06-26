siteDir=/Users/Shared/Projects/tangly-os-site

# Copy the generated images of plantuml to the expected static directories as configured. Hugo and asciidoc processor have expectations where the
# images should be.

mv $siteDir/fsm-design-* $siteDir/static/fsm/design
mv $siteDir/fsm-userGuideFsm-* $siteDir/static/fsm/userguidefsm
mv $siteDir/bus-* $siteDir/static/bus/businessmodels
mv $sitedDir/orm-* $sitedDir/static/commons/orm

# move the generated images to static folder (hugo services images from this location)
mv $siteDir/agile-architecture-images/*.svg $siteDir/static/ideas/agilearchitecture/agile-architecture-images
mv $siteDir/architecture-code-scene-images/*.svg $siteDir/static/ideas/architecturecodescene/architecture-code-scene-images

# move the images to static folder (hugo services images from this location)
mv $siteDir/content/ideas/agile-architecture-images/* $siteDir/static/ideas/agilearchitecture/agile-architecture-images
mv $siteDir/content/ideas/architecture-code-scene-images/* $siteDir/static/ideas/architecturecodescene/architecture-code-scene-images
