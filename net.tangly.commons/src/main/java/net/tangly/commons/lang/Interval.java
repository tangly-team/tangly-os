package net.tangly.commons.lang;

/**
 * Defines an interval of values of type providing a simple ordering function. The class is immutable.
 *
 * @param <T> type of the interval, shall provide an ordering function
 */
public class Interval<T extends Number> {
    private final T lower;
    private final T upper;

    public Interval(T lower, T upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public T lower() {
        return lower;
    }

    public T upper() {
        return upper;
    }
}
