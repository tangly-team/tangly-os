package net.tangly.commons.crm.ui;

import java.util.List;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.products.Project;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.vaadin.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;

public class ProjectsView extends InternalEntitiesView<Project> {
    public ProjectsView(@NotNull List<Project> items, Provider<Project> provider, TagTypeRegistry registry) {
        super(Project.class, InternalEntitiesView::defineGrid, provider, registry);
    }

    @Override
    protected Project create() {
        return new Project();
    }
}
