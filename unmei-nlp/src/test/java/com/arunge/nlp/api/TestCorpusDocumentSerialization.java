package com.arunge.nlp.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.arunge.nlp.corpus.BasicCorpus;
import com.arunge.nlp.corpus.Corpus;
import com.arunge.nlp.corpus.CorpusDocument;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.FeatureTextDocument;

public class TestCorpusDocumentSerialization {

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        String a = "The dog picked up the bone. The dog then went and buried the bone.";
        Corpus corpus = new BasicCorpus();
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        features.put(FeatureDescriptor.of("feat1"), 1.0);
        features.put(FeatureDescriptor.of("f2"), -7.5);
        FeatureTextDocument doc = new FeatureTextDocument("a", a, features);
        NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
        AnnotatedTextDocument preprocessed = pipeline.apply(doc);
        corpus.addTokenizedDocument(preprocessed);
        CorpusDocument cd = corpus.getDocuments().get(0);
        System.out.println(cd.getNgramLength(1));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(baos);
        stream.writeObject(corpus);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Corpus deser = (Corpus) inStream.readObject();
        assertEquals(1, deser.size());
//        assertEquals(corpus.featureIndexer.size(), deser.featureIndexer.size());
        CorpusDocument decoded = deser.getDocuments().get(0);
        assertEquals(decoded.getLabel(), cd.getLabel());
        assertEquals(decoded.getDocId(), cd.getDocId());
        assertEquals(decoded.getNgramLength(1), cd.getNgramLength(1), 0.00001);
        Map<Integer, Double> origVocab = cd.getNgrams(1);
        Map<Integer, Double> decodedVocab = decoded.getNgrams(1);
        assertEquals(origVocab.size(), decodedVocab.size());
        for(Entry<Integer, Double> entry : decodedVocab.entrySet()) {
            assertTrue(origVocab.containsKey(entry.getKey()));
            assertEquals(origVocab.get(entry.getKey()), entry.getValue());
        }
        Map<Integer, Double> origFeatures = cd.getFeatures();
        Map<Integer, Double> decodedFeatures = decoded.getFeatures();
        assertEquals(origFeatures.size(), decodedFeatures.size());
        for(Entry<Integer, Double> entry : decodedFeatures.entrySet()) {
            assertTrue(origFeatures.containsKey(entry.getKey()));
            assertEquals(origFeatures.get(entry.getKey()), entry.getValue());
        }
    }
    
}
