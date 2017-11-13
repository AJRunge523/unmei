package com.arunge.unmei.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilterIterator<T> implements Iterator<T> {

    protected Iterator<T> internal;
    private T next;
    private Predicate<T> filter;
    
    public FilterIterator(Iterator<T> base, Predicate<T> filter) {
        this.internal = base;
        this.filter = filter;
        this.next = null;
    }
    
    @Override
    public boolean hasNext() {
        if(next != null) {
            return true;
        }
        if(getNext() == false) {
            return false;
        }
        return true;
    }

    @Override
    public T next() {
        if(next == null) { 
            if(getNext() == false) {
                throw new NoSuchElementException();
            } else {
                return next;
            }
        }
        return next;
    }
    
    private boolean getNext() {
        while(internal.hasNext()) {
            T candidate = internal.next();
            if(filter.test(candidate)) {
                next = candidate;
                return true;
            }
        }
        return false;
    }

}
