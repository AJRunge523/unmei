package com.arunge.nlp.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

/**
 * 
 *<p>A vocabulary for storing n-grams of arbitrary order.<p>
 *
 * @author Andrew Runge
 *
 */
public class NGramIndexer implements Serializable {

    private static final long serialVersionUID = 936146487908466113L;

    protected static String SEP = String.valueOf((char) 31);
    
    private static Logger LOG = LoggerFactory.getLogger(NGramIndexer.class);
    
    protected Long2IntOpenHashMap[] indexers;
    protected Vocabulary vocabulary;
    protected long[][] indexes2Keys;
    
    /**
     * Defines an NGramIndexer that will store n-grams up to the specified order
     * @param order
     */
    public NGramIndexer(int order) {
        if(order > 3) {
            throw new UnsupportedOperationException("Currently only support n-grams up to order 3");
        }
        this.indexers = new Long2IntOpenHashMap[order - 1];
        for(int i = 0; i < indexers.length; i++) {
            indexers[i] = new Long2IntOpenHashMap();
        }
        this.vocabulary = new Vocabulary();
        this.indexes2Keys = new long[order - 1][10];
    }
    
    public String[] getNgram(int index, int order) { 
        if(order == 1) {
            return new String[] { vocabulary.getWord(index) };
        }
        long ngramKey = indexes2Keys[order - 2][index];
        int[] indexedNgram = NgramKeyCompression.extractKey(ngramKey, order);
        return lookupWords(indexedNgram, false);
    }
    
    public int getOrder() {
        return indexes2Keys.length + 1;
    }
    
    public boolean contains(String...ngram) {
        return getIndex(ngram) != -1;
    }
    
    public Map<Long, Integer> getNgrams(int order) {
        if(order >= 2 && order <= getOrder()) { 
            return Collections.unmodifiableMap(indexers[order - 2]);
        }
        return new HashMap<>();
    }
    
    public int size(int order) {
        if(order == 1) {
            return vocabulary.size();
        } else if(order <= 0 || order > indexers.length + 1) {
            return -1;
        } else {
            return indexers[order - 2].size();
        }
    }
    
    /**
     * Returns the index of the n-gram, or -1 if the n-gram is not present in the indexer.
     * @param ngram
     * @return
     */
    public int getIndex(String...ngram) {
        int order = ngram.length;
        if(order == 1) {
            return vocabulary.getIndex(ngram[0]);
        } else if(order == 0 || order > indexers.length + 1){
            LOG.error("Invalid n-gram order. Order: {}, Max supported order: {}", ngram.length, indexers.length + 1);
            return -1;
        } else {
            int[] indexedNgram = lookupIndexes(ngram, false);
            long ngramKey = NgramKeyCompression.generateKey(indexedNgram);
            int retVal = indexers[order - 2].get(ngramKey);
            if(retVal == 0) {
                return -1;
            } else {
                return retVal;
            }
        }
    }
    
    /**
     * Retrieve the index for a given ngram key. Use 
     * @param ngramId
     * @param order
     * @return
     */
    public int getIndex(long ngramKey, int order) {
        if(order == 0 || order > indexers.length + 1) {
            LOG.error("Invalid n-gram order. Order: {}, Max supported order: {}", order, indexers.length + 1);
            return -1;
        } else {
            int retVal = indexers[order - 2].get(ngramKey);
            if(retVal == 0) { 
                return -1;
            } else {
                return retVal;
            }
        }
    }
    
    public Vocabulary getVocabulary() { 
        return vocabulary;
    }
    
    public int getOrAdd(String...ngram) {
        return getOrAdd(true, ngram);
    }
    
    /**
     * Adds an n-gram containing 1 <= n <= order words and returns an index that can be used for
     * retrieval. If the n-gram is already present in the indexer, the previously computed index
     * is returned instead.
     * @param ngram
     * @return
     */
    public int getOrAdd(boolean recursive, String... ngram) { 
        int order = ngram.length;
        if(order == 1) { 
            return vocabulary.getOrAdd(ngram[0]);
        } else if(order == 0 || order > indexers.length + 1) { 
            LOG.error("Unable to process n-gram. N-gram order: {}, Max order: {}", ngram.length, indexers.length + 1);
            return -1;
        } else {
            int[] indexedNgram = lookupIndexes(ngram, recursive);
            if(recursive && ngram.length > 2) {
                for(int gapSize = 2; gapSize < ngram.length; gapSize++) {
                    for(int i = 0; i <= ngram.length - gapSize; i++) {
                        addNgram(indexedNgram, i, i + gapSize);
                    }
                }
            }
            return addNgram(indexedNgram, 0, ngram.length);
        }
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
                index2Keys = Arrays.copyOf(index2Keys, (int) (index2Keys.length * 3.0/2));
            }
            index2Keys[index] = ngramKey;
        }
        return index;
    }
    
    private int[] lookupIndexes(String[] words, boolean addIfMiss) {
        int[] indexes = new int[words.length];
        for(int i = 0; i < words.length; i++ ) {
            if(addIfMiss) {
                indexes[i] = vocabulary.getOrAdd(words[i]);
            } else {
                indexes[i] = vocabulary.getIndex(words[i]);
            }
                
        }
        return indexes;
    }
    
    private String[] lookupWords(int[] indexes, boolean addIfMiss) { 
        String[] words = new String[indexes.length];
        for(int i = 0; i < indexes.length; i++) {
            words[i] = vocabulary.getWord(indexes[i]);
        }
        return words;
    }
    
    public void write(OutputStream out) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(out);
        stream.writeObject(this);
    }
    
    public void write(File f) throws IOException {
        write(new FileOutputStream(f));
    }
    
    public static NGramIndexer read(InputStream in) throws IOException {
        ObjectInputStream stream = new ObjectInputStream(in);
        try {
            return (NGramIndexer) stream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to deserialize vocabulary", e);
        }
    }
    
    public static NGramIndexer read(File f) throws IOException {
        return read(new FileInputStream(f));
    }
}
