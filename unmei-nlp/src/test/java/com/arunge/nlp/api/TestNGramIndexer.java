package com.arunge.nlp.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class TestNGramIndexer {

    @Test
    public void testBigramIndexAndRetrieval() {
        NGramIndexer indexer = new NGramIndexer(2);
        int index = indexer.getOrAdd("this", "is");
        indexer.getOrAdd("is", "a");
        indexer.getOrAdd("a", "test");
        indexer.getOrAdd("test", "a");
        indexer.getOrAdd("this", "test");
        
        assertEquals(0, indexer.getIndex("this"));
        assertEquals(1, indexer.getIndex("is"));
        assertEquals(2, indexer.getIndex("a"));
        assertEquals(3, indexer.getIndex("test"));
        assertEquals(1, indexer.getIndex("this", "is"));
        assertEquals(2, indexer.getIndex("is", "a"));
        assertEquals(3, indexer.getIndex("a", "test"));
        assertEquals(4, indexer.getIndex("test", "a"));
        assertEquals(-1, indexer.getIndex("not", "present"));
        assertEquals(-1, indexer.getIndex("try", "a", "trigram"));
        assertEquals(-1, indexer.getIndex("try"));
        assertArrayEquals(new String[]{"this", "is"}, indexer.getNgram(index, 2));
        assertArrayEquals(new String[] {"test"}, indexer.getNgram(3, 1));
        assertEquals(indexer.size(1), 4);
        assertEquals(indexer.size(2), 5);
        assertEquals(indexer.getOrAdd("is", "a"), 2);
        assertEquals(indexer.size(2), 5);
    }
    
    @Test
    public void testTrigramIndexAndRetrieval() {
        NGramIndexer indexer = new NGramIndexer(3);
        int index = indexer.getOrAdd("this", "is", "a");
        indexer.getOrAdd("is", "a", "test");
        indexer.getOrAdd("a", "test", "of");
        indexer.getOrAdd("test", "of", "a");
        indexer.getOrAdd("of", "a", "vocabulary");
        assertEquals(6, indexer.size(1));
        assertEquals(6, indexer.size(2));
        assertEquals(5, indexer.size(3));
        assertEquals(0, indexer.getIndex("this"));
        assertEquals(1, indexer.getIndex("is"));
        assertEquals(2, indexer.getIndex("a"));
        assertEquals(3, indexer.getIndex("test"));
        assertEquals(1, indexer.getIndex("this", "is"));
        assertEquals(2, indexer.getIndex("is", "a"));
        assertEquals(3, indexer.getIndex("a", "test"));
        assertEquals(4, indexer.getIndex("test", "of"));
        assertEquals(5, indexer.getIndex("of", "a"));
        assertEquals(6, indexer.getIndex("a", "vocabulary"));
        
        assertEquals(1, indexer.getIndex("this", "is", "a"));
        assertEquals(2, indexer.getIndex("is", "a", "test"));
        assertEquals(3, indexer.getIndex("a", "test", "of"));
        assertEquals(4, indexer.getIndex("test", "of", "a"));
        assertEquals(5, indexer.getIndex("of", "a", "vocabulary"));
        
        assertEquals(-1, indexer.getIndex("not", "present"));
        assertEquals(-1, indexer.getIndex("try", "a", "trigram"));
        assertEquals(-1, indexer.getIndex("try"));
        assertArrayEquals(new String[]{"this", "is", "a"}, indexer.getNgram(index, 3));
        assertArrayEquals(new String[] {"test"}, indexer.getNgram(3, 1));
        assertEquals(indexer.getOrAdd("is", "a", "test"), 2);
        assertEquals(indexer.size(3), 5);
    }
    
    @Test
    public void testInvalidOrder() { 
        try {
            new NGramIndexer(4);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }
 
    @Test
    public void testExport() throws IOException {
        NGramIndexer indexer = new NGramIndexer(3);
        int index = indexer.getOrAdd("this", "is", "a");
        indexer.getOrAdd("is", "a", "test");
        indexer.getOrAdd("a", "test", "of");
        indexer.getOrAdd("test", "of", "a");
        indexer.getOrAdd("of", "a", "vocabulary");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        indexer.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        NGramIndexer decoded = NGramIndexer.read(bais);
        assertEquals(6, decoded.size(1));
        assertEquals(6, decoded.size(2));
        assertEquals(5, decoded.size(3));
        assertEquals(0, decoded.getIndex("this"));
        assertEquals(1, decoded.getIndex("is"));
        assertEquals(2, decoded.getIndex("a"));
        assertEquals(3, decoded.getIndex("test"));
        assertEquals(1, decoded.getIndex("this", "is"));
        assertEquals(2, decoded.getIndex("is", "a"));
        assertEquals(3, decoded.getIndex("a", "test"));
        assertEquals(4, decoded.getIndex("test", "of"));
        assertEquals(5, decoded.getIndex("of", "a"));
        assertEquals(6, decoded.getIndex("a", "vocabulary"));
        
        assertEquals(1, decoded.getIndex("this", "is", "a"));
        assertEquals(2, decoded.getIndex("is", "a", "test"));
        assertEquals(3, decoded.getIndex("a", "test", "of"));
        assertEquals(4, decoded.getIndex("test", "of", "a"));
        assertEquals(5, decoded.getIndex("of", "a", "vocabulary"));
        
        assertEquals(-1, decoded.getIndex("not", "present"));
        assertEquals(-1, decoded.getIndex("try", "a", "trigram"));
        assertEquals(-1, decoded.getIndex("try"));
        assertArrayEquals(new String[]{"this", "is", "a"}, decoded.getNgram(index, 3));
        assertArrayEquals(new String[] {"test"}, decoded.getNgram(3, 1));
        assertEquals(decoded.getOrAdd("is", "a", "test"), 2);
        assertEquals(decoded.size(3), 5);
    }
    
}

