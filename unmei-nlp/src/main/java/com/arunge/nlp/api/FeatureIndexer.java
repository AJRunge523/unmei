package com.arunge.nlp.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class FeatureIndexer implements Serializable, Iterable<Map.Entry<FeatureDescriptor, Integer>>{

    private static final long serialVersionUID = 2356851576642458045L;
    private Map<FeatureDescriptor, Integer> indexes;
    private Int2DoubleOpenHashMap weights;
    private List<FeatureDescriptor> feats;
    
    public FeatureIndexer() {
        this.indexes = new HashMap<>();
        this.weights = new Int2DoubleOpenHashMap();
        this.feats = new ArrayList<>();
    }
    
    public int getOrAdd(String featureName) {
        return getOrAdd(FeatureDescriptor.of(featureName));
    }
 
    public int getOrAdd(FeatureDescriptor desc) {
        if(!indexes.containsKey(desc)) {
            int index = indexes.size();
            indexes.put(desc, index);
            weights.put(index, 1.0);
            feats.add(desc);
        }
        return indexes.get(desc);
    }
    
    public int getIndex(String featureName) {
        return getIndex(FeatureDescriptor.of(featureName));
    }

    public int getIndex(FeatureDescriptor desc) { 
        if(indexes.containsKey(desc)) {
            return indexes.get(desc);
        } else {
            return -1;
        }
    }
    
    public FeatureDescriptor getFeature(int index) { 
        return feats.get(index);
    }
    
    public double getWeight(int featIndex) { 
        return weights.get(featIndex);
    }
    
    public void setWeight(int featIndex, double weight) {
        weights.put(featIndex, weight);
    }

    public int size() {
        return indexes.size();
    }
    
    @Override
    public Iterator<Entry<FeatureDescriptor, Integer>> iterator() {
        return indexes.entrySet().iterator();
    }
}
