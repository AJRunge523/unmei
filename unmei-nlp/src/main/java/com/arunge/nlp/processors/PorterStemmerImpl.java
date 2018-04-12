package com.arunge.nlp.processors;

import java.util.List;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.Stemmer;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;

import opennlp.tools.stemmer.PorterStemmer;

public class PorterStemmerImpl implements Stemmer {

    private PorterStemmer stemmer;
    
    public PorterStemmerImpl() {
        this.stemmer = new PorterStemmer();
    }
    
    @Override
    public void tag(List<AnnotatedToken> sentence) {
        for(AnnotatedToken tok : sentence) { 
            String stem = stemmer.stem(tok.text());
            tok.addAnnotation(Annotator.STEM, stem);
        }
    }

    @Override
    public void tag(AnnotatedTextDocument doc) {
        for(AnnotatedTextField field : doc.getTextFields().values()) {
            tag(field);
        }
    }

    @Override
    public void tag(AnnotatedTextField field) {
        for(List<AnnotatedToken> sentence : field.getSentences()) {
            tag(sentence);
        }
    }

}
