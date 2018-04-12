package com.arunge.nlp.api;

import java.util.List;

import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;


public interface POSTagger {

    void tag(List<AnnotatedToken> sentence);
    
    void tag(AnnotatedTextDocument doc);
    
    public void tag(AnnotatedTextField field);
    
}
