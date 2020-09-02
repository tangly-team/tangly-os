package net.tangly.commons.crm.ui;

import java.util.List;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.products.Project;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.vaadin.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;

public class ProjectsView extends InternalEntitiesView<Project> {
    public ProjectsView(@NotNull List<Project> items, @NotNull Mode mode, Provider<Project> provider, TagTypeRegistry registry) {
        super(Project.class, mode, provider, registry);
        initialize();
    }

    @Override
    protected Project create() {
        return new Project();
    }
}
