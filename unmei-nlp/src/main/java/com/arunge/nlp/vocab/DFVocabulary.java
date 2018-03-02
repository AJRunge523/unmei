package com.arunge.nlp.vocab;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

import com.arunge.nlp.api.Vocabulary;

public class DFVocabulary extends Vocabulary {

    private static final long serialVersionUID = 4216302255003553294L;
    private int[] docFreqVector;
    private int numDocs;
    
    public DFVocabulary() {
        super();
        this.docFreqVector = new int[10];
    }
    
    public DFVocabulary(int initSize) {
        super(initSize);
        this.docFreqVector = new int[initSize];
    }
    
    public DFVocabulary(DFVocabulary other) {
        super(other);
        this.docFreqVector = Arrays.copyOf(other.docFreqVector, other.docFreqVector.length);
        this.numDocs = other.numDocs;
    }
    
    @Override
    public int getOrAdd(String word) {
        if(!vocab.containsKey(word)) {
            vocab.put(word, index2Word.size());
            index2Word.add(word);
            if(docFreqVector.length <= index2Word.size()) { 
                docFreqVector = Arrays.copyOf(docFreqVector, docFreqVector.length * 3 / 2);
            }
        }
        return vocab.get(word);
    }
    
    public void incrementDocFrequency(int index) {
        if(index >= index2Word.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Word.size()));
        }
        docFreqVector[index] += 1;
    }
    
    public void setDocFrequency(int index, int frequency) {
        if(index >= index2Word.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Word.size()));
        }
        docFreqVector[index] = frequency;
    }
    
    public int getDocFrequency(int index) {
        if(index >= index2Word.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Word.size()));
        }
        return docFreqVector[index];
    }
    
    public double[] computeIDFVector() {
        if(numDocs == 0) {
            return new double[docFreqVector.length];
        }
        double[] idfVector = new double[docFreqVector.length];
        for(int i = 0; i < docFreqVector.length; i++) {
            if(docFreqVector[i] == 0) {
                idfVector[i] = 0;
            } else {
                idfVector[i] = Math.log(numDocs / ((double) docFreqVector[i]));
            }
        }
        return idfVector;
    }
    
    public int[] getDocFrequencies() {
        return Arrays.copyOf(docFreqVector, docFreqVector.length);
    }
    
    public int getNumDocs() {
        return numDocs;
    }

    public void setNumDocs(int numDocs) {
        this.numDocs = numDocs;
    }
    
    public void incrementNumDocs() {
        this.numDocs += 1;
    }

    /**
     * Creates a copy of this vocabulary with all terms below a certain frequency threshold removed.
     * All remaining terms are remapped to new indices.
     * @param minInclusion
     * @return
     */
    public DFVocabulary trimTail(int minInclusion) {
        DFVocabulary newVocab = new DFVocabulary();
        newVocab.setNumDocs(numDocs);
        for(int i = 0; i < vocab.size(); i++) {
            if(docFreqVector[i] >= minInclusion) {
                int newIndex = newVocab.getOrAdd(index2Word.get(i));
                newVocab.setDocFrequency(newIndex, docFreqVector[i]);
            }
        }
        return newVocab;
    }
    
    public static DFVocabulary read(File w) throws IOException {
        return read(new FileInputStream(w));
    }
    
    public static DFVocabulary read(InputStream in) throws IOException {
        ObjectInputStream stream = new ObjectInputStream(in);
        try {
            return (DFVocabulary) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to deserialize vocabulary", e);
        }
    }
    
}
