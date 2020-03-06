package net.tangly.commons.lang;

/**
 * Defines an interval of values of type providing a simple ordering function. The class is immutable.
 *
 * @param <T> type of the interval, shall provide an ordering function
 */
public record Interval<T extends Number>(T lower, T upper) {
}
