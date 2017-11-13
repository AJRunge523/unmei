package com.arunge.unmei.iterators;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class CloseableIterators {

    public static <T> CloseableIterator<T> wrap(Iterator<T> iterator) { 
        return new BaseCloseableIterator<T>(iterator);
    }
    
    public static <R, T> CloseableIterator<T> map(Iterator<R> base, Function<R, T> transform) {
        return new CloseableTransformIterator<R, T>(base, transform);
    }
    
    public static <T> CloseableIterator<T> filter(Iterator<T> base, Predicate<T> filter) {
        return new CloseableFilterIterator<T>(base, filter);
    }
    
}
