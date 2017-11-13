package com.arunge.unmei.iterators;

import java.io.Closeable;
import java.util.Iterator;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseableFilterIterator<T> extends FilterIterator<T> implements CloseableIterator<T>{

    private static Logger LOG = LoggerFactory.getLogger(CloseableFilterIterator.class);
    
    public CloseableFilterIterator(Iterator<T> base, Predicate<T> filter) {
        super(base, filter);
    }

    @Override
    public void close() throws Exception {
        if(internal instanceof Closeable) {
            try {
                ((Closeable) internal).close();
            } catch (Exception e) {
                LOG.error("Error closing iterator.", e);
            }
        } else if(internal instanceof AutoCloseable) {
            try {
                ((AutoCloseable) internal).close();
            } catch (Exception e) { 
                LOG.error("Error closing iterator.", e);
            }
        }
        
        //Otherwise do nothing.
    }

}
