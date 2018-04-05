package com.arunge.nlp.vocab;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.CorpusDocument;
import com.arunge.nlp.api.NGramCorpusDocument;
import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.api.Vocabulary;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.TextDocument;

public class TestCountingNGramCorpus {
    static private NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
    
    @Test
    public void testSingle() { 
        String a = "The dog picked up the bone. The dog then went and buried the bone.";
        CountingNGramCorpus corpus = new CountingNGramCorpus(2);
        corpus.addTokenizedDocument(createDocument(a));
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(10));
        assertThat(corpus.getDocuments().size(), equalTo(1));
        NGramCorpusDocument d = (NGramCorpusDocument) corpus.getDocuments().get(0);
        assertThat(d.getLength(), equalTo(16));
        List<String> words = v.getVocabWords();
        assertThat(words, hasItem("the"));
        assertThat(fetch(d, v, "the"), equalTo(4.0));
        assertThat(words, hasItem("dog"));
        assertThat(fetch(d, v, "dog"), equalTo(2.0));
        assertThat(words, hasItem("picked"));
        assertThat(fetch(d, v, "picked"), equalTo(1.0));
        assertThat(words, hasItem("up"));
        assertThat(fetch(d, v, "up"), equalTo(1.0));
        assertThat(words, hasItem("bone"));
        assertThat(fetch(d, v, "bone"), equalTo(2.0));
        assertThat(words, hasItem("then"));
        assertThat(fetch(d, v, "then"), equalTo(1.0));
        assertThat(words, hasItem("went"));
        assertThat(fetch(d, v, "went"), equalTo(1.0));
        assertThat(words, hasItem("and"));
        assertThat(fetch(d, v, "and"), equalTo(1.0));
        assertThat(words, hasItem("buried"));
        assertThat(fetch(d, v, "buried"), equalTo(1.0));
        Map<Integer, Double> docBigrams = d.getNgrams(2);
        assertEquals(11, docBigrams.size());
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("the", "dog")));
        
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("dog", "picked")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("picked", "up")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("up", "the")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("the", "bone")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("bone", ".")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("dog", "then")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("then", "went")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("went", "and")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("and", "buried")));
        assertTrue(docBigrams.containsKey(corpus.indexer.getIndex("buried", "the")));

        CountingNGramIndexer cng = (CountingNGramIndexer) corpus.getNgramIndexer();
        assertThat(cng.getNgramFrequency(2, corpus.indexer.getIndex("the", "dog")), equalTo(2L));
        assertThat(cng.getNgramFrequency(2, corpus.indexer.getIndex("the", "bone")), equalTo(2L));
        assertThat(cng.getNgramFrequency(2, corpus.indexer.getIndex("picked", "up")), equalTo(1L));
        
    }
    
    @Test
    public void multiDocTest() {
        String a = "This is the simple test the test.";
        String b = "This is a different kind of test.";
        String c = "This is this is a this a kind";
        CountingNGramCorpus corpus = new CountingNGramCorpus(2);
        corpus.addTokenizedDocument(createDocument(a));
        corpus.addTokenizedDocument(createDocument(b));
        corpus.addTokenizedDocument(createDocument(c));
        
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(10));
        assertThat(corpus.getDocuments().size(), equalTo(3));
        CorpusDocument docA = corpus.getDocuments().get(0);
        CorpusDocument docB = corpus.getDocuments().get(1);
        CorpusDocument docC = corpus.getDocuments().get(2);
        List<String> words = v.getVocabWords();
        assertThat(words, hasItem("this"));
        assertThat(fetch(docA, v, "this"), equalTo(1.0));
        assertThat(fetch(docB, v, "this"), equalTo(1.0));
        assertThat(fetch(docC, v, "this"), equalTo(3.0));
        assertThat(words, hasItem("is"));
        assertThat(fetch(docA, v, "is"), equalTo(1.0));
        assertThat(fetch(docB, v, "is"), equalTo(1.0));
        assertThat(fetch(docC, v, "is"), equalTo(2.0));
        assertThat(words, hasItem("the"));
        assertThat(fetch(docA, v, "the"), equalTo(2.0));
        assertThat(fetch(docB, v, "the"), equalTo(0.0));
        assertThat(fetch(docC, v, "the"), equalTo(0.0));
        assertThat(words, hasItem("a"));
        assertThat(fetch(docA, v, "a"), equalTo(0.0));
        assertThat(fetch(docB, v, "a"), equalTo(1.0));
        assertThat(fetch(docC, v, "a"), equalTo(2.0));
        assertThat(words, hasItem("simple"));
        assertThat(fetch(docA, v, "simple"), equalTo(1.0));
        assertThat(fetch(docB, v, "simple"), equalTo(0.0));
        assertThat(fetch(docC, v, "simple"), equalTo(0.0));
        assertThat(words, hasItem("different"));
        assertThat(fetch(docA, v, "different"), equalTo(0.0));
        assertThat(fetch(docB, v, "different"), equalTo(1.0));
        assertThat(fetch(docC, v, "different"), equalTo(0.0));
        assertThat(words, hasItem("kind"));
        assertThat(fetch(docA, v, "kind"), equalTo(0.0));
        assertThat(fetch(docB, v, "kind"), equalTo(1.0));
        assertThat(fetch(docC, v, "kind"), equalTo(1.0));
        assertThat(words, hasItem("of"));
        assertThat(fetch(docA, v, "of"), equalTo(0.0));
        assertThat(fetch(docB, v, "of"), equalTo(1.0));
        assertThat(fetch(docC, v, "of"), equalTo(0.0));
        assertThat(words, hasItem("test"));
        assertThat(fetch(docA, v, "test"), equalTo(2.0));
        assertThat(fetch(docB, v, "test"), equalTo(1.0));
        assertThat(fetch(docC, v, "test"), equalTo(0.0));
        
        CountingNGramIndexer cng = (CountingNGramIndexer) corpus.getNgramIndexer();
        assertThat(cng.getNgramFrequency(2, corpus.indexer.getIndex("this", "is")), equalTo(4L));
        assertThat(cng.getNgramFrequency(1, corpus.indexer.getIndex("test")), equalTo(3L));
        assertThat(cng.getNgramFrequency(1, corpus.indexer.getIndex("kind")), equalTo(2L));
        assertThat(cng.getNgramFrequency(2, corpus.indexer.getIndex("the", "simple")), equalTo(1L));
        try {
            assertThat(cng.getNgramFrequency(1, corpus.indexer.getIndex("wrong")), equalTo(0));
            fail();
        } catch (IndexOutOfBoundsException e) {
        }


        
    }
    
    private double fetch(CorpusDocument doc, Vocabulary vocab, String word) {
        return doc.getWord(vocab.getIndex(word));
    }
    
    @Test
    public void testEmptyCorpus() {
        CountingNGramCorpus corpus = new CountingNGramCorpus(2);
        corpus.addTokenizedDocument(createDocument(""));
        Vocabulary v = corpus.getVocabulary();
        assertEquals(0, v.size());
        List<CorpusDocument> docs = corpus.getDocuments();
        assertEquals(1, docs.size());
    }
    
    @Test
    public void testAddingDocAfterCompute() {
        String a = "This is my first sentence.";
        String b = "This is my second sentence.";
        Corpus corpus = new CountingNGramCorpus(2);
        corpus.addTokenizedDocument(createDocument(a));
        corpus.getDocuments();
        try {
            corpus.addTokenizedDocument(createDocument(b));
            fail();
        } catch (UnsupportedOperationException e) { 
            
        }
                
    }
    
    private PreprocessedTextDocument createDocument(String text) {
        TextDocument doc = new TextDocument(UUID.randomUUID().toString(), text);
        return pipeline.apply(doc);
    }
}
