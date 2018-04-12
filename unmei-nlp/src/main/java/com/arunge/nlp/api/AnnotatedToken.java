package com.arunge.nlp.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AnnotatedToken extends Token{

    private Map<Annotator, String> annotations;
    
    public AnnotatedToken(String text, int start, int end) {
        super(text, start, end);
        this.annotations = new HashMap<>();
    }

    public AnnotatedToken(String text, int start, int end, Map<Annotator, String> annotations) { 
        super(text, start, end);
        this.annotations = annotations;
    }
    
    public void addAnnotation(Annotator annotator, String value) {
        this.annotations.put(annotator, value);
    }
    
    public Optional<String> getAnnotation(Annotator annotator) {
        return Optional.ofNullable(annotations.get(annotator));
    }
    
    public Map<Annotator, String> getAnnotations() {
        return annotations;
    }
    
    public boolean hasAnnotation(Annotator annotator) {
        return annotations.containsKey(annotator);
    }
    
    public String render(Annotator...annotators) {
        StringBuilder sb = new StringBuilder();
        if(annotators.length == 0) { 
            return super.toString();
        }
        sb.append(super.toString());
        sb.append(" {");
        for(Annotator a : annotators) {
            sb.append(a.name() + " : " + annotations.get(a) + ", ");
        }
        return sb.append("} ").toString();
    }
    
}
