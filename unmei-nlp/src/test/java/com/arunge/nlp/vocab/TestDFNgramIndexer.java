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
        indexer.incrementDocFrequency(tri1);
        indexer.incrementDocFrequency(tri1);
        assertEquals(2, indexer.getDocFrequency(tri1));
        indexer.incrementDocFrequency(tri2);
        assertEquals(1, indexer.getDocFrequency(tri2));
        indexer.incrementDocFrequency(1);
        assertEquals(1, indexer.getDocFrequency(1));
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
        int uni1 = indexer.getIndex("this");
        int uni2 = indexer.getIndex("is");
        int uni3 = indexer.getIndex("a");
        int uni4 = indexer.getIndex("test");
        int uni5 = indexer.getIndex("vocabulary");
        int bi1 = indexer.getIndex("this", "is");
        int bi2 = indexer.getIndex("is", "a");
        int bi3 = indexer.getIndex("a", "test");
        int bi4 = indexer.getIndex("test", "vocabulary");
        for(int i =0; i < 3; i++) {
            indexer.incrementNumDocs();
        }
        for(int i = 0; i < 3; i++) {
            indexer.incrementDocFrequency(tri1);
            indexer.incrementDocFrequency(uni1);
            indexer.incrementDocFrequency(uni5);
            indexer.incrementDocFrequency(bi3);
        }
        for(int i = 0; i < 2; i++) {
            indexer.incrementDocFrequency(tri2);
            indexer.incrementDocFrequency(uni2);
            indexer.incrementDocFrequency(uni4);
            indexer.incrementDocFrequency(bi4);
        }
        indexer.incrementDocFrequency(tri3);
        indexer.incrementDocFrequency(uni3);
        indexer.incrementDocFrequency(bi1);
        indexer.incrementDocFrequency(bi2);
        double[] idfVector = indexer.computeIDFVector();
        assertEquals(0.0, idfVector[uni1], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[uni2], 0.0001);
        assertEquals(Math.log(3.0), idfVector[uni3], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[uni4], 0.0001);
        assertEquals(0.0, idfVector[uni5], 0.0001);
        assertEquals(Math.log(3.0), idfVector[bi1], 0.0001);
        assertEquals(Math.log(3.0), idfVector[bi2], 0.0001);
        assertEquals(0.0, idfVector[bi3], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[bi4], 0.0001);
        assertEquals(0.0, idfVector[tri1], 0.0001);
        assertEquals(Math.log(3.0/2), idfVector[tri2], 0.0001);
        assertEquals(Math.log(3.0), idfVector[tri3], 0.0001);
    }
    
    @Test
    public void testTrimTail() {
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        int tri1 = indexer.getOrAdd("this", "is", "a");
        int tri2 = indexer.getOrAdd("is", "a", "test");
        int tri3 = indexer.getOrAdd("a", "test", "vocabulary");
        int uni1 = indexer.getIndex("this");
        int uni2 = indexer.getIndex("is");
        int uni3 = indexer.getIndex("a");
        int uni4 = indexer.getIndex("test");
        int uni5 = indexer.getIndex("vocabulary");
        int bi1 = indexer.getIndex("this", "is");
        int bi2 = indexer.getIndex("is", "a");
        int bi3 = indexer.getIndex("a", "test");
        int bi4 = indexer.getIndex("test", "vocabulary");
        for(int i =0; i < 3; i++) {
            indexer.incrementNumDocs();
        }        
        for(int i = 0; i < 3; i++) {
            indexer.incrementDocFrequency(tri1);
            indexer.incrementNgramFrequency(tri1);
            indexer.incrementDocFrequency(tri3);
            indexer.incrementNgramFrequency(tri3);
            indexer.incrementDocFrequency(bi3);
            indexer.incrementNgramFrequency(bi3);
            indexer.incrementDocFrequency(uni1);
            indexer.incrementNgramFrequency(uni1);
            indexer.incrementDocFrequency(uni2);
            indexer.incrementNgramFrequency(uni2);
            indexer.incrementDocFrequency(uni3);
            indexer.incrementNgramFrequency(uni3);
            indexer.incrementDocFrequency(uni4);
            indexer.incrementNgramFrequency(uni4);
            indexer.incrementDocFrequency(uni5);
            indexer.incrementNgramFrequency(uni5);
        }
        for(int i = 0; i < 2; i++) {
            indexer.incrementDocFrequency(tri2);
            indexer.incrementNgramFrequency(tri2);
            indexer.incrementDocFrequency(bi1);
            indexer.incrementNgramFrequency(bi1);
        }
        indexer.incrementDocFrequency(bi2);
        indexer.incrementNgramFrequency(bi2);
        indexer.incrementDocFrequency(bi4);
        indexer.incrementNgramFrequency(bi4);

        assertEquals(indexer.getNumNgrams(1), 15);
        assertEquals(indexer.getNumNgrams(2), 7);
        assertEquals(indexer.getNumNgrams(3), 8);
        
        CountingNGramIndexer newIndexer = indexer.trimTail(0, 3);
        assertEquals(5, newIndexer.size(1));
        assertEquals(1, newIndexer.size(2));
        assertEquals(2, newIndexer.size(3));
        assertTrue(newIndexer.contains("this"));
        assertTrue(newIndexer.contains("vocabulary"));
        assertTrue(newIndexer.contains("a", "test"));
        assertTrue(newIndexer.contains("this", "is", "a"));
        assertTrue(newIndexer.contains("a", "test", "vocabulary"));
        
        assertEquals(15, newIndexer.getNumNgrams(1));
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("this")));
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("is")));
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("a")));
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("test")));
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("vocabulary")));
        assertEquals(3, newIndexer.getNumNgrams(2), 3);
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("a", "test")));
        assertEquals(6, newIndexer.getNumNgrams(3), 6);
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("this", "is", "a")));
        assertEquals(3, newIndexer.getNgramFrequency(newIndexer.getIndex("a", "test", "vocabulary")));
        
    }
    
    @Test
    public void testSerialization() throws IOException { 
        CountingNGramIndexer indexer = new CountingNGramIndexer(3);
        int tri1 = indexer.getOrAdd("this", "is", "a");
        int tri2 = indexer.getOrAdd("is", "a", "test");
        int tri3 = indexer.getOrAdd("a", "test", "vocabulary");
        int uni1 = indexer.getIndex("this");
        int uni2 = indexer.getIndex("is");
        int uni3 = indexer.getIndex("a");
        int uni4 = indexer.getIndex("test");
        int uni5 = indexer.getIndex("vocabulary");
        int bi1 = indexer.getIndex("this", "is");
        int bi2 = indexer.getIndex("is", "a");
        int bi3 = indexer.getIndex("a", "test");
        int bi4 = indexer.getIndex("test", "vocabulary");
        for(int i =0; i < 3; i++) {
            indexer.incrementNumDocs();
        }        for(int i = 0; i < 3; i++) {
            indexer.incrementDocFrequency(tri1);
            indexer.incrementDocFrequency(tri3);
            indexer.incrementDocFrequency(bi1);
            indexer.incrementDocFrequency(uni1);
            indexer.incrementDocFrequency(uni2);
            indexer.incrementDocFrequency(uni3);
            indexer.incrementDocFrequency(uni4);
            indexer.incrementDocFrequency(uni5);
        }
        for(int i = 0; i < 2; i++) {
            indexer.incrementDocFrequency(tri2);
            indexer.incrementDocFrequency(bi4);
        }
        indexer.incrementDocFrequency(bi2);
        indexer.incrementDocFrequency(bi3);
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
