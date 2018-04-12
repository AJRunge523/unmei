package com.arunge.nlp.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.FeatureDescriptor;

public class AnnotatedTextDocument {

    private String docId;
    private Optional<String> label;
    private Map<String, AnnotatedTextField> textFields;
    private Map<FeatureDescriptor, Double> features;
    
    public AnnotatedTextDocument(String docId) { 
        this.docId = docId;
        this.label = Optional.empty();
        this.textFields = new HashMap<>();
        this.features = new HashMap<>();
    }

    /**
     * Create a new <code>PreprocessedTextDocument</code> using a TextDocument to fill in the id, label, and features.
     * @param doc
     */
    public AnnotatedTextDocument(TextDocument doc) {
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

    public AnnotatedTextField getDefaultField() { 
        return textFields.get(TextDocument.DEFAULT_FIELD);
    }
    
    public AnnotatedTextField getField(String fieldName) {
        return textFields.get(fieldName);
    }
    
    public Map<String, AnnotatedTextField> getTextFields() {
        return textFields;
    }

    public Collection<String> getFieldNames() {
        return textFields.keySet();
    }
    
    public void addTextField(String fieldName, AnnotatedTextField field) {
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
    
    @Override
    public String toString() {
        return render();
    }
    
    public String render(Annotator...annotators) { 
        StringBuilder sb = new StringBuilder();
        for(String field : textFields.keySet()) {
            sb.append(field + "=========================================================\n");
            sb.append(textFields.get(field).render(annotators));
        }
        return sb.toString();
    }
    
}
