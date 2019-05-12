package net.tangly.commons.lang;

public class Tuple<T, U> {
    T left;
    U right;

    Tuple(T left, U right) {
        this.left = left;
        this.right = right;
    }

    T left() {
        return left;
    }

    U right() {
        return right;
    }
}
