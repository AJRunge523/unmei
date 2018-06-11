package com.arunge.nlp.weka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.arunge.nlp.corpus.Corpus;
import com.arunge.nlp.corpus.CorpusDocument;
import com.arunge.nlp.vocab.CountingNGramIndexer;
import com.arunge.nlp.vocab.NGramIndexer;
import com.arunge.nlp.vocab.Vocabulary;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.SparseInstance;

public class WekaCorpusConverters {

    /**
     * Create a list of {@link Attribute}s from a {@link Vocabulary} for use in creating instances from a {@link Corpus}.
     * @param vocab The corpus <code>Vocabulary</code> containing all words that should be turned into <code>Attribute</code>s.
     * @return
     */
    public static ArrayList<Attribute> createVocabAttributes(Vocabulary vocab){ 
        ArrayList<Attribute> attributes = new ArrayList<>();
        for(int i = 0; i < vocab.size(); i++) {
            if(vocab.getWord(i) != null) {
                Attribute attr = new Attribute(vocab.getWord(i));
                attributes.add(attr);
            }
        }
        return attributes;
    }
    
    public static ArrayList<Attribute> createNgramVocabAttributes(NGramIndexer indexer) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        for(int i = 0; i < indexer.size(); i++) {
            String[] key = indexer.getNgram(i);
            String label = Arrays.stream(key).reduce("",  (a, b) -> {
                if(a.isEmpty()) {
                    return b;
                } else if(b.isEmpty()) {
                    return a;
                } else {
                    return a + "_" + b;
                }
            });
            attributes.add(new Attribute(label));
        }
        return attributes;
    }
        
    /**
     * Create a Weka {@link Instance} from a {@link CorpusDocument}. If the document specifies a label, it will be added to the end of the attribute list.
     * @param doc The document to process
     * @param totalVocabAttrs The total number of vocabulary attributes for all n-gram orders
     * @param indexer The n-gram indexer
     * @param vocabAttrIndices Map of ngram order --> list of vocab attributes
     * @param attributes Complete list of all attributes.
     * @return
     */
    public static Instance convert(CorpusDocument doc, int totalVocabAttrs, NGramIndexer indexer, 
            List<Attribute> attributes, boolean includeId) { 
        double[] values = new double[attributes.size()];

        for(int i = 1; i <= doc.getOrder(); i++) {
            Map<Integer, Double> docNgrams = doc.getNgrams(i);
            for(Integer k : docNgrams.keySet()) {
                values[k] = docNgrams.get(k);
            }
        }
        Map<Integer, Double> docFeats = doc.getFeatures();
        for(int featIndex : docFeats.keySet()) {
            values[totalVocabAttrs + featIndex] = docFeats.get(featIndex);
        }
        Instance instance = new SparseInstance(1.0, values);
        if(includeId) { 
            instance.setValue(attributes.get(attributes.size() - 2), doc.getDocId());
        }
        if(doc.getLabel() != null && !doc.getLabel().isEmpty()) {
            instance.setValue(attributes.get(attributes.size() - 1), doc.getLabel());
        }
        return instance;
    }
    
}
