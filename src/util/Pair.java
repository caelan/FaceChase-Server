package util;

/**
 * A Pair is a grouping of two Objects.
 */
public class Pair<U, V> {
    
    private final U first;
    private final V second;
    
    /**
     * Creates a Pair with first object
     * U and second object V.
     * @param first The pair's first object.
     * @param second The pair's second object.
     */
    public Pair(U f, V s) {
        first = f;
        second = s;
    }
    
    /**
     * Package-protected access to a Pair's first object.
     * @return The pair's first object.
     */
    public U getFirst() {
        return first;
    }
    
    /**
     * Package-protected access a Pair's second object.
     * @return The pair's second object.
     */
    public V getSecond() {
        return second;
    }
}
