package com.arunge.nlp.vocab;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

import com.arunge.nlp.api.Vocabulary;

/**
 * 
 *<p>A vocabulary that supports counting word occurrences. Words can be counted both based on # of occurrences in the corpus
 *   and # of documents the word occurs in in the corpus.<p>
 *
 * @author Andrew Runge
 *
 */
public class CountingVocabulary extends Vocabulary {

    private static final long serialVersionUID = 4216302255003553294L;
    private int[] wordFreqVector;
    private int[] docFreqVector;
    private int numDocs;
    
    public CountingVocabulary() {
        super();
        this.docFreqVector = new int[10];
        this.wordFreqVector = new int[10];
    }
    
    public CountingVocabulary(int initSize) {
        super(initSize);
        this.docFreqVector = new int[initSize];
        this.wordFreqVector = new int[initSize];
    }
    
    public CountingVocabulary(CountingVocabulary other) {
        super(other);
        this.docFreqVector = Arrays.copyOf(other.docFreqVector, other.docFreqVector.length);
        this.wordFreqVector = Arrays.copyOf(other.wordFreqVector, other.wordFreqVector.length);
        this.numDocs = other.numDocs;
    }
    
    @Override
    public int getOrAdd(String word) {
        int wordIndex = super.getOrAdd(word);
        if(docFreqVector.length <= index2Word.size()) { 
            docFreqVector = Arrays.copyOf(docFreqVector, docFreqVector.length * 3 / 2);
        }
        if(wordFreqVector.length <= index2Word.size()) {
            wordFreqVector = Arrays.copyOf(wordFreqVector,  wordFreqVector.length * 3 / 2);
        }
        return wordIndex;
    }
    
    public void incrementDocFrequency(int index) {
        if(frozen) {
            return;
        }
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
    
    public void incrementWordFrequency(int index) {
        if(frozen) {
            return;
        }
        if(index >= index2Word.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Word.size()));
        }
        wordFreqVector[index] += 1;
    }
    
    public void setWordFrequency(int index, int frequency) {
        if(index >= index2Word.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Word.size()));
        }
        wordFreqVector[index] = frequency;
    }
    
    public int getWordFrequency(int index) {
        if(index >= index2Word.size()) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Word.size()));
        }
        return wordFreqVector[index];
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
    
    public int[] getWordFrequencies() {
        return Arrays.copyOf(wordFreqVector, wordFreqVector.length);
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
     * Creates a copy of this vocabulary with all terms below a certain document frequency threshold removed.
     * All remaining terms are remapped to new indices.
     * @param minInclusion
     * @return
     */
    public CountingVocabulary trimTail(int minInclusion) {
        CountingVocabulary newVocab = new CountingVocabulary();
        newVocab.setNumDocs(numDocs);
        for(int i = 0; i < vocab.size(); i++) {
            if(docFreqVector[i] >= minInclusion) {
                int newIndex = newVocab.getOrAdd(index2Word.get(i));
                newVocab.setDocFrequency(newIndex, docFreqVector[i]);
            }
        }
        if(frozen) {
            newVocab.freezeVocab();
        }
        return newVocab;
    }
    
    public static CountingVocabulary read(File w) throws IOException {
        return read(new FileInputStream(w));
    }
    
    public static CountingVocabulary read(InputStream in) throws IOException {
        ObjectInputStream stream = new ObjectInputStream(in);
        try {
            return (CountingVocabulary) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to deserialize vocabulary", e);
        }
    }
    
}
