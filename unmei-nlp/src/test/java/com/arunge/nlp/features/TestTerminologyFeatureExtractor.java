package com.arunge.nlp.features;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.TextDocument;

public class TestTerminologyFeatureExtractor {

    @Test
    public void testExtractor() {
        String text = "Neural networks are the future of all machine learning techniques";
        NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline(Annotator.POS, Annotator.LEMMA);
        AnnotatedTextDocument doc = pipeline.apply(new TextDocument("a", text));
        TerminologyFeatureExtractor extractor = new TerminologyFeatureExtractor("src/test/resources/ai_terminology.txt");
        Map<FeatureDescriptor, Double> features = extractor.extractFeatures(doc);
        assertEquals(features.get(FeatureDescriptor.of("term_machine_learning")).doubleValue(), 0.1, 0.0001);
        assertEquals(features.get(FeatureDescriptor.of("term_neural_network")).doubleValue(), 0.1, 0.0001);
        
    }
    
    @Test
    public void testSplits() { 
        String field1 = "Neural networks are the future of all natural language processing";
        String field2 = "Machine learning still uses many other techniques, however.";
        NLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline(Annotator.POS, Annotator.LEMMA, Annotator.SEGMENT);
        TextDocument d = new TextDocument("a");
        d.setTextField("field1", field1);
        d.setTextField("field2", field2);
        AnnotatedTextDocument doc = pipeline.apply(d);
        TerminologyFeatureExtractor extractor = new TerminologyFeatureExtractor("src/test/resources/ai_terminology.txt");
        Map<FeatureDescriptor, Double> features = extractor.extractFeatures(doc);
        assertEquals(features.size(), 2);
        for(FeatureDescriptor key : features.keySet()) {
            System.out.println(key);
        }
        assertEquals(features.get(FeatureDescriptor.of("field2_term_machine_learning")).doubleValue(), 0.05, 0.0001);
        assertEquals(features.get(FeatureDescriptor.of("field1_term_neural_network")).doubleValue(), 0.05, 0.0001);
    }

}
