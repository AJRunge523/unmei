package com.arunge.unmei.iterators;

import java.io.Closeable;
import java.util.Iterator;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseableTransformIterator<R, T> extends TransformIterator<R, T> implements CloseableIterator<T>{

    private static Logger LOG = LoggerFactory.getLogger(CloseableTransformIterator.class);
    
    public CloseableTransformIterator(Iterator<R> iter, Function<R, T> transform) {
        super(iter, transform);
        // TODO Auto-generated constructor stub
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
