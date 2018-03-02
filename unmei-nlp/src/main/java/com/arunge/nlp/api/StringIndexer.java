package com.arunge.nlp.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

public class StringIndexer implements Iterable<Map.Entry<String, Integer>>, Serializable {

    private static final long serialVersionUID = -8690097609915330833L;
    protected Map<String, Integer> vocab;
    protected List<String> index2Word;
    
    public StringIndexer() {
        this.vocab = new HashMap<>();
        this.index2Word = new ArrayList<>();
    }
    
    public StringIndexer(int initSize) {
        this.vocab = new HashMap<>(initSize);
        this.index2Word = new ArrayList<>(initSize);
    }
    
    public StringIndexer(StringIndexer other) {
        this.vocab = new HashMap<>(other.vocab);
        this.index2Word = new ArrayList<>(other.index2Word);
    }
    
    public String getWord(int index) {
        return index2Word.get(index);
    }
    
    public int getOrAdd(String word) {
        if(!vocab.containsKey(word)) {
            vocab.put(word, index2Word.size());
            index2Word.add(word);
        }
        return vocab.get(word);
    }
 
    public int getIndex(String word) {
        if(vocab.containsKey(word)) {
            return vocab.get(word);
        } else {
            return -1;
        }
    }
    
    public int size() {
        return vocab.size();
    }
    
    public Map<String, Integer> getIndexes() {
        return ImmutableMap.copyOf(vocab);
    }

    @Override
    public Iterator<Entry<String, Integer>> iterator() {
        return vocab.entrySet().iterator();
    }
}
