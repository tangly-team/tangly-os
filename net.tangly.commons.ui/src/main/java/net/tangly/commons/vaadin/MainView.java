/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.vaadin;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.Tag;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.commons.crm.ui.NaturalEntitiesView;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends VerticalLayout {
    public static NaturalEntity create(String number) {
        NaturalEntity entity = new NaturalEntity();
        entity.id("identifier-" + number);
        entity.name("name-" + number);
        entity.fromDate(LocalDate.of(2018, Month.JANUARY, 1));
        entity.toDate(LocalDate.of(2018, Month.DECEMBER, 31));
        entity.text("this is a text for entity description");
        entity.add(Tag.of("geo", "region", "CH"));
        entity.add(Comment.of("John Doe", "This is comment 1 written by John Doe for" + number));
        entity.add(Comment.of("John Doe", "This is comment 2 written by John Doe for " + number));
        entity.add(Comment.of("John Doe", "This is comment 3 written by John Doe  for" + number));
        return entity;
    }

    public MainView() {
        List<NaturalEntity> entities = new ArrayList<>(Arrays.asList(create("001"), create("002"), create("003")));
        TagTypeRegistry registry = new TagTypeRegistry();
        CrmTags.registerTags(registry);

        NaturalEntitiesView crud = new NaturalEntitiesView(entities, DataProvider.ofCollection(entities), registry);
        setSizeFull();
        add(menuBar(), crud);
    }

    private MenuBar menuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);

        MenuItem crm = menuBar.addItem("CRM");
        SubMenu crmSubMenu = crm.getSubMenu();
        MenuItem legalEntities = crmSubMenu.addItem("Legal Entities");
        MenuItem naturalEntities = crmSubMenu.addItem("Natural Entities");
        MenuItem contracts = crmSubMenu.addItem("Contracts");

        MenuItem products = menuBar.addItem("Products");
        SubMenu productdSubMenu = crm.getSubMenu();
        MenuItem projects = crmSubMenu.addItem("Projects");

        MenuItem ledger = menuBar.addItem("Ledger");
        SubMenu ledgerSubMenu = ledger.getSubMenu();
        return menuBar;
    }
}
