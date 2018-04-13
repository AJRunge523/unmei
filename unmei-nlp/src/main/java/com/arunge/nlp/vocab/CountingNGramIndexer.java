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
    private int[][] docFreqVectors;
    private int[][] ngramFreqVectors;
    private long[] numNgrams;
    
    public CountingNGramIndexer(int order) {
        super(order);
        this.vocabulary = new CountingVocabulary();
        this.docFreqVectors = new int[order - 1][10];
        this.ngramFreqVectors = new int[order - 1][10];
        this.numNgrams = new long[order - 1];
    }

    public CountingNGramIndexer trimTail(int minInclusion) {
        CountingNGramIndexer copy = new CountingNGramIndexer(docFreqVectors.length + 1);
        copy.setNumDocs(getNumDocs());
        
        CountingVocabulary currentVocab = (CountingVocabulary) vocabulary;
        copy.vocabulary = currentVocab.trimTail(minInclusion);
        
        
        //Add all n-grams whose counts are above the threshold to the new indexer - maps these
        //n-grams to new indexes to shrink the amount of space needed for storage.
        for(int o = 2; o <= getOrder(); o++) {
            for(int i = 0; i < indexes2Keys[o - 2].length; i++) {
                if(getDocFrequency(i, o) >= minInclusion) {
                    
                    //Get the original ngram and add it to the new indexer
                    String[] ngram = getNgram(i, o);
                    int index = copy.getOrAdd(false, ngram);
                    copy.docFreqVectors[o - 2][index] = docFreqVectors[o - 2][i];//setDocFrequency(index, o, docFreqVectors[o - 2][i]);
                    copy.ngramFreqVectors[o - 2][index] = ngramFreqVectors[o - 2][i];
                    copy.numNgrams[o - 2] += ngramFreqVectors[o - 2][i];
                }
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
        int index = indexers[order - 2].get(ngramKey);
        if(index == 0) {
            indexers[order - 2].put(ngramKey,  indexers[order - 2].size() + 1);
            index = indexers[order - 2].size();
            long[] index2Keys = indexes2Keys[order - 2];
            if(index >= index2Keys.length) {
                indexes2Keys[order - 2] = Arrays.copyOf(index2Keys, (int) (index2Keys.length * 3.0/2));
                docFreqVectors[order - 2] = Arrays.copyOf(docFreqVectors[order - 2], (int) (docFreqVectors[order - 2].length * 3.0/2));
                ngramFreqVectors[order - 2] = Arrays.copyOf(ngramFreqVectors[order - 2], (int) (ngramFreqVectors[order - 2].length * 3.0/2));
            }
            indexes2Keys[order - 2][index] = ngramKey;
        }
        return index;
    }
    
    
    
    public void incrementDocFrequency(int index, int order) {
        validateOrder(order);
        if(order == 1) {
            ((CountingVocabulary) vocabulary).incrementDocFrequency(index);
        } else if(index >= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            docFreqVectors[order - 2][index] += 1;
        }
    }
    
    public int getDocFrequency(int index, int order) {
        validateOrder(order);
        if(order == 1) {
            return((CountingVocabulary) vocabulary).getDocFrequency(index);
        } else if(index >= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            return docFreqVectors[order - 2][index];
        }
        
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
            return docFreqVectors[order - 2][index];
        }
    }
    
    public double[][] computeIDFVector() {
        int numDocs = getNumDocs();
        if(numDocs == 0) {
            return new double[docFreqVectors.length][];
        }
        CountingVocabulary vocab = (CountingVocabulary) vocabulary;
        double[][] idfVectors = new double[docFreqVectors.length + 1][];
        idfVectors[0] = vocab.computeIDFVector();
        for(int o = 0; o < docFreqVectors.length; o++) {
            idfVectors[o + 1] = new double[docFreqVectors[o].length];
            for(int i = 0; i < docFreqVectors[o].length; i++) {
                if(docFreqVectors[o][i] == 0) {
                    idfVectors[o + 1][i] = 0;
                } else {
                    idfVectors[o + 1][i] = Math.log(numDocs / ((double) docFreqVectors[o][i]));
                }
            }
        }
        return idfVectors;
    }
    
    public int[][] getDocFrequencies() {
        return Arrays.copyOf(docFreqVectors, docFreqVectors.length);
    }
    
    public int getNumDocs() {
        return ((CountingVocabulary) vocabulary).getNumDocs();
    }

    public void incrementNumDocs() {
        ((CountingVocabulary) vocabulary).incrementNumDocs();
    }
    
    /**
     * Private method for managing number of docs in the vocab when copying/trimming the vocabulary.
     * @param numDocs
     */
    private void setNumDocs(int numDocs) {
        ((CountingVocabulary) vocabulary).setNumDocs(numDocs);
    }
    
    /**
     * Increment the frequency of the ngram with the provided index and order by 1. If the vocabulary is frozen,
     * this is a no-op. 
     * @param index
     */
    public void incrementNgramFrequency(int index, int order) {
        incrementNgramFrequency(index, order, 1);
    }
    
    /**
     * Increment the frequency of the ngram with the provided index and order by the provided amount. If the vocabulary is frozen,
     * this is a no-op. 
     * @param index
     */
    public void incrementNgramFrequency(int index, int order, int inc) { 
        validateOrder(order);
        if(frozen) {
            return;
        }
        if(order == 1) {
            ((CountingVocabulary) vocabulary).incrementWordFrequency(index); 
        } else if(index >= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            numNgrams[order - 2]+=inc;
            ngramFreqVectors[order - 2][index]+=inc;
        }
    }
    
    public long getNgramFrequency(int index, int order) {
        validateOrder(order);
        if(order == 1) {
            return ((CountingVocabulary) vocabulary).getWordFrequency(index);
        } else if(index>= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            return ngramFreqVectors[order - 2][index];
        }
    }
    
    public long getNgramFrequency(String...ngram) {
        int order = ngram.length;
        validateOrder(order);
        if(order == 1) {
            
            return ((CountingVocabulary) vocabulary).getWordFrequency(ngram[0]);
        } else {
            int index = getIndex(ngram);
            if(index == -1) {
                return 0;
            }
            return ngramFreqVectors[order - 2][index];
        }
        
    }
    
    /**
     * Return the number of ngram tokens indexed by the vocabulary.
     * @param order
     * @return
     */
    public long getNumNgrams(int order) {
        validateOrder(order);
        if(order == 1) {
            return ((CountingVocabulary) vocabulary).getNumTokens();
        }
        return numNgrams[order - 2];
    }
    
    private void validateOrder(int order) {
        if(order < 1 || order > indexes2Keys.length + 1) {
            throw new UnsupportedOperationException("Invalid order: " + order + ", Maximum supported order: " + (indexes2Keys.length + 1));
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
