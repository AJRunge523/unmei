package com.arunge.nlp.api;

public class Range {

    private int start;
    private int end;
    
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public int getStartOffset() {
        return start;
    }
    
    public int getEndOffset() { 
        return end;
    }
    
    public void setStartOffset(int start) {
        this.start = start;
    }
    
    public void setEndOffset(int end) { 
        this.end = end;
    }
    
    public boolean intersects(Range other) { 
        return (other.start <= this.start && other.end >= this.start) || (other.start <= this.end && other.end >= this.end);
    }
    
    public String toString() { 
        return "(" + start + ", " + end + ")";
    }
}
