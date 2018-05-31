package com.arunge.nlp.corpus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.api.TokenForms;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.TextDocument;
import com.arunge.nlp.vocab.NGramIndexer;
import com.arunge.nlp.vocab.Vocabulary;

public class TestTfidfNgramCorpus {

    static private NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
    
    @Test
    public void testSingle() { 
        String a = "The dog picked up the bone. The dog then went and buried the bone.";
        TfIdfNgramCorpus corpus = new TfIdfNgramCorpus(2);
        corpus.setTokenFormExtraction(TokenForms.lowercase());
        corpus.addTokenizedDocument(createDocument(a));
        NGramIndexer indexer = corpus.getNgramIndexer();
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(11));
        assertThat(corpus.getDocuments().size(), equalTo(1));
        NGramCorpusDocument d = (NGramCorpusDocument) corpus.getDocuments().get(0);
        assertThat(d.getLength(), equalTo(16));
        Map<Integer, Double> docVocab = d.getVocab();
        List<String> words = v.getVocabWords();
        assertThat(words, hasItem("the"));
        assertThat(words, hasItem("dog"));
        assertThat(words, hasItem("picked"));
        assertThat(words, hasItem("up"));
        assertThat(words, hasItem("bone"));
        assertThat(words, hasItem("then"));
        assertThat(words, hasItem("went"));
        assertThat(words, hasItem("and"));
        assertThat(words, hasItem("buried"));
        for(String w : words) {
            System.out.println(w + " --> " + indexer.getIndex(w));
            if(w.equals("<DUMMY>")) {
                assertNull(docVocab.get(indexer.getIndex(w)));
            } else {
                assertThat(docVocab.get(indexer.getIndex(w)), equalTo(0.0));
            }
        }
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

    }
    
    @Test
    public void multiDocTest() {
        String a = "This is the simple test the test.";
        String b = "This is a different kind of test.";
        String c = "This is this is a this a kind";
        TfIdfNgramCorpus corpus = new TfIdfNgramCorpus(2, TFType.RAW);
        corpus.setTokenFormExtraction(TokenForms.lowercase());
        corpus.addTokenizedDocument(createDocument(a));
        corpus.addTokenizedDocument(createDocument(b));
        corpus.addTokenizedDocument(createDocument(c));
        
        Vocabulary v = corpus.getVocabulary();
        NGramIndexer indexer = corpus.getNgramIndexer();
        assertThat(v.size(), equalTo(11));
        assertThat(corpus.getDocuments().size(), equalTo(3));
        CorpusDocument docA = corpus.getDocuments().get(0);
        CorpusDocument docB = corpus.getDocuments().get(1);
        CorpusDocument docC = corpus.getDocuments().get(2);
        List<String> words = v.getVocabWords();
        assertThat(words, hasItem("this"));
        assertThat(fetch(docA, indexer, "this"), equalTo(0.0));
        assertThat(fetch(docB, indexer, "this"), equalTo(0.0));
        assertThat(fetch(docC, indexer, "this"), equalTo(0.0));
        assertThat(words, hasItem("is"));
        assertThat(fetch(docA, indexer, "is"), equalTo(0.0));
        assertThat(fetch(docB, indexer, "is"), equalTo(0.0));
        assertThat(fetch(docC, indexer, "is"), equalTo(0.0));
        assertThat(words, hasItem("the"));
        assertThat(fetch(docA, indexer, "the"), equalTo(2 * Math.log(3.0)));
        assertThat(fetch(docB, indexer, "the"), equalTo(0.0));
        assertThat(fetch(docC, indexer, "the"), equalTo(0.0));
        assertThat(words, hasItem("a"));
        assertThat(fetch(docA, indexer, "a"), equalTo(0.0));
        assertThat(fetch(docB, indexer, "a"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(docC, indexer, "a"), equalTo(2 * Math.log(3/2.0)));
        assertThat(words, hasItem("simple"));
        assertThat(fetch(docA, indexer, "simple"), equalTo(1 * Math.log(3.0)));
        assertThat(fetch(docB, indexer, "simple"), equalTo(0.0));
        assertThat(fetch(docC, indexer, "simple"), equalTo(0.0));
        assertThat(words, hasItem("different"));
        assertThat(fetch(docA, indexer, "different"), equalTo(0.0));
        assertThat(fetch(docB, indexer, "different"), equalTo(1 * Math.log(3.0)));
        assertThat(fetch(docC, indexer, "different"), equalTo(0.0));
        assertThat(words, hasItem("kind"));
        assertThat(fetch(docA, indexer, "kind"), equalTo(0.0));
        assertThat(fetch(docB, indexer, "kind"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(docC, indexer, "kind"), equalTo(1 * Math.log(3/2.0)));
        assertThat(words, hasItem("of"));
        assertThat(fetch(docA, indexer, "of"), equalTo(0.0));
        assertThat(fetch(docB, indexer, "of"), equalTo(1 * Math.log(3.0/1.0)));
        assertThat(fetch(docC, indexer, "of"), equalTo(0.0));
        assertThat(words, hasItem("test"));
        assertThat(fetch(docA, indexer, "test"), equalTo(2 * Math.log(3/2.0)));
        assertThat(fetch(docB, indexer, "test"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(docC, indexer, "test"), equalTo(0.0));
        
        
    }
    
    private double fetch(CorpusDocument doc, NGramIndexer indexer, String word) {
        return doc.getWord(indexer.getIndex(word));
    }
    
    @Test
    public void testEmptyCorpus() {
        TfIdfNgramCorpus corpus = new TfIdfNgramCorpus(2);
        corpus.addTokenizedDocument(createDocument(""));
        Vocabulary v = corpus.getVocabulary();
        //Always a single dummy word in the vocabulary of an ngram corpus.
        assertEquals(1, v.size());
        List<CorpusDocument> docs = corpus.getDocuments();
        assertEquals(1, docs.size());
    }
    
    @Test
    public void testAddingDocAfterCompute() {
        String a = "This is my first sentence.";
        String b = "This is my second sentence.";
        Corpus corpus = new TfIdfNgramCorpus(2);
        corpus.addTokenizedDocument(createDocument(a));
        corpus.getDocuments();
        try {
            corpus.addTokenizedDocument(createDocument(b));
            fail();
        } catch (UnsupportedOperationException e) { 
            
        }
                
    }
    
    @Test
    public void testTrimCorpus() { 
        String a = "This is my first sentence this is my friend.";
        String b = "This is your second good sentence that good day.";
        String c = "That was a good sentence that.";
        TfIdfNgramCorpus corpus = new TfIdfNgramCorpus(2, TFType.RAW);
        corpus.setTokenFormExtraction(TokenForms.lowercase());
        corpus.addTokenizedDocument(createDocument(a));
        corpus.addTokenizedDocument(createDocument(b));
        corpus.addTokenizedDocument(createDocument(c));
        corpus.trimTail(0, 2);
        Vocabulary v = corpus.getVocabulary();
        NGramIndexer indexer = corpus.getNgramIndexer();
        assertThat(v.size(), equalTo(7));
        assertThat(corpus.getDocuments().size(), equalTo(3));
        NGramCorpusDocument d = (NGramCorpusDocument) corpus.getDocuments().get(0);
        assertThat(d.getLength(), equalTo(6));
        List<String> words = v.getVocabWords();
        assertThat(words, hasItem("this"));
        assertThat(fetch(d, indexer, "this"), equalTo(2 * Math.log(3.0/2.0)));
        assertThat(words, hasItem("is"));
        assertThat(fetch(d, indexer, "is"), equalTo(2 * Math.log(3.0/2.0)));
        assertThat(words, not(hasItem("my")));
        assertThat(words, not(hasItem("first")));
        assertThat(words, hasItem("sentence"));
        assertThat(fetch(d, indexer, "sentence"), equalTo(0.0));
        assertThat(words, not(hasItem("friend")));
        assertThat(words, hasItem("."));
        assertThat(fetch(d, indexer, "."), equalTo(0.0));
        assertThat(d.getNgramCount(corpus.indexer.getIndex("this", "is"), 2), equalTo(2 * Math.log(3.0/2.0)));
        
        
        d = (NGramCorpusDocument) corpus.getDocuments().get(1);
        assertThat(d.getLength(), equalTo(7));
        assertThat(fetch(d, indexer, "that"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(d, indexer, "this"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(words, hasItem("is"));
        assertThat(words, hasItem("good"));
        assertThat(fetch(d, indexer, "good"), equalTo(2 * Math.log(3.0/2.0)));
        assertThat(words, not(hasItem("your")));
        assertThat(words, not(hasItem("second")));
        assertThat(words, hasItem("sentence"));
        assertThat(words, hasItem("."));
        
        d = (NGramCorpusDocument) corpus.getDocuments().get(2);
        assertThat(d.getLength(), equalTo(5));
        assertThat(d.getNgrams(2).size(), equalTo(2));
        assertThat(words, hasItem("good"));
        
        assertThat(fetch(d, indexer, "good"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(d.getNgramCount(corpus.indexer.getIndex("good", "sentence"), 2), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(d.getNgramCount(corpus.indexer.getIndex("sentence", "that"), 2), equalTo(1 * Math.log(3.0/2.0)));
     
    }
    
    private AnnotatedTextDocument createDocument(String text) {
        TextDocument doc = new TextDocument(UUID.randomUUID().toString(), text);
        return pipeline.apply(doc);
    }
    
}
