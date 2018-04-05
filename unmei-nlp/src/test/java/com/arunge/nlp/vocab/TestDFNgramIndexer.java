package com.arunge.nlp.vocab;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class TestDFNgramIndexer {

    @Test
    public void testDocFrequencies() {
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        int tri1 = indexer.getOrAdd("this", "is", "a");
        int tri2 = indexer.getOrAdd("is", "a", "test");
        indexer.incrementDocFrequency(tri1, 3);
        indexer.incrementDocFrequency(tri1, 3);
        assertEquals(2, indexer.getDocFrequency(tri1, 3));
        indexer.incrementDocFrequency(tri2, 3);
        assertEquals(1, indexer.getDocFrequency(tri2, 3));
        indexer.incrementDocFrequency(1, 2);
        assertEquals(1, indexer.getDocFrequency(1, 2));
    }
    
    @Test
    public void testResizing() {
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        indexer.getOrAdd("a", "b", "c");
        indexer.getOrAdd("a", "b", "d");
        indexer.getOrAdd("a", "b", "e");
        indexer.getOrAdd("a", "b", "f");
        indexer.getOrAdd("a", "b", "g");
        indexer.getOrAdd("a", "c", "c");
        indexer.getOrAdd("a", "d", "c");
        indexer.getOrAdd("a", "e", "c");
        indexer.getOrAdd("a", "f", "c");
        indexer.getOrAdd("a", "g", "c");
        indexer.getOrAdd("e", "b", "c");
        assertEquals(11, indexer.size(3));
    }
    
    @Test
    public void testIDFVector() {
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        int tri1 = indexer.getOrAdd("this", "is", "a");
        int tri2 = indexer.getOrAdd("is", "a", "test");
        int tri3 = indexer.getOrAdd("a", "test", "vocabulary");
        for(int i =0; i < 3; i++) {
            indexer.incrementNumDocs();
        }
        for(int i = 0; i < 3; i++) {
            indexer.incrementDocFrequency(tri1, 3);
            indexer.incrementDocFrequency(3, 2);
            indexer.incrementDocFrequency(0, 1);
            indexer.incrementDocFrequency(4, 1);
        }
        for(int i = 0; i < 2; i++) {
            indexer.incrementDocFrequency(tri2, 3);
            indexer.incrementDocFrequency(4, 2);
            indexer.incrementDocFrequency(1, 1);
            indexer.incrementDocFrequency(3, 1);
        }
        indexer.incrementDocFrequency(tri3, 3);
        indexer.incrementDocFrequency(1, 2);
        indexer.incrementDocFrequency(2, 2);
        indexer.incrementDocFrequency(2, 1);
        double[][] idfVector = indexer.computeIDFVector();
        assertEquals(0.0, idfVector[0][0], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[0][1], 0.0001);
        assertEquals(Math.log(3.0), idfVector[0][2], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[0][3], 0.0001);
        assertEquals(0.0, idfVector[0][4], 0.0001);
        assertEquals(Math.log(3.0), idfVector[1][1], 0.0001);
        assertEquals(Math.log(3.0), idfVector[1][2], 0.0001);
        assertEquals(0.0, idfVector[1][3], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[1][4], 0.0001);
        assertEquals(0.0, idfVector[2][1], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[2][2], 0.0001);
        assertEquals(Math.log(3.0), idfVector[2][3], 0.0001);
    }
    
    @Test
    public void testTrimTail() {
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        int tri1 = indexer.getOrAdd("this", "is", "a");
        int tri2 = indexer.getOrAdd("is", "a", "test");
        int tri3 = indexer.getOrAdd("a", "test", "vocabulary");
        for(int i =0; i < 3; i++) {
            indexer.incrementNumDocs();
        }        
        for(int i = 0; i < 3; i++) {
            indexer.incrementDocFrequency(tri1, 3);
            indexer.incrementNgramFrequency(tri1, 3);
            indexer.incrementDocFrequency(tri3, 3);
            indexer.incrementNgramFrequency(tri3, 3);
            indexer.incrementDocFrequency(3, 2);
            indexer.incrementNgramFrequency(3, 2);
            indexer.incrementDocFrequency(0, 1);
            indexer.incrementNgramFrequency(0, 1);
            indexer.incrementDocFrequency(1, 1);
            indexer.incrementNgramFrequency(1, 1);
            indexer.incrementDocFrequency(2, 1);
            indexer.incrementNgramFrequency(2, 1);
            indexer.incrementDocFrequency(3, 1);
            indexer.incrementNgramFrequency(3, 1);
            indexer.incrementDocFrequency(4, 1);
            indexer.incrementNgramFrequency(4, 1);
        }
        for(int i = 0; i < 2; i++) {
            indexer.incrementDocFrequency(tri2, 3);
            indexer.incrementNgramFrequency(tri2, 3);
            indexer.incrementDocFrequency(4, 2);
            indexer.incrementNgramFrequency(4, 2);
        }
        indexer.incrementDocFrequency(1, 2);
        indexer.incrementNgramFrequency(1, 2);
        indexer.incrementDocFrequency(2, 2);
        indexer.incrementNgramFrequency(2, 2);

        assertEquals(indexer.getNumNgrams(1), 15);
        assertEquals(indexer.getNumNgrams(2), 7);
        assertEquals(indexer.getNumNgrams(3), 8);
        
        CountingNGramIndexer newIndexer = indexer.trimTail(3);
        assertEquals(5, newIndexer.size(1));
        assertEquals(1, newIndexer.size(2));
        assertEquals(2, newIndexer.size(3));
        assertTrue(newIndexer.contains("this"));
        assertTrue(newIndexer.contains("vocabulary"));
        System.out.println(newIndexer.getIndex("a", "test"));
        assertTrue(newIndexer.contains("a", "test"));
        assertTrue(newIndexer.contains("this", "is", "a"));
        assertTrue(newIndexer.contains("a", "test", "vocabulary"));
        
        assertEquals(newIndexer.getNumNgrams(1), 15);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("this"), 1), 3);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("is"), 1), 3);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("a"), 1), 3);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("test"), 1), 3);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("vocabulary"), 1), 3);
        assertEquals(newIndexer.getNumNgrams(2), 3);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("a", "test"), 2), 3);
        assertEquals(newIndexer.getNumNgrams(3), 6);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("this", "is", "a"), 3), 3);
        assertEquals(newIndexer.getNgramFrequency(newIndexer.getIndex("a", "test", "vocabulary"), 3), 3);
        
    }
    
    @Test
    public void testSerialization() throws IOException { 
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        int tri1 = indexer.getOrAdd("this", "is", "a");
        int tri2 = indexer.getOrAdd("is", "a", "test");
        int tri3 = indexer.getOrAdd("a", "test", "vocabulary");
        for(int i =0; i < 3; i++) {
            indexer.incrementNumDocs();
        }        for(int i = 0; i < 3; i++) {
            indexer.incrementDocFrequency(tri1, 3);
            indexer.incrementDocFrequency(tri3, 3);
            indexer.incrementDocFrequency(3, 2);
            indexer.incrementDocFrequency(0, 1);
            indexer.incrementDocFrequency(1, 1);
            indexer.incrementDocFrequency(2, 1);
            indexer.incrementDocFrequency(3, 1);
            indexer.incrementDocFrequency(4, 1);
        }
        for(int i = 0; i < 2; i++) {
            indexer.incrementDocFrequency(tri2, 3);
            indexer.incrementDocFrequency(4, 2);
        }
        indexer.incrementDocFrequency(1, 2);
        indexer.incrementDocFrequency(2, 2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        indexer.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        CountingNGramIndexer deser = CountingNGramIndexer.read(bais);
        assertEquals(indexer.getNumDocs(), deser.getNumDocs());
        assertEquals(indexer.size(1), deser.size(1));
        assertEquals(indexer.size(2), deser.size(2));
        assertEquals(indexer.size(3), deser.size(3));
        assertArrayEquals(indexer.getDocFrequencies(), deser.getDocFrequencies());
    }
}
