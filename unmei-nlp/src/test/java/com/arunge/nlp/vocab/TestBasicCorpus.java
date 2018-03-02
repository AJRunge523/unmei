package com.arunge.nlp.vocab;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.CorpusDocument;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureWeightType;
import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.api.Vocabulary;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.TextDocument;

public class TestBasicCorpus {

    static private NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
    
    @Test
    public void testSingle() { 
        String a = "The dog picked up the bone. The dog then went and buried the bone.";
        Corpus corpus = new BasicCorpus();
        TextDocument doc = new TextDocument("a", a);
        PreprocessedTextDocument preprocessed = pipeline.apply(doc);
        corpus.addTokenizedDocument(preprocessed);        
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(10));
        assertThat(corpus.getDocuments().size(), equalTo(1));
        CorpusDocument d = corpus.getDocuments().get(0);
        assertThat(d.getLength(), equalTo(16));
        Map<Integer, Double> docVocab = d.getVocab();
        List<String> words = v.getVocabWords();
        assertThat(words, hasItem("the"));
        assertThat(docVocab.get(v.getIndex("the")), equalTo(4.0));
        assertThat(words, hasItem("dog"));
        assertThat(docVocab.get(v.getIndex("dog")), equalTo(2.0));
        assertThat(words, hasItem("picked"));
        assertThat(docVocab.get(v.getIndex("picked")), equalTo(1.0));
        assertThat(words, hasItem("up"));
        assertThat(docVocab.get(v.getIndex("up")), equalTo(1.0));
        assertThat(words, hasItem("bone"));
        assertThat(docVocab.get(v.getIndex("bone")), equalTo(2.0));
        assertThat(words, hasItem("then"));
        assertThat(docVocab.get(v.getIndex("then")), equalTo(1.0));
        assertThat(words, hasItem("went"));
        assertThat(docVocab.get(v.getIndex("went")), equalTo(1.0));
        assertThat(words, hasItem("and"));
        assertThat(docVocab.get(v.getIndex("and")), equalTo(1.0));
        assertThat(words, hasItem("buried"));
        assertThat(docVocab.get(v.getIndex("buried")), equalTo(1.0));

    }
    
    @Test
    public void multiDocTest() {
        String a = "This is a simple test.";
        String b = "This is a different kind of test.";
        String c = "This is this is a this";
        Corpus corpus = new BasicCorpus();
        corpus.addTokenizedDocument(createDocument(a));
        corpus.addTokenizedDocument(createDocument(b));
        corpus.addTokenizedDocument(createDocument(c));
        
        Vocabulary v = corpus.getVocabulary();
        assertThat(v.size(), equalTo(9));
        assertThat(corpus.getDocuments().size(), equalTo(3));
        
    }
    
    @Test
    public void testEmptyDoc() {
        Corpus corpus = new BasicCorpus();
        corpus.addTokenizedDocument(createDocument(""));
        Vocabulary v = corpus.getVocabulary();
        assertEquals(0, v.size());
    }
    
    @Test
    public void testFeatures() {
        Corpus corpus = new BasicCorpus();
        PreprocessedTextDocument a = createDocument("This is a test");
        a.addFeature(FeatureDescriptor.of("constant"), 5.0);
        PreprocessedTextDocument b = createDocument("This is another test");
        b.addFeature(new FeatureDescriptor("tfidf", FeatureWeightType.TFIDF), 3.0);
        PreprocessedTextDocument c = createDocument("This is not a test");
        c.addFeature(new FeatureDescriptor("tfidf", FeatureWeightType.TFIDF), 6.0);
        corpus.addTokenizedDocument(a);
        corpus.addTokenizedDocument(b);
        corpus.addTokenizedDocument(c);
        corpus.finalize();
        CorpusDocument docA = corpus.getDocuments().get(0);
        assertTrue(docA.getFeature(0).isPresent());
        assertEquals(docA.getFeature(0).get(), 5.0, 0.00001);
        CorpusDocument docB = corpus.getDocuments().get(1);
        assertTrue(!docB.getFeature(0).isPresent());
        assertTrue(docB.getFeature(1).isPresent());
        assertEquals(docB.getFeature(1).get(), 3.0 * Math.log(3.0 / 2.0), 0.00001);
        CorpusDocument docC = corpus.getDocuments().get(2);
        assertTrue(!docC.getFeature(0).isPresent());
        assertTrue(docC.getFeature(1).isPresent());
        assertEquals(docC.getFeature(1).get(), 6.0 * Math.log(3.0 / 2.0), 0.00001);
        
    }
    
    private PreprocessedTextDocument createDocument(String text) {
        TextDocument doc = new TextDocument(UUID.randomUUID().toString(), text);
        return pipeline.apply(doc);
    }
}
