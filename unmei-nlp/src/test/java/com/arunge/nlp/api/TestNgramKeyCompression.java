package com.arunge.nlp.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestNgramKeyCompression {

        @Test
        public void testBigramNgramKeyCompression() {
            long key = NgramKeyCompression.generateBigramKey(123, 456);
//            System.out.println(key);
//            System.out.println(key >> 32);
//            System.out.println((int) key);
            int[] indexes = NgramKeyCompression.extractBigramKey(key);
            assertEquals(123, indexes[0]);
            assertEquals(456, indexes[1]);
        }

        @Test
        public void testTrigramKeyCompression() {
            long key = NgramKeyCompression.generateTrigramKey(123, 456, 789);
            int[] indexes = NgramKeyCompression.extractTrigramKey(key);
            assertEquals(123, indexes[0]);
            assertEquals(456, indexes[1]);
            assertEquals(789, indexes[2]);
            
        }
}
