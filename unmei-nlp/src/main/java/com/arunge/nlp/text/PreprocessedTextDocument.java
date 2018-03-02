package com.arunge.nlp.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.arunge.nlp.api.FeatureDescriptor;

public class PreprocessedTextDocument {

    private String docId;
    private Optional<String> label;
    private Map<String, PreprocessedTextField> textFields;
    private Map<FeatureDescriptor, Double> features;
    
    public PreprocessedTextDocument(String docId) { 
        this.docId = docId;
        this.label = Optional.empty();
        this.textFields = new HashMap<>();
        this.features = new HashMap<>();
    }

    /**
     * Create a new <code>PreprocessedTextDocument</code> using a TextDocument to fill in the id, label, and features.
     * @param doc
     */
    public PreprocessedTextDocument(TextDocument doc) {
        this.docId = doc.getId();
        this.label = doc.getLabel();
        if(doc instanceof FeatureTextDocument) {
            this.features = new HashMap<>(((FeatureTextDocument) doc).getFeatures());
        } else {
            this.features = new HashMap<>();
        }
        this.textFields = new HashMap<>();
    }
    
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Optional<String> getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = Optional.of(label);
    }

    public PreprocessedTextField getField(String fieldName) {
        return textFields.get(fieldName);
    }
    
    public Map<String, PreprocessedTextField> getTextFields() {
        return textFields;
    }

    public void addTextField(String fieldName, PreprocessedTextField field) {
        this.textFields.put(fieldName, field);
    }
    
    public Map<FeatureDescriptor, Double> getFeatures() {
        return features;
    }

    public void addFeatures(Map<FeatureDescriptor, Double> features) {
        this.features.putAll(features);
    }
    
    public void addFeature(FeatureDescriptor feature, double value) {
        this.features.put(feature, value);
    }
    
    public int getLength() { 
        return textFields.values().stream().map(f -> f.getLength()).reduce(0, (a, b) -> a + b);
    }
    
    
}
