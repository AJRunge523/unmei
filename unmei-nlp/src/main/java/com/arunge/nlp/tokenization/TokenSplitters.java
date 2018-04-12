package com.arunge.nlp.tokenization;

public class TokenSplitters {

    public static TokenSplitter hyphen() {
        return new TokenSplitter("-");
    }
    
    public static TokenSplitter slashes() {
        return new TokenSplitter("[\\/\\\\]");
    }
    
    public static TokenSplitter underscores() {
        return new TokenSplitter("_");
    }
}
