package net.tangly.commons.vaadin;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.Entity;
import net.tangly.bus.core.EntityImp;
import net.tangly.bus.core.Tag;

public class EntityForm implements CrudForm<Entity>, CrudActionsListener<Entity> {
    private Binder<Entity> binder;
    private List<Entity> entities = new ArrayList<>(Arrays.asList(create("001"), create("002"), create("003")));

    private static Entity create(String number) {
        Entity entity = new EntityImp() {
        };
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

    public static void defineGrid(Grid<Entity> grid) {
        grid.setVerticalScrollingEnabled(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Entity::oid).setKey("oid").setHeader("Oid").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
                .setFrozen(true);
        grid.addColumn(Entity::id).setKey("id").setHeader("Id").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::fromDate).setKey("from").setHeader("From").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Entity::toDate).setKey("to").setHeader("to").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
    }

    public DataProvider<Entity, ?> dataProvider() {
        return new ListDataProvider<>(entities);
    }

    @Override
    public FormLayout createForm(Operation operation, Entity entity) {
        TextField oid = new TextField("Oid");
        oid.setPlaceholder("oid");
        oid.setEnabled(Operation.isReadWrite(operation));

        TextField id = new TextField("Id");
        id.setPlaceholder("id");
        id.setEnabled(Operation.isReadWrite(operation));

        TextField name = new TextField("Name");
        name.setPlaceholder("name");
        name.setEnabled(Operation.isReadWrite(operation));

        DatePicker fromDate = new DatePicker("From Date");
        fromDate.setEnabled(Operation.isReadWrite(operation));

        DatePicker toDate = new DatePicker("To Date");
        toDate.setEnabled(Operation.isReadWrite(operation));

        HtmlComponent br = new HtmlComponent("br");
        TextArea text = new TextArea("Text");
        text.setHeight("8em");
        text.getStyle().set("colspan", "2");
        text.setEnabled(Operation.isReadWrite(operation));

        binder = new Binder<>(Entity.class);
        binder.bind(oid, Entity::id, null);
        binder.bind(id, Entity::id, null);
        binder.bind(fromDate, Entity::fromDate, null);
        binder.bind(toDate, Entity::toDate, null);
        binder.bind(text, Entity::text, null);

        FormLayout form = new FormLayout(oid, id, name, fromDate, toDate, br, text);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("21em", 2),
                new FormLayout.ResponsiveStep("42em", 3));
        return form;
    }

    @Override
    public Entity formCompleted(Operation operation, Entity entity) {
        switch (operation) {
            case UPDATE:
                try {
                    binder.writeBean(entity);
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
                break;
            case CREATE:
                try {
                    Entity created = new EntityImp() {
                    };
                    binder.writeBean(created);
                    return created;
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
                break;
        }
        return entity;
    }

    @Override
    public void entityAdded(DataProvider<Entity, ?> dataProvider, Entity entity) {
        ListDataProvider<Entity> dp = (ListDataProvider<Entity>) dataProvider;
        entities.add(entity);

    }

    @Override
    public void entityDeleted(DataProvider<Entity, ?> dataProvider, Entity entity) {
        ListDataProvider<Entity> dp = (ListDataProvider<Entity>) dataProvider;
        entities.remove(entity);
    }
}
