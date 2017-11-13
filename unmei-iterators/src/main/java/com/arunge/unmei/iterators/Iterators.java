package com.arunge.unmei.iterators;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Iterators {

    public static <R, T> Iterator<T> map(Iterator<R> base, Function<R, T> transform) {
        return new TransformIterator<R, T>(base, transform);
    }
    
    public static <T> Iterator<T> filter(Iterator<T> base, Predicate<T> filter) {
        return new FilterIterator<T>(base, filter);
    }
    
    public static <T> Stream<T> toStream(Iterator<T> base) {
        Iterable<T> iterable = () -> base;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
