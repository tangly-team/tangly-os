package net.tangly.ui.components;

import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Define a canonical filter for entities. The filter provides access to the internal identifier, external identifier, name, text and time interval fields.
 * <p>Subclass the filter to add additional criteria specific to your entity type to the filter.</p>
 *
 * @param <T> Type of the entity which instances shall be filtered
 */
public class EntityFilter<T extends HasOid & HasId & HasName & HasText & HasTimeInterval> extends ItemView.ItemFilter<T> {
    private Long oid;
    private String id;
    private String name;
    private String text;
    private HasTimeInterval.IntervalFilter<T> fromInterval;
    private HasTimeInterval.IntervalFilter<T> toInterval;

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

    public void fromInterval(LocalDate start, LocalDate end) {
        // TODO Debug Component
        this.fromInterval = new HasTimeInterval.IntervalFilter<>(start, end);
        //      refresh();
    }

    public void toInterval(LocalDate start, LocalDate end) {
        // TODO Debug Component
        this.toInterval = new HasTimeInterval.IntervalFilter<>(start, end);
        //      refresh();
    }

    @Override
    public boolean test(@NotNull T entity) {
        return matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text);
        //        && (Objects.isNull(fromInterval) || fromInterval.test(entity)) &&
        //            (Objects.isNull(toInterval) || toInterval.test(entity));
    }
}
