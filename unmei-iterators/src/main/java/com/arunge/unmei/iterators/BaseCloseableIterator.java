package com.arunge.unmei.iterators;

import java.io.Closeable;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseCloseableIterator<T> implements CloseableIterator<T> {

    private static Logger LOG = LoggerFactory.getLogger(BaseCloseableIterator.class);
    
    private Iterator<T> internal;
    
    public BaseCloseableIterator(Iterator<T> iterator) {
        this.internal = iterator;
    }
    
    @Override
    public boolean hasNext() {
        return internal.hasNext();
    }

    @Override
    public T next() {
        return internal.next();
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
