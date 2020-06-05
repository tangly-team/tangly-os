package net.tangly.commons.vaadin;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import net.tangly.bus.core.Entity;

@Theme(value = Material.class)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@Route("")
public class MainView extends VerticalLayout {
    public MainView() {
        EntityForm form = new net.tangly.commons.vaadin.EntityForm();
        Crud<Entity> crud = new Crud<>(Entity.class, false, EntityForm::defineGrid, form.dataProvider(), form, form);
        setSizeFull();
        add(crud);
    }
}
