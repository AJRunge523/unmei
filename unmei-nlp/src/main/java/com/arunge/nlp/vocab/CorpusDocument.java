package com.arunge.nlp.vocab;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class CorpusDocument implements Serializable {

    private static final long serialVersionUID = 3021118049524358838L;

    private final String docId;

    private Int2DoubleOpenHashMap docVocab;
    private int length;
    private String label;
    private Int2DoubleOpenHashMap docFeatures;
    
    public CorpusDocument(String docId) {
        this.docId = docId;
        this.docVocab = new Int2DoubleOpenHashMap();
        this.length = 0;
        this.docFeatures = new Int2DoubleOpenHashMap();
    }

    protected CorpusDocument(CorpusDocument doc) {
        this.docId = doc.docId;
        this.docVocab = new Int2DoubleOpenHashMap(doc.docVocab);
        this.length = doc.length;
        this.docFeatures = new Int2DoubleOpenHashMap(doc.docFeatures);
    }
    
    /**
     * Adds the word to the document or, if already present, increments the count for the word instead.
     * Returns true if the word was added, false otherwise.
     * @param wordIndex
     * @return
     */
    public boolean addOrIncrementWord(int wordIndex) {
        return addOrIncrementWord(wordIndex, 1.0);
    }
    
    public boolean addOrIncrementWord(int wordIndex, double count) {
//        length += count;
        double result = docVocab.merge(wordIndex, count, (a, b) -> a + b);
        if(result > count) {
            return false;
        }
        return true;
    }
    
    public void setWordCount(int wordIndex, double count) {
        docVocab.put(wordIndex, count);
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    public Map<Integer, Double> getVocab(){ 
        return Collections.unmodifiableMap(docVocab);
    }
    
    public Map<Integer, Double> getFeatures() {
        return Collections.unmodifiableMap(docFeatures);
    }
    
    public double getWord(int index) { 
        
        if(docVocab.containsKey(index)) {
            return docVocab.get(index);
        } else {
            return 0.0;
        }
    }
    
    public CorpusDocument buildLengthNormCountDoc() {
        CorpusDocument copy = new CorpusDocument(this);
        copy.docVocab.replaceAll((key, value) -> value / length);
        return copy;
    }
    
    public CorpusDocument buildLogLengthNormCountDoc() { 
        CorpusDocument copy = new CorpusDocument(this);
        copy.docVocab.replaceAll((key, value) -> 1 + Math.log(value / length));
        return copy;
    }
    
    public void addFeature(int index, double value) {
        this.docFeatures.put(index, value);
    }
    
    public String getDocId() {
        return docId;
    }
    
    public int getLength() {
        return length;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public Optional<Double> getFeature(int feature) {
        if(docFeatures.containsKey(feature)) {
            return Optional.of(docFeatures.get(feature));
        } else {
            return Optional.empty();
        }
    }
    
    public void setFeature(int feature, double value) {
        this.docFeatures.put(feature, value);
    }
    
    public void setFeatures(Map<Integer, Double> features) {
        this.docFeatures = new Int2DoubleOpenHashMap(features);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((docFeatures == null) ? 0 : docFeatures.hashCode());
        result = prime * result + ((docId == null) ? 0 : docId.hashCode());
        result = prime * result + ((docVocab == null) ? 0 : docVocab.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + length;
        return result;
    }

}
