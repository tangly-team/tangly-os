package net.tangly.ui.components;

import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

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
    private HasTimeInterval.IntervalFilter<T> intervalFilter;

    public EntityFilter() {
    }

    public void oid(Long oid) {
        this.oid = oid;
        dataView().refreshAll();
    }

    public void id(String id) {
        this.id = id;
        dataView().refreshAll();
    }

    public void name(String name) {
        this.name = name;
        dataView().refreshAll();
    }

    public void text(String text) {
        this.text = text;
        dataView().refreshAll();
    }

    public void from(LocalDate from) {
        this.intervalFilter = new HasTimeInterval.IntervalFilter<>(from, intervalFilter.to());
        dataView().refreshAll();
    }

    public void to(LocalDate to) {
        this.intervalFilter = new HasTimeInterval.IntervalFilter<>(intervalFilter.from(), to);
        dataView().refreshAll();
    }

    @Override
    public boolean test(@NotNull T entity) {
        return matches(entity.id(), id) && matches(entity.name(), name) && matches(entity.text(), text) && (Objects.isNull(intervalFilter) || intervalFilter.test(entity));
    }
}
