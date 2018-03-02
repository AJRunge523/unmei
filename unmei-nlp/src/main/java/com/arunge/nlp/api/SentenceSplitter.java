package com.arunge.nlp.api;

import java.util.List;

public interface SentenceSplitter {

    public List<Range> split(String text);
    
    public List<Range> split(List<Token> tokens);
    
}
