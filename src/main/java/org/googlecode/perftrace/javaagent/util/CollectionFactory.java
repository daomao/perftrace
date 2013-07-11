package org.googlecode.perftrace.javaagent.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
 
/**
 * Static factory methods to ease the creation of new collection types (when using generics). Most of these method
 * leverage the compiler's ability to match generic types by return value. Typical usage (with a static import):
 * <p/>
 * <pre>
 * Map<Foo, Bar> map = newMap();
 * </pre>
 * <p/>
 * <p/>
 * This is a replacement for:
 * <p/>
 * <pre>
 * Map<Foo, Bar> map = new HashMap<Foo, Bar>();
 * </pre>
 */
public final class CollectionFactory
{
    /**
     * Constructs and returns a generic {@link HashMap} instance.
     */
    public static <K, V> Map<K, V> newMap()
    {
        return new HashMap<K, V>();
    }
 
    /**
     * Constructs and returns a generic {@link java.util.HashSet} instance.
     */
    public static <T> Set<T> newSet()
    {
        return new HashSet<T>();
    }
 
    /**
     * Contructs a new {@link HashSet} and initializes it using the provided collection.
     */
    public static <T, V extends T> Set<T> newSet(Collection<V> values)
    {
        return new HashSet<T>(values);
    }
 
    public static <T, V extends T> Set<T> newSet(V... values)
    {
        // Was a call to newSet(), but Sun JDK can't handle that. Fucking generics.
        return new HashSet<T>(Arrays.asList(values));
    }
 
    /**
     * Constructs a new {@link java.util.HashMap} instance by copying an existing Map instance.
     */
    public static <K, V> Map<K, V> newMap(Map<? extends K, ? extends V> map)
    {
        return new HashMap<K, V>(map);
    }
 
    /**
     * Constructs a new concurrent map, which is safe to access via multiple threads.
     */
    public static <K, V> ConcurrentMap<K, V> newConcurrentMap()
    {
        return new ConcurrentHashMap<K, V>();
    }
 
    /**
     * Contructs and returns a new generic {@link java.util.ArrayList} instance.
     */
    public static <T> List<T> newList()
    {
        return new ArrayList<T>();
    }
 
    /**
     * Creates a new, fully modifiable list from an initial set of elements.
     */
    public static <T, V extends T> List<T> newList(V... elements)
    {
        // Was call to newList(), but Sun JDK can't handle that.
        return new ArrayList<T>(Arrays.asList(elements));
    }
 
    /**
     * Useful for queues.
     */
    public static <T> LinkedList<T> newLinkedList()
    {
        return new LinkedList<T>();
    }
 
    /**
     * Constructs and returns a new {@link java.util.ArrayList} as a copy of the provided collection.
     */
    public static <T, V extends T> List<T> newList(Collection<V> list)
    {
        return new ArrayList<T>(list);
    }
 
    /**
     * Constructs and returns a new {@link java.util.concurrent.CopyOnWriteArrayList}.
     */
    public static <T> List<T> newThreadSafeList()
    {
        return new CopyOnWriteArrayList<T>();
    }
 
    public static <T> Stack<T> newStack()
    {
        return new Stack<T>();
    }
}
