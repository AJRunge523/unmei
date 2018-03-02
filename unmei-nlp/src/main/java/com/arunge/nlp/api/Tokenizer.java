package com.arunge.nlp.api;

import java.util.List;
import java.util.stream.Stream;

public interface Tokenizer {

    public Stream<Token> tokenize(String text);
    
    public List<Token> tokenizeToList(String text);
    
}
