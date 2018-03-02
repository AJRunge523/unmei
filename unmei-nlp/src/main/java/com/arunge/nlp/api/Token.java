package com.arunge.nlp.api;

/**
 * 
 *<p>Basic class to represent a String token<p>
 *
 * @author Andrew Runge
 *
 */
public class Token {

    private String text;
    private Range range;

    public Token(String text, int start, int end) {
        this.text = text;
        this.range = new Range(start, end);
    }
    
    public Token(String text, Range range) {
        this.text = text;
        this.range = range;
    }
    
    public String text() {
        return text;
    }
    
    public int start() {
        return range.getStartOffset();
    }
    
    public int end() {
        return range.getEndOffset();
    }
    
    public Range range() {
        return range;
    }
    
    @Override
    public String toString() {
        return text + " " + range;
    }
}
