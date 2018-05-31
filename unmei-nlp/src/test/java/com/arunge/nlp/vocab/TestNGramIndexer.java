package com.arunge.nlp.vocab;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.arunge.nlp.vocab.NGramIndexer;

public class TestNGramIndexer {

    @Test
    public void testBigramIndexAndRetrieval() {
        NGramIndexer indexer = new NGramIndexer(2);
        int index = indexer.getOrAdd("this", "is");
        indexer.getOrAdd("is", "a");
        indexer.getOrAdd("a", "test");
        indexer.getOrAdd("test", "a");
        int testIndex = indexer.getOrAdd("this", "test");
        System.out.println(testIndex);
        assertEquals(indexer.size(), 10);
        assertEquals(1, indexer.getIndex("this"));
        assertEquals(2, indexer.getIndex("is"));
        assertEquals(4, indexer.getIndex("a"));
        assertEquals(6, indexer.getIndex("test"));
        assertEquals(3, indexer.getIndex("this", "is"));
        assertEquals(5, indexer.getIndex("is", "a"));
        assertEquals(7, indexer.getIndex("a", "test"));
        assertEquals(8, indexer.getIndex("test", "a"));
        assertEquals(9, indexer.getIndex("this", "test"));
        assertEquals(-1, indexer.getIndex("not", "present"));
        assertEquals(-1, indexer.getIndex("try", "a", "trigram"));
        assertEquals(-1, indexer.getIndex("try"));
        assertArrayEquals(new String[]{"this", "is"}, indexer.getNgram(index));
        assertArrayEquals(new String[] {"test"}, indexer.getNgram(6));
        assertEquals(indexer.getOrAdd("is", "a"), 5);
    }
    
    @Test
    public void testTrigramIndexAndRetrieval() {
        NGramIndexer indexer = new NGramIndexer(3);
        int index = indexer.getOrAdd("this", "is", "a");
        indexer.getOrAdd("is", "a", "test");
        indexer.getOrAdd("a", "test", "of");
        indexer.getOrAdd("test", "of", "a");
        indexer.getOrAdd("of", "a", "vocabulary");
        assertEquals(indexer.size(), 18);
        assertEquals(1, indexer.getIndex("this"));
        assertEquals(2, indexer.getIndex("is"));
        assertEquals(3, indexer.getIndex("a"));
        assertEquals(7, indexer.getIndex("test"));
        assertEquals(10, indexer.getIndex("of"));
        assertEquals(4, indexer.getIndex("this", "is"));
        assertEquals(5, indexer.getIndex("is", "a"));
        assertEquals(8, indexer.getIndex("a", "test"));
        assertEquals(11, indexer.getIndex("test", "of"));
        assertEquals(13, indexer.getIndex("of", "a"));
        assertEquals(16, indexer.getIndex("a", "vocabulary"));
        
        assertEquals(6, indexer.getIndex("this", "is", "a"));
        assertEquals(9, indexer.getIndex("is", "a", "test"));
        assertEquals(12, indexer.getIndex("a", "test", "of"));
        assertEquals(14, indexer.getIndex("test", "of", "a"));
        assertEquals(17, indexer.getIndex("of", "a", "vocabulary"));
        
        assertEquals(-1, indexer.getIndex("not", "present"));
        assertEquals(-1, indexer.getIndex("try", "a", "trigram"));
        assertEquals(-1, indexer.getIndex("try"));
        assertArrayEquals(new String[]{"this", "is", "a"}, indexer.getNgram(index));
        assertArrayEquals(new String[] {"test"}, indexer.getNgram(7));
        assertEquals(indexer.getOrAdd("is", "a", "test"), 9);
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
        System.out.println(decoded.size());
        assertEquals(1, indexer.getIndex("this"));
        assertEquals(2, indexer.getIndex("is"));
        assertEquals(3, indexer.getIndex("a"));
        assertEquals(7, indexer.getIndex("test"));
        assertEquals(10, indexer.getIndex("of"));
        assertEquals(4, indexer.getIndex("this", "is"));
        assertEquals(5, indexer.getIndex("is", "a"));
        assertEquals(8, indexer.getIndex("a", "test"));
        assertEquals(11, indexer.getIndex("test", "of"));
        assertEquals(13, indexer.getIndex("of", "a"));
        assertEquals(16, indexer.getIndex("a", "vocabulary"));
        
        assertEquals(6, indexer.getIndex("this", "is", "a"));
        assertEquals(9, indexer.getIndex("is", "a", "test"));
        assertEquals(12, indexer.getIndex("a", "test", "of"));
        assertEquals(14, indexer.getIndex("test", "of", "a"));
        assertEquals(17, indexer.getIndex("of", "a", "vocabulary"));
        
        assertEquals(-1, indexer.getIndex("not", "present"));
        assertEquals(-1, indexer.getIndex("try", "a", "trigram"));
        assertEquals(-1, indexer.getIndex("try"));
        assertArrayEquals(new String[]{"this", "is", "a"}, indexer.getNgram(index));
        assertArrayEquals(new String[] {"test"}, indexer.getNgram(7));
        assertEquals(indexer.getOrAdd("is", "a", "test"), 9);
    }
    
    
}

