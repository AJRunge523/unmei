package com.arunge.nlp.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestNgramKeyCompression {

        @Test
        public void testKeyCompression() {
            long key = NgramKeyCompression.generateBigramKey(123, 456);
            int[] indexes = NgramKeyCompression.extractKey(key);
            assertEquals(2, indexes.length);
            assertEquals(123, indexes[0]);
            assertEquals(456, indexes[1]);
            
            key = NgramKeyCompression.generateTrigramKey(123, 456, 789);
            indexes = NgramKeyCompression.extractKey(key);
            assertEquals(3, indexes.length);
            assertEquals(123, indexes[0]);
            assertEquals(456, indexes[1]);
            assertEquals(789, indexes[2]);
        }
    
        @Test
        public void testBigramNgramKeyCompression() {
            long key = NgramKeyCompression.generateBigramKey(123, 456);
            int[] indexes = NgramKeyCompression.extractBigramKey(key);
            assertEquals(123, indexes[0]);
            assertEquals(456, indexes[1]);
            int v1 = (int) Math.pow(2,  30) - 1;
            key = NgramKeyCompression.generateBigramKey(v1, v1);
            System.out.println(Long.toBinaryString(key));
            indexes = NgramKeyCompression.extractBigramKey(key);
            assertEquals(v1, indexes[0]);
            assertEquals(v1, indexes[1]);
        }

        @Test
        public void testTrigramKeyCompression() {
            long key = NgramKeyCompression.generateTrigramKey(123, 456, 789);
            int[] indexes = NgramKeyCompression.extractTrigramKey(key);
            assertEquals(123, indexes[0]);
            assertEquals(456, indexes[1]);
            assertEquals(789, indexes[2]);
            int v1 = (int) Math.pow(2, 20) - 1;
            key = NgramKeyCompression.generateTrigramKey(v1, v1, v1);
            System.out.println(Long.toBinaryString(key));
            indexes = NgramKeyCompression.extractTrigramKey(key);
            assertEquals(v1, indexes[0]);
            assertEquals(v1, indexes[1]);
            assertEquals(v1, indexes[2]);
        }
        
        @Test
        public void testIllegalBigramValues() {
            try {
                NgramKeyCompression.generateBigramKey((int) Math.pow(2, 30), 1);
                fail();
            } catch (UnsupportedOperationException e) {
            }
            try { 
                NgramKeyCompression.generateTrigramKey((int) Math.pow(2, 20), 1, 1);
                fail();
            } catch (UnsupportedOperationException e) { 
            }
        }
        
}
