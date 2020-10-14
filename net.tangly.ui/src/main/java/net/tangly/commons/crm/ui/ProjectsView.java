package net.tangly.commons.crm.ui;

import java.util.List;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.products.Product;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.vaadin.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;

public class ProjectsView extends InternalEntitiesView<Product> {
    public ProjectsView(@NotNull List<Product> items, @NotNull Mode mode, Provider<Product> provider, TagTypeRegistry registry) {
        super(Product.class, mode, provider, registry);
        initialize();
    }

    @Override
    protected Product create() {
        return new Product();
    }
}
