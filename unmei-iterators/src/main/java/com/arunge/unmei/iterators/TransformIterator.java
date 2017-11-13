package com.arunge.unmei.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class TransformIterator<R, T> implements Iterator<T> {

    protected Iterator<R> internal;
    private Function<R, T> transform;
    
    public TransformIterator(Iterator<R> iter, Function<R, T> transform) {
        this.internal = iter;
        this.transform = transform;
    }

    @Override
    public boolean hasNext() {
        return internal.hasNext();
    }

    @Override
    public T next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        return transform.apply(internal.next());
    }
    
}
