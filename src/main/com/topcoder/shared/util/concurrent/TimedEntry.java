/*
 * TimedEntry
 * 
 * Created Oct 25, 2007
 */
package com.topcoder.shared.util.concurrent;

public class TimedEntry<T> {
    private long entryTS;
    private T value;

    public TimedEntry(T value) {
        this.value = value;
    }

    public long getEntryTS() {
        return entryTS;
    }

    public T getValue() {
        return value;
    }
}