package com.arunge.nlp.weka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.CorpusDocument;
import com.arunge.nlp.api.NGramCorpusDocument;
import com.arunge.nlp.api.NGramIndexer;
import com.arunge.nlp.api.Vocabulary;
import com.arunge.nlp.vocab.DFNGramIndexer;

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
    
    public static Map<Integer, ArrayList<Attribute>> createNgramVocabAttributes(NGramIndexer indexer) {
        Map<Integer, ArrayList<Attribute>> ngramAttributes = new HashMap<>();
        ArrayList<Attribute> attributes = new ArrayList<>();
        Vocabulary vocab = indexer.getVocabulary();
        for(int i = 0; i < vocab.size(); i++) {
            if(vocab.getWord(i) != null) {
                Attribute attr = new Attribute(vocab.getWord(i));
                attributes.add(attr);
            }
        }
        ngramAttributes.put(1, attributes);
        for(int o = 2; o <= indexer.getOrder(); o++) {
            ngramAttributes.put(o, new ArrayList<Attribute>());
            attributes = ngramAttributes.get(o);
            for(int i = 1; i < indexer.size(o); i++) {
                String[] key = indexer.getNgram(i, o);
                String label = Arrays.stream(key).reduce("", (a, b) -> {
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
        }
        return ngramAttributes;
    }
    
    /**
     * Create a Weka {@link Instance} from a {@link CorpusDocument}. If the document specifies a label, it will be added to the end of the attribute list.
     * @param doc The <code>CorpusDocument</code> to convert.
     * @param vocabSize The size of the global vocabulary
     * @param attributes The set of attributes extracted from a <code>Vocabulary</code>
     * @return
     */
    public static Instance convert(CorpusDocument doc, int vocabSize, List<Attribute> attributes, boolean includeId) {
        
        Map<Integer, Double> docVocab = doc.getVocab();
        double[] values = new double[attributes.size()];
        
        for(int wordIndex : docVocab.keySet()) {
            values[wordIndex] = docVocab.get(wordIndex);
        }
        Map<Integer, Double> docFeats = doc.getFeatures();
        for(int featIndex : docFeats.keySet()) {
            values[vocabSize + featIndex] = docFeats.get(featIndex);
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
    
    /**
     * Create a Weka {@link Instance} from a {@link NGramCorpusDocument}. If the document specifies a label, it will be added to the end of the attribute list.
     * @param doc The document to process
     * @param totalVocabAttrs The total number of vocabulary attributes for all n-gram orders
     * @param indexer The n-gram indexer
     * @param vocabAttrIndices Map of ngram order --> list of vocab attributes
     * @param attributes Complete list of all attributes.
     * @return
     */
    public static Instance convert(NGramCorpusDocument doc, int totalVocabAttrs, DFNGramIndexer indexer, 
            Map<Integer, ArrayList<Attribute>> vocabAttrIndices, List<Attribute> attributes, boolean includeId) { 
        double[] values = new double[attributes.size()];

        int offset = 0;
        for(int i = 1; i <= doc.getOrder(); i++) {
            Map<Integer, Double> docNgrams = doc.getNgrams(i);
            for(Integer k : docNgrams.keySet()) {
//                System.out.println(attributes.get(k + offset).name() + ": " + indexer.getNgram(k, i)[0] + " --> " + docNgrams.get(k));
                values[k + offset] = docNgrams.get(k);
            }
            offset += vocabAttrIndices.get(i).size() - 1;
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
