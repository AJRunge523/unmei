package com.arunge.nlp.vocab;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

import com.arunge.nlp.api.NGramIndexer;
import com.arunge.nlp.api.NgramKeyCompression;

public class DFNGramIndexer extends NGramIndexer {

    private static final long serialVersionUID = 3798562177609286573L;
    private int[][] docFreqVectors;
    
    public DFNGramIndexer(int order) {
        super(order);
        this.vocabulary = new DFVocabulary();
        this.docFreqVectors = new int[order - 1][10];
    }

    public DFNGramIndexer trimTail(int minInclusion) {
        DFNGramIndexer copy = new DFNGramIndexer(docFreqVectors.length + 1);
        copy.setNumDocs(getNumDocs());
        
        DFVocabulary currentVocab = (DFVocabulary) vocabulary;
        copy.vocabulary = currentVocab.trimTail(minInclusion);
        
        //Add all n-grams whose counts are above the threshold to the new indexer - maps these
        //n-grams to new indexes to shrink the amount of space needed for storage.
        for(int o = 2; o <= getOrder(); o++) {
            for(int i = 0; i < indexes2Keys[o - 2].length; i++) {
                if(getDocFrequency(i, o) >= minInclusion) {
                    
                    //Get the original ngram and add it to the new indexer
                    String[] ngram = getNgram(i, o);
                    int index = copy.getOrAdd(false, ngram);
                    copy.setDocFrequency(index, o, docFreqVectors[o - 2][i]);
                }
            }
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
            }
            indexes2Keys[order - 2][index] = ngramKey;
        }
        return index;
    }
    
    
    
    public void incrementDocFrequency(int index, int order) {
        if(order < 1 || order > indexes2Keys.length + 1) {
            throw new UnsupportedOperationException("Invalid order: " + order + ", Maximum supported order: " + (indexes2Keys.length + 1));
        }
        
        if(order == 1) {
            ((DFVocabulary) vocabulary).incrementDocFrequency(index);
        } else if(index >= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            docFreqVectors[order - 2][index] += 1;
        }
    }
    
    public void setDocFrequency(int index, int order, int frequency) {
        if(order < 1 || order > indexes2Keys.length + 1) {
            throw new UnsupportedOperationException("Invalid order: " + order + ", Maximum supported order: " + (indexes2Keys.length + 1));
        }
        if(order == 1) {
            ((DFVocabulary) vocabulary).setDocFrequency(index, frequency);
        } else if(index >= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            docFreqVectors[order - 2][index] = frequency;
        }
    }
    
    public int getDocFrequency(int index, int order) {
        if(order < 1 || order > indexes2Keys.length + 1) {
            throw new UnsupportedOperationException("Invalid order: " + order + ", Maximum supported order: " + (indexes2Keys.length + 1));
        }
        if(order == 1) {
            return((DFVocabulary) vocabulary).getDocFrequency(index);
        } else if(index >= indexes2Keys[order - 2].length) {
            throw new IndexOutOfBoundsException(String.format("Index %d is out of bounds, current size: %d", index, indexes2Keys[order].length));
        } else {
            return docFreqVectors[order - 2][index];
        }
        
    }
    
    public double[][] computeIDFVector() {
        int numDocs = getNumDocs();
        if(numDocs == 0) {
            return new double[docFreqVectors.length][];
        }
        DFVocabulary vocab = (DFVocabulary) vocabulary;
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
        return ((DFVocabulary) vocabulary).getNumDocs();
    }

    public void setNumDocs(int numDocs) {
        ((DFVocabulary) vocabulary).setNumDocs(numDocs);
    }
    
    public void incrementNumDocs() {
        ((DFVocabulary) vocabulary).incrementNumDocs();
    }
    
    public static DFNGramIndexer read(InputStream in) throws IOException {
      ObjectInputStream stream = new ObjectInputStream(in);
      try {
          return (DFNGramIndexer) stream.readObject();
      } catch (ClassNotFoundException e) {
          throw new RuntimeException("Unable to deserialize vocabulary", e);
      }
  }
  
  public static DFNGramIndexer read(File f) throws IOException {
      return read(new FileInputStream(f));
  }
}
