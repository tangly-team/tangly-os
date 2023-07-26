package net.tangly.ui.components;

import net.tangly.core.DateRange;
import net.tangly.core.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Define a canonical filter for entities. The filter provides access to the internal identifier, external identifier, name, text and time interval fields.
 * <p>Subclass the filter to add additional criteria specific to your entity type to the filter.</p>
 *
 * @param <T> Type of the entity which instances shall be filtered
 */
public class EntityFilter<T extends Entity> extends ItemView.ItemFilter<T> {

    private Long oid;
    private String id;
    private String name;
    private String text;
    private DateRange.DateFilter fromRange;
    private DateRange.DateFilter toRange;

    public EntityFilter() {
    }

    public void oid(Long oid) {
        this.oid = oid;
        refresh();
    }

    public void id(String id) {
        this.id = id;
        refresh();
    }

    public void name(String name) {
        this.name = name;
        refresh();
    }

    public void text(String text) {
        this.text = text;
        refresh();
    }

    public void fromRange(@NotNull DateRange range) {
        this.fromRange = new DateRange.DateFilter(range);
        refresh();
    }

    public void toRange(@NotNull DateRange range) {
        this.toRange = new DateRange.DateFilter(range);
        refresh();
    }

    @Override
    public boolean test(@NotNull T entity) {
        return matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text) && (Objects.isNull(fromRange) || fromRange.test(entity.from())) &&
            (Objects.isNull(toRange) || toRange.test(entity.to()));
    }
}
