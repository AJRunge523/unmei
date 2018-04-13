package com.arunge.nlp.vocab;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class TestVocabularySerialization {

    @Test
    public void testSerialization() throws IOException {
        Vocabulary vocab = new Vocabulary();
        vocab.getOrAdd("this");
        vocab.getOrAdd("is");
        vocab.getOrAdd("a");
        vocab.getOrAdd("test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        vocab.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Vocabulary deser = Vocabulary.read(bais);
        assertEquals(vocab.size(), deser.size());
        assertEquals(vocab.getIndex("this"), deser.getIndex("this"));
        assertEquals(vocab.getIndex("is"), deser.getIndex("is"));
        assertEquals(vocab.getIndex("a"), deser.getIndex("a"));
        assertEquals(vocab.getIndex("test"), deser.getIndex("test"));
    }
    
    @Test
    public void testDFVocabSerialization() throws IOException {
        CountingVocabulary vocab = new CountingVocabulary();
        vocab.getOrAdd("this");
        vocab.getOrAdd("is");
        vocab.getOrAdd("a");
        vocab.getOrAdd("test");
        for(int i = 0; i < 3; i++) {
            vocab.incrementDocFrequency(0);    
        }
        for(int i = 0; i < 2; i++) {
            vocab.incrementDocFrequency(1);
            vocab.incrementDocFrequency(2);            
        }
        vocab.incrementDocFrequency(3);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        vocab.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        CountingVocabulary deser = CountingVocabulary.read(bais);
        assertEquals(vocab.size(), deser.size());
        assertEquals(vocab.getIndex("this"), deser.getIndex("this"));
        assertEquals(vocab.getIndex("is"), deser.getIndex("is"));
        assertEquals(vocab.getIndex("a"), deser.getIndex("a"));
        assertEquals(vocab.getIndex("test"), deser.getIndex("test"));
        assertArrayEquals(vocab.getDocFrequencies(), deser.getDocFrequencies());
    }
    
}
