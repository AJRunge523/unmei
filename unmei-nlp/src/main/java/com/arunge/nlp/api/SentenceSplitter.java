package com.arunge.nlp.api;

import java.util.List;

public interface SentenceSplitter {

    public List<List<Token>> split(String text);
    
    public List<List<Token>> split(List<Token> tokens);
    
}
