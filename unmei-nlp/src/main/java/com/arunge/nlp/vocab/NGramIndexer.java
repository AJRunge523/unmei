package com.arunge.nlp.vocab;

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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.nlp.api.NgramKeyCompression;

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
    protected long[] index2Keys;
    protected boolean frozen;
    protected int order;
    protected int size;
    
    /**
     * Defines an NGramIndexer that will store n-grams up to the specified order
     * @param order
     */
    public NGramIndexer(int order) {
        this.order = order;
        if(order > 3) {
            throw new UnsupportedOperationException("Currently only support n-grams up to order 3");
        }
        this.indexers = new Long2IntOpenHashMap[order];
        for(int i = 0; i < indexers.length; i++) {
            indexers[i] = new Long2IntOpenHashMap();
        }
        this.vocabulary = new Vocabulary();
        this.vocabulary.getOrAdd("<DUMMY>");
        this.index2Keys = new long[10];
        this.frozen = false;
    }
    
    public String[] getNgram(int index) { 
        long ngramKey = index2Keys[index];
        int[] indexedNgram = NgramKeyCompression.extractKey(ngramKey);
        return lookupWords(indexedNgram, false);
    }
    
    public int getOrder() {
        return this.order;
    }
    
    public boolean contains(String...ngram) {
        return getIndex(ngram) != -1;
    }
    
    public int size() {
        return this.size + 1;
    }
    
    public int size(int order) {
        return indexers[order - 1].size();
    }
    
    public Map<Long, Integer> getNgrams(int order) { 
        return indexers[order - 1];
    }
    
    public int getIndex(List<String> ngram) { 
        if(ngram == null) {
            return -1;
        }
        int order = ngram.size();
        if(order == 0 || order > this.order) {
            LOG.error("Invalid n-gram order. Order: {}, Max supported order: {}", order, this.order);
            return -1;
        }
        return getIndex(lookupIndexes(ngram, false));
    }
    
    /**
     * Returns the index of the n-gram, or -1 if the n-gram is not present in the indexer.
     * @param ngram
     * @return
     */
    public int getIndex(String...ngram) {
        if(ngram == null) {
            return -1;
        }
        int order = ngram.length;
        if(order == 0 || order > this.order){
            LOG.error("Invalid n-gram order. Order: {}, Max supported order: {}", ngram.length, this.order);
            return -1;
        } else {
            return getIndex(lookupIndexes(ngram, false));
        }
    }
    
    /**
     * Retrieve the index for the provided ngram, where each word in the ngram has already been indexed.
     * 
     * This method is provided to provide potential speed ups in cases where n-grams are being retrieved
     * along a sliding window to allow for caching previously retrieved indices.
     * @param ngram
     * @return
     */
    public int getIndex(int...ngram) {
        if(ngram == null) {
            return -1;
        }
        int order = ngram.length;
        if(order == 0 || order > this.order){
            LOG.error("Invalid n-gram order. Order: {}, Max supported order: {}", ngram.length, this.order);
            return -1;
        } else {
            long ngramKey = NgramKeyCompression.generateKey(ngram);
            int retVal = indexers[order - 1].get(ngramKey);
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
        if(order == 0 || order > this.order) {
            LOG.error("Invalid n-gram order. Order: {}, Max supported order: {}", order, this.order);
            return -1;
        } else {
            int retVal = indexers[order - 1].get(ngramKey);
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
     * @param recursive - Whether or not to add lower order n-grams present in the provided ngram
     * if they are missing from the indexer.
     * @param ngram
     * @return
     */
    public int getOrAdd(boolean recursive, String... ngram) { 
        int order = ngram.length;
        if(order == 0 || order > this.order) { 
            LOG.error("Unable to process n-gram. N-gram order: {}, Max order: {}", ngram.length, this.order);
            return -1;
        } else {
            int[] indexedNgram = lookupIndexes(ngram, !this.frozen);
            if(recursive) {
                for(int gapSize = 1; gapSize < ngram.length; gapSize++) {
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
        int index = indexers[order - 1].get(ngramKey);
        if(index == 0 && !frozen) {
            this.size += 1;
            index = this.size;
            indexers[order - 1].put(ngramKey, index);
            if(index >= index2Keys.length) {
                index2Keys = Arrays.copyOf(index2Keys, (int) (index2Keys.length * 3.0/2));
            }
            index2Keys[index] = ngramKey;
            return index;
        } else if(index == 0) { 
            return -1;
        } else {
            return index;
        }
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
    
    private int[] lookupIndexes(List<String> words, boolean addIfMiss) { 
        int[] indexes = new int[words.size()];
        int i = 0;
        for(String word : words) {
            if(addIfMiss) {
                indexes[i] = vocabulary.getOrAdd(word);
            } else {
                indexes[i] = vocabulary.getIndex(word);
            }
            i+=1;
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
    
    /**
     * 
     * @return
     */
    public boolean isFrozen() {
        return frozen;
    }
    
    /**
     * Freeze the contents of the vocabulary, preventing further items from being added.
     */
    public void freezeVocab() {
        this.vocabulary.freezeVocab();
        this.frozen = true;
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
