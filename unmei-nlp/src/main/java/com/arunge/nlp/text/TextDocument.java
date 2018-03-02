package com.arunge.nlp.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TextDocument {

    private String id;
    private List<String> textFields;
    private Map<String, Integer> fieldNames;
    private Optional<String> label;
    public static String DEFAULT_FIELD = "<DEFAULT>";
    
    public TextDocument(String id) {
        this.id = id;
        this.textFields = new ArrayList<>();
        this.fieldNames = new HashMap<>();
        this.label = Optional.empty();
    }
    
    /**
     * Creates a <code>TextDocument</code> with the provided text stored
     * in the default text field.
     * @param id
     * @param text
     */
    public TextDocument(String id, String text) {
        this.id = id;
        this.textFields = new ArrayList<>();
        this.textFields.add(text);
        this.fieldNames = new HashMap<>();
        fieldNames.put(DEFAULT_FIELD, 0);
        this.label = Optional.empty();
    }
    
    public TextDocument(String id, String text, String label) {
        this.id = id;
        this.textFields = new ArrayList<>();
        this.textFields.add(text);
        this.fieldNames = new HashMap<>();
        fieldNames.put(DEFAULT_FIELD, 0);
        this.label = Optional.of(label);
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Returns the full, combined text of all fields in the document.
     * Fields are combined in the order in which they were added to the document.
     * @return
     */
    public String getText() {
        return textFields.stream().reduce((a, b) -> a + "\n" + b).get();
    }
    
    public Map<String, String> getTextFields() {
        Map<String, String> fields = new HashMap<>();
        for(String key : fieldNames.keySet()) {
            fields.put(key, textFields.get(fieldNames.get(key)));
        }
        return fields;
    }
    
    /**
     * Combine all text fields into a single field, stored with the default name. 
     */
    public void combineFields() {
        String text = getText();
        fieldNames.clear();
        textFields.clear();
        fieldNames.put(DEFAULT_FIELD, 0);
        textFields.add(text);
    }
    
    /**
     * Set the value of default text field.
     * @param text
     */
    public void setText(String text) {
        this.textFields.add(0, text);
    }
    
    /**
     * If the specified field name exists in the document, 
     * it will be overwritten. Otherwise, it will be added
     * to the document
     * @param fieldName
     * @param text
     */
    public void setTextField(String fieldName, String text) { 
        if(fieldNames.containsKey(fieldName)) {
            this.textFields.set(fieldNames.get(fieldName), text);
        } else {
            this.textFields.add(text);
            this.fieldNames.put(fieldName, this.textFields.size() - 1);
        }
    }
    
    /**
     * Retrieve a list of the text fields in the document.
     * @return
     */
    public List<String> getFieldNames() {
        return new ArrayList<>(fieldNames.keySet());
    }
    
    /**
     * Remove all text associated with the given field name from the document.
     * @param fieldName
     */
    public void clearTextField(String fieldName) {
        if(fieldNames.containsKey(fieldName)) {
            int pos = fieldNames.get(fieldName);
            textFields.set(pos, "");
            fieldNames.remove(fieldName);
        }
    }
    
    /**
     * Get the text content of the specified field.
     * @param fieldName
     * @return
     */
    public String getTextField(String fieldName) { 
        if(fieldNames.containsKey(fieldName)) {
            return this.textFields.get(fieldNames.get(fieldName));
        }
        return "";
    }
    
    public Optional<String> getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = Optional.of(label);
    }
}
