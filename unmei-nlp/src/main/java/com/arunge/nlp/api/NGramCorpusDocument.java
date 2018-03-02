package com.arunge.nlp.api;

import java.util.Collections;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class NGramCorpusDocument extends CorpusDocument {

    private static final long serialVersionUID = 446866277115911309L;
    private Int2DoubleOpenHashMap[] docNgrams;
    private int[] ngramLengths;
    
    public NGramCorpusDocument(String docId, int order) {
        super(docId);
        this.docNgrams = new Int2DoubleOpenHashMap[order - 1];
        for(int i = 0; i < docNgrams.length; i++) {
            docNgrams[i] = new Int2DoubleOpenHashMap();
        }
        this.ngramLengths = new int[order - 1];
        // TODO Auto-generated constructor stub
    }

    public boolean addOrIncrementNgram(int ngramIndex, int order) {
        return addOrIncrementNgram(ngramIndex, order, 1.0);
    }
    
    public boolean addOrIncrementNgram(int ngramIndex, int order, double count) {
        if(order == 1) {
            return addOrIncrementWord( ngramIndex);
        }
        int orderIndex = order - 2;
//        ngramLengths[orderIndex] += count;
        double result = docNgrams[orderIndex].merge(ngramIndex, count, (a, b) -> a + b);
        if(result > count) { 
            return false;
        }
        return true;
    }
    
    public void setNgramLength(int length, int order) {
        ngramLengths[order - 2] = length;
    }
    
    public int getNgramLength(int order) { 
        if(order <= 0 || order > getOrder()) {
            return -1;
        }
        if(order == 1) {
            return getLength();
        } else {
            return ngramLengths[order - 2];
        }
    }
    
    public int getOrder() {
        return docNgrams.length + 1;
    }
    
    public double getNgramCount(int ngramIndex, int order) {
        if(order == 1) {
            return getWord(ngramIndex);
        }
        return docNgrams[order - 2].get(ngramIndex);
    }
    
    public Map<Integer, Double> getNgrams(int order){ 
        if(order == 1) {
            return getVocab();
        }
        return Collections.unmodifiableMap(docNgrams[order - 2]);
    }
    
    public void setNgramCount(int ngramIndex, int order, double count) { 
        if(order == 1) { 
            setWordCount(ngramIndex, count);
        } else {
            docNgrams[order - 2].put(ngramIndex, count);
        }
    }
    
    @Override
    public void buildLengthNormCountDoc() { 
        super.buildLengthNormCountDoc();
        for(int i = 0; i < docNgrams.length; i++) {
            int index = i;
            docNgrams[i].replaceAll((key, value) -> value / ngramLengths[index]);
        }
    }
    
    @Override
    public void buildLogLengthNormCountDoc() { 
        super.buildLogLengthNormCountDoc();
        for(int i = 0; i < docNgrams.length; i++) {
            int index = i;
            docNgrams[i].replaceAll((key, value) -> 1 + Math.log(value / ngramLengths[index]));
        }
    }
    
}
