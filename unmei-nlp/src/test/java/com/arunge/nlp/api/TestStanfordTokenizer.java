package com.arunge.nlp.api;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.arunge.nlp.stanford.StanfordSentenceSplitter;
import com.arunge.nlp.stanford.StanfordTokenizer;

public class TestStanfordTokenizer {

    @Test
    public void test1() {
        StanfordSentenceSplitter splitter = new StanfordSentenceSplitter();
        splitter.split("This is my first sentence. Now, Dr. Smith, here is the second sentence. What about the third one? \"This is the fourth one\", he said.");
    }
    
    @Test
    public void test2() { 
        StanfordTokenizer tokenizer = new StanfordTokenizer();
        List<Token> tokens = tokenizer.tokenize("Dr. Merriam-Webster decided to visit www.google.com instead of calling her usual pharmacy at 703-724-0499. \"Where will I find the medicine I'm looking for\", she wondered. ")
                .collect(Collectors.toList());
        for(Token t : tokens) {
            System.out.println(t);
        }
            
    }
    
}
