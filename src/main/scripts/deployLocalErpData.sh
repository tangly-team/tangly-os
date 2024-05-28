#
# Copyright 2024-2024 Marcel Baumann
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
# the License at
#
#          https://apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
# OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
#
#

# deploy the archive of the ERP data to the local file system

sudo rm -R /var/tangly-erp/
sudo cp -R /Users/marcelbaumann/Library/CloudStorage/GoogleDrive-marcel.baumann@tangly.net/My\ Drive/tangly/10-Data/tangly-erp /var/
sudo chown -R marcelbaumann /var/tangly-erp/
sudo chmod -R 755 /var/tangly-erp/
