package com.arunge.nlp.text;

import java.util.HashMap;
import java.util.Map;

import com.arunge.nlp.api.FeatureDescriptor;

/**
 * 
 *<p>Feature-enriched text document.<p>
 *
 * @author Andrew Runge
 *
 */
public class FeatureTextDocument extends TextDocument {

    private Map<FeatureDescriptor, Double> features;
    
    public FeatureTextDocument(String id, String text) {
        this(id, text, new HashMap<>());
    }
    
    public FeatureTextDocument(String id, String text, Map<FeatureDescriptor, Double> features) {
        super(id, text);
        this.features = features;
    }
    
    public FeatureTextDocument(String id, String text, String label, Map<FeatureDescriptor, Double> features) {
        super(id, text, label);
        this.features = features;
    }
    
    /**
     * Wrapper to convert a regular {@link TextDocument} into a <code>FeatureTextDocument</code>
     * @param doc
     * @param features
     */
    public FeatureTextDocument(TextDocument doc, Map<FeatureDescriptor, Double> features) {
        super(doc.getId(), "");
        for(String field : doc.getFieldNames()) {
            setTextField(field, doc.getTextField(field));
        }
        this.features = features;
        if(doc.getLabel().isPresent()) {
            this.setLabel(doc.getLabel().get());
        }
    }
    
    public Map<FeatureDescriptor, Double> getFeatures() {
        return features;
    }

    public void setFeatures(Map<FeatureDescriptor, Double> features) {
        this.features = features;
    }
    
    public void addFeature(FeatureDescriptor feat, double value) {
        this.features.put(feat, value);
    }
    
}
