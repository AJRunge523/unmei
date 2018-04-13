package com.arunge.nlp.vocab;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.corpus.Corpus;
import com.arunge.nlp.corpus.CorpusDocument;
import com.arunge.nlp.corpus.TFType;
import com.arunge.nlp.corpus.TfIdfCorpus;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.TextDocument;

public class TestTfIdfCorpus {

    static private NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
    
    @Test
    public void testSingle() { 
        String a = "The dog picked up the bone. The dog then went and buried the bone.";
        Corpus corpus = new TfIdfCorpus();
        corpus.addTokenizedDocument(createDocument(a));
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(10));
        assertThat(corpus.getDocuments().size(), equalTo(1));
        CorpusDocument d = corpus.getDocuments().get(0);
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
            assertThat(docVocab.get(v.getIndex(w)), equalTo(0.0));
        }

    }
    
    @Test
    public void multiDocTest() {
        String a = "This is the simple test the test.";
        String b = "This is a different kind of test.";
        String c = "This is this is a this a kind";
        Corpus corpus = new TfIdfCorpus(TFType.RAW);
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
        assertThat(fetch(docA, v, "this"), equalTo(0.0));
        assertThat(fetch(docB, v, "this"), equalTo(0.0));
        assertThat(fetch(docC, v, "this"), equalTo(0.0));
        assertThat(words, hasItem("is"));
        assertThat(fetch(docA, v, "is"), equalTo(0.0));
        assertThat(fetch(docB, v, "is"), equalTo(0.0));
        assertThat(fetch(docC, v, "is"), equalTo(0.0));
        assertThat(words, hasItem("the"));
        assertThat(fetch(docA, v, "the"), equalTo(2 * Math.log(3.0)));
        assertThat(fetch(docB, v, "the"), equalTo(0.0));
        assertThat(fetch(docC, v, "the"), equalTo(0.0));
        assertThat(words, hasItem("a"));
        assertThat(fetch(docA, v, "a"), equalTo(0.0));
        assertThat(fetch(docB, v, "a"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(docC, v, "a"), equalTo(2 * Math.log(3/2.0)));
        assertThat(words, hasItem("simple"));
        assertThat(fetch(docA, v, "simple"), equalTo(1 * Math.log(3.0)));
        assertThat(fetch(docB, v, "simple"), equalTo(0.0));
        assertThat(fetch(docC, v, "simple"), equalTo(0.0));
        assertThat(words, hasItem("different"));
        assertThat(fetch(docA, v, "different"), equalTo(0.0));
        assertThat(fetch(docB, v, "different"), equalTo(1 * Math.log(3.0)));
        assertThat(fetch(docC, v, "different"), equalTo(0.0));
        assertThat(words, hasItem("kind"));
        assertThat(fetch(docA, v, "kind"), equalTo(0.0));
        assertThat(fetch(docB, v, "kind"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(docC, v, "kind"), equalTo(1 * Math.log(3/2.0)));
        assertThat(words, hasItem("of"));
        assertThat(fetch(docA, v, "of"), equalTo(0.0));
        assertThat(fetch(docB, v, "of"), equalTo(1 * Math.log(3.0/1.0)));
        assertThat(fetch(docC, v, "of"), equalTo(0.0));
        assertThat(words, hasItem("test"));
        assertThat(fetch(docA, v, "test"), equalTo(2 * Math.log(3/2.0)));
        assertThat(fetch(docB, v, "test"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(fetch(docC, v, "test"), equalTo(0.0));
        
        
    }
    
    private double fetch(CorpusDocument doc, Vocabulary vocab, String word) {
        return doc.getWord(vocab.getIndex(word));
    }
    
    @Test
    public void testEmptyCorpus() {
        TfIdfCorpus corpus = new TfIdfCorpus();
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
        Corpus corpus = new TfIdfCorpus();
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
        String a = "This is my first sentence my friend.";
        String b = "That is your second sentence that day.";
        String c = "That was a good sentence.";
        Corpus corpus = new TfIdfCorpus(TFType.RAW);
        corpus.addTokenizedDocument(createDocument(a));
        corpus.addTokenizedDocument(createDocument(b));
        corpus.addTokenizedDocument(createDocument(c));
        corpus.trimTail(2);
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(4));
        assertThat(corpus.getDocuments().size(), equalTo(3));
        CorpusDocument d = corpus.getDocuments().get(0);
        assertThat(d.getLength(), equalTo(3));
        List<String> words = v.getVocabWords();
        assertThat(words, not(hasItem("this")));
        assertThat(words, hasItem("is"));
        assertThat(fetch(d, v, "is"), equalTo(1 * Math.log(3.0/2.0)));
        assertThat(words, not(hasItem("my")));
        assertThat(words, not(hasItem("first")));
        assertThat(words, hasItem("sentence"));
        assertThat(fetch(d, v, "sentence"), equalTo(0.0));
        assertThat(words, not(hasItem("friend")));
        assertThat(words, hasItem("."));
        assertThat(fetch(d, v, "."), equalTo(0.0));
        
        d = corpus.getDocuments().get(1);
        assertThat(d.getLength(), equalTo(5));
        assertThat(words, hasItem("that"));
        assertThat(fetch(d, v, "that"), equalTo(2 * Math.log(3.0/2.0)));
        assertThat(words, hasItem("is"));
        assertThat(words, not(hasItem("your")));
        assertThat(words, not(hasItem("second")));
        assertThat(words, hasItem("sentence"));
        assertThat(words, hasItem("."));
     
    }
    
    private AnnotatedTextDocument createDocument(String text) {
        TextDocument doc = new TextDocument(UUID.randomUUID().toString(), text);
        return pipeline.apply(doc);
    }
    
}
