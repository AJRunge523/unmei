package com.arunge.nlp.vocab;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.arunge.nlp.api.Vocabulary;

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
        DFVocabulary vocab = new DFVocabulary();
        vocab.getOrAdd("this");
        vocab.getOrAdd("is");
        vocab.getOrAdd("a");
        vocab.getOrAdd("test");
        vocab.setDocFrequency(0, 3);
        vocab.setDocFrequency(1, 2);
        vocab.setDocFrequency(2, 2);
        vocab.setDocFrequency(3, 1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        vocab.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DFVocabulary deser = DFVocabulary.read(bais);
        assertEquals(vocab.size(), deser.size());
        assertEquals(vocab.getIndex("this"), deser.getIndex("this"));
        assertEquals(vocab.getIndex("is"), deser.getIndex("is"));
        assertEquals(vocab.getIndex("a"), deser.getIndex("a"));
        assertEquals(vocab.getIndex("test"), deser.getIndex("test"));
        assertArrayEquals(vocab.getDocFrequencies(), deser.getDocFrequencies());
    }
    
}
