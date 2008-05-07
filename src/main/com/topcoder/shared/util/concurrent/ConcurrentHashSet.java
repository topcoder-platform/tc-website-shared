/*
 * ConcurrentHashSet
 * 
 * Created May 3, 2008
 */
package com.topcoder.shared.util.concurrent;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashSet backed up for a {@link ConcurrentHashMap}.
 * 
 * @autor Diego Belfer (Mural)
 * @version $Id$
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, Serializable {
    private final ConcurrentHashMap<E, Boolean> map;
    
    public ConcurrentHashSet() {
        map = new ConcurrentHashMap();
    }
    
    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<E,Boolean>(initialCapacity);
    }
    
    public ConcurrentHashSet(int initialCapacity, float loadFactor, int concurrencyFactory) {
        map = new ConcurrentHashMap<E,Boolean>(initialCapacity, loadFactor, concurrencyFactory);
    }

    public ConcurrentHashSet(Collection<? extends E> c) {
        map = new ConcurrentHashMap<E,Boolean>(c.size()+1);
        addAll(c);
    }
    
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }
 
    public boolean add(E o) {
        return map.put(o, Boolean.TRUE) == Boolean.TRUE;
    }
    
    public boolean remove(Object o) {
        return map.remove(o) == Boolean.TRUE;
    }
    
    public void clear() {
        map.clear();
    }
    
    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    
    public Object[] toArray() {
        return map.keySet().toArray();
    }
    
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
}
