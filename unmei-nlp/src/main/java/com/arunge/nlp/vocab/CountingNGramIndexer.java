package com.arunge.nlp.vocab;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

import com.arunge.nlp.api.NgramKeyCompression;

public class CountingNGramIndexer extends NGramIndexer {

    private static final long serialVersionUID = 3798562177609286573L;
    private int[] docFreqVectors;
    private int[] ngramFreqVectors;
    private long[] numNgrams;
    private int numDocs = 0;
    
    public CountingNGramIndexer(int order) {
        super(order);
        this.docFreqVectors = new int[10];
        this.ngramFreqVectors = new int[10];
        this.numNgrams = new long[order];
    }

    public CountingNGramIndexer trimTail(int minCount, int minDocs) {
        CountingNGramIndexer copy = new CountingNGramIndexer(this.order);
        copy.setNumDocs(getNumDocs());
        
        //Add all n-grams whose counts are above the threshold to the new indexer - maps these
        //n-grams to new indexes to shrink the amount of space needed for storage.
        for(int i = 1; i < index2Keys.length; i++) {
            if(getNgramFrequency(i) >= minCount && getDocFrequency(i) >= minDocs) {
                
                //Get the original ngram and add it to the new indexer
                String[] ngram = getNgram(i);
                int index = copy.getOrAdd(false, ngram);
                System.out.println(Arrays.stream(ngram).reduce("", (a, b) -> a + " " + b) + " --> " + index);
                copy.docFreqVectors[index] = docFreqVectors[i];
                copy.ngramFreqVectors[index] = ngramFreqVectors[i];
                copy.numNgrams[ngram.length - 1] += ngramFreqVectors[i];
            }
        }
        if(frozen) {
            copy.freezeVocab();
        }
        return copy;
    }
    
    protected int addNgram(int[] indexedNgram, int from, int to) {
        int order = to - from;
        long ngramKey = NgramKeyCompression.generateKey(indexedNgram, from, to);
        int index = indexers[order - 1].get(ngramKey);
        if(index == 0 && !frozen) {
            this.size += 1;
            index = this.size;
            indexers[order - 1].put(ngramKey, index);
            if(index >= index2Keys.length) {
                index2Keys = Arrays.copyOf(index2Keys, (int) (index2Keys.length * 3.0/2));
                docFreqVectors = Arrays.copyOf(docFreqVectors, (int) (docFreqVectors.length * 3.0/2));
                ngramFreqVectors = Arrays.copyOf(ngramFreqVectors, (int) (ngramFreqVectors.length * 3.0/2));
            }
            index2Keys[index] = ngramKey;
            return index;
        } else if(index == 0) { 
            return -1;
        } else {
            return index;
        }
    }
    
    public void incrementDocFrequency(int index) {
        if(index < 0 || index >= index2Keys.length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Keys.length));
        }
        docFreqVectors[index] += 1;
    }
    
    public int getDocFrequency(int index) {
        if(index < 0 || index >= index2Keys.length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Keys.length));
        } 
        return docFreqVectors[index];
    }
    
    public int getDocFrequency(String...ngram) {
        int order = ngram.length;
        validateOrder(order);
        if(order == 1) {
            
            return (int) ((CountingVocabulary) vocabulary).getDocFrequency(ngram[0]);
        } else {
            int index = getIndex(ngram);
            if(index == -1) {
                return 0;
            }
            return docFreqVectors[index];
        }
    }
    
    public double[] computeIDFVector() {
        int numDocs = getNumDocs();
        if(numDocs == 0) {
            return new double[0];
        }
        double[] idfVectors = new double[docFreqVectors.length];
        for(int i = 0; i < docFreqVectors.length; i++) {
            if(docFreqVectors[i] == 0) {
                idfVectors[i] = 0;
            } else {
                idfVectors[i] = Math.log(numDocs / ((double) docFreqVectors[i]));
            }
        }
        return idfVectors;
    }
    
    public int[] getDocFrequencies() {
        return Arrays.copyOf(docFreqVectors, docFreqVectors.length);
    }
    
    public int getNumDocs() {
        return this.numDocs;
    }

    public void incrementNumDocs() {
        this.numDocs += 1;
    }
    
    /**
     * Private method for managing number of docs in the vocab when copying/trimming the vocabulary.
     * @param numDocs
     */
    private void setNumDocs(int numDocs) {
        this.numDocs = numDocs;
    }
    
    /**
     * Increment the frequency of the ngram with the provided index and order by 1. If the vocabulary is frozen,
     * this is a no-op. 
     * @param index
     */
    public void incrementNgramFrequency(int index) {
        incrementNgramFrequency(index, 1);
    }
    
    /**
     * Increment the frequency of the ngram with the provided index and order by the provided amount. If the vocabulary is frozen,
     * this is a no-op. 
     * @param index
     */
    public void incrementNgramFrequency(int index, int inc) { 
        if(frozen) {
            return;
        }
        if (index < 0 || index >= index2Keys.length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Keys.length));
        } else {
            String[] ngram = getNgram(index);
            numNgrams[ngram.length - 1]+=inc;
            ngramFreqVectors[index]+=inc;
        }
    }
    
    public long getNgramFrequency(int index) {
        if(index >= index2Keys.length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, index2Keys.length));
        } else {
            return ngramFreqVectors[index];
        }
    }
    
    public long getNgramFrequency(String...ngram) {
        int order = ngram.length;
        validateOrder(order);
        int index = getIndex(ngram);
        if(index == -1) {
            return 0;
        }
        return ngramFreqVectors[index];
    }
    
    /**
     * Return the number of ngram tokens indexed by the vocabulary.
     * @param order
     * @return
     */
    public long getNumNgrams(int order) {
        validateOrder(order);
        return numNgrams[order - 1];
    }
    
    private void validateOrder(int order) {
        if(order < 1 || order > this.order + 1) {
            throw new UnsupportedOperationException("Invalid order: " + order + ", Maximum supported order: " + (this.order + 1));
        }
    }

    public static CountingNGramIndexer read(InputStream in) throws IOException {
        ObjectInputStream stream = new ObjectInputStream(in);
        try {
            return (CountingNGramIndexer) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to deserialize vocabulary", e);
        }
    }

    public static CountingNGramIndexer read(File f) throws IOException {
        return read(new FileInputStream(f));
    }
}
