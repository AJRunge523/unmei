package com.arunge.nlp.corpus;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * 
 *<p>A <code>CorpusDocument</code> is an indexed representation of a text document. The text of the document
 *   is represented using a bag-of-words model, where each word (or n-gram) is simply represented as an index
 *   in an external vocabulary and a value. The value may be a count, or it may be transformed by an external
 *   application to be something such as a weight for the term for this document.<p>
 *
 * @author Andrew Runge
 *
 */
public class CorpusDocument implements Serializable {

    private static final long serialVersionUID = 446866277115911309L;
    private Int2DoubleOpenHashMap[] docNgrams;

    private final String docId;
    private String label;
    private Int2DoubleOpenHashMap docFeatures;
    
    /**
     * Create a 
     * @param docId
     */
    public CorpusDocument(String docId) {
        this(docId, 1);
    }
    
    public CorpusDocument(String docId, int order) {
        this.docId = docId;
        this.docNgrams = new Int2DoubleOpenHashMap[order];
        for(int i = 0; i < docNgrams.length; i++) {
            docNgrams[i] = new Int2DoubleOpenHashMap();
        }
        this.docFeatures = new Int2DoubleOpenHashMap();
    }

    public CorpusDocument(CorpusDocument copy) { 
        this.docId = copy.docId;
        this.docNgrams = Arrays.copyOf(copy.docNgrams, copy.docNgrams.length);
        this.docFeatures = new Int2DoubleOpenHashMap(copy.docFeatures);
        this.label = copy.label;
    }
    
    public boolean addOrIncrementNgram(int ngramIndex, int order) {
        return addOrIncrementNgram(ngramIndex, order, 1.0);
    }
    
    public boolean addOrIncrementNgram(int ngramIndex, int order, double count) {
        int orderIndex = order - 1;
        double result = docNgrams[orderIndex].merge(ngramIndex, count, (a, b) -> a + b);
        if(result > count) { 
            return false;
        }
        return true;
    }
    
    public String getDocId() {
        return docId;
    }
    
    /**
     * Returns the sum of the occurrence counts for all n-grams of the specified order.
     * 
     * Note that if the counts have been modified, this may not reflect the true length of the document.
     * @param order
     * @return
     */
    public double getNgramLength(int order) { 
        if(order <= 0 || order > getOrder()) {
            throw new IndexOutOfBoundsException(String.format("Invalid n-gram order: %d, expected 1 <= input <= %d", order, getOrder()));
        }
        return docNgrams[order - 1].values().stream().reduce(0.0, (a, b) -> a + b);
    }
    
    public int getOrder() {
        return docNgrams.length;
    }
    
    /**
     * Retrieve the value associated with this ngram index.
     * @param ngramIndex
     * @param order
     * @return
     */
    public double getNgramValue(int ngramIndex, int order) {
        return docNgrams[order - 1].get(ngramIndex);
    }
    
    public Map<Integer, Double> getNgrams(int order){ 
        return Collections.unmodifiableMap(docNgrams[order - 1]);
    }
    
    /**
     * Update the value associated with this ngram index.
     * @param ngramIndex
     * @param order
     * @return
     */
    public void setNgramValue(int ngramIndex, int order, double count) { 
        docNgrams[order - 1].put(ngramIndex, count);
    }
    
    public Map<Integer, Double> getFeatures() {
        return Collections.unmodifiableMap(docFeatures);
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
       
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public CorpusDocument buildLengthNormCountDoc() { 
        CorpusDocument copy = new CorpusDocument(this);
        for(int i = 0; i < docNgrams.length; i++) {
            double length = getNgramLength(i + 1);
            copy.docNgrams[i].replaceAll((key, value) -> value / length);
        }
        return copy;
    }
    
    public CorpusDocument buildLogLengthNormCountDoc() { 
        CorpusDocument copy = new CorpusDocument(this);
        for(int i = 0; i < docNgrams.length; i++) {
            double length = getNgramLength(i + 1);
            copy.docNgrams[i].replaceAll((key, value) -> 1 + Math.log(value / length));
        }
        return copy;
    }
    
    
    
    
}
