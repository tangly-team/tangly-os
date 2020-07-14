package net.tangly.commons.crm.ui;

import java.util.List;

import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.products.Project;
import net.tangly.commons.vaadin.EntitiesView;
import org.jetbrains.annotations.NotNull;

public class ProjectsView extends EntitiesView<Project> {
    public ProjectsView(@NotNull List<Project> items, DataProvider<Project, ?> dataProvider, TagTypeRegistry registry) {
        super(Project.class, EntitiesView::defineGrid, items, dataProvider, registry);
    }

    @Override
    protected Project create() {
        return new Project();
    }
}
