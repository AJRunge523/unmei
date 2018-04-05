package com.arunge.nlp.api;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;

public class TestTokenSplitter {

    @Test
    public void testHyphenSplit() {
        Token t = new Token("test-token", 0, 10);
        TokenSplitter split = TokenSplitters.hyphen();
        Token[] splitTokArr = split.apply(t).toArray(Token[]::new);
        assertEquals(3, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "test");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 4);
        assertEquals(splitTokArr[1].text(), "-");
        assertEquals(splitTokArr[1].start(), 4);
        assertEquals(splitTokArr[1].end(), 5);
        assertEquals(splitTokArr[2].text(), "token");
        assertEquals(splitTokArr[2].start(), 5);
        assertEquals(splitTokArr[2].end(), 10);
        
        Token t2 = new Token("multi-hyphen-word-test", 0, 22);
        splitTokArr = split.apply(t2).toArray(Token[]::new);
        assertEquals(7, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "multi");
        assertEquals(splitTokArr[1].text(), "-");
        assertEquals(splitTokArr[2].text(), "hyphen");
        assertEquals(splitTokArr[3].text(), "-");
        assertEquals(splitTokArr[4].text(), "word");
        assertEquals(splitTokArr[5].text(), "-");
        assertEquals(splitTokArr[6].text(), "test");
        
        Token t3 = new Token("-start", 0, 6);
        splitTokArr = split.apply(t3).toArray(Token[]::new);
        assertEquals(2, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "-");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 1);
        assertEquals(splitTokArr[1].text(), "start");
        assertEquals(splitTokArr[1].start(), 1);
        assertEquals(splitTokArr[1].end(), 6);
        
        Token t4 = new Token("end-", 0, 4);
        splitTokArr = split.apply(t4).toArray(Token[]::new);
        assertEquals(2, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "end");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 3);
        assertEquals(splitTokArr[1].text(), "-");
        assertEquals(splitTokArr[1].start(), 3);
        assertEquals(splitTokArr[1].end(), 4);

        Token t5 = new Token("--start", 0, 6);
        splitTokArr = split.apply(t5).toArray(Token[]::new);
        assertEquals(3, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "-");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 1);
        assertEquals(splitTokArr[1].text(), "-");
        assertEquals(splitTokArr[1].start(), 1);
        assertEquals(splitTokArr[1].end(), 2);
        assertEquals(splitTokArr[2].text(), "start");
        assertEquals(splitTokArr[2].start(), 2);
        assertEquals(splitTokArr[2].end(), 7);
        
        Token t6 = new Token("end--", 0, 5);
        splitTokArr = split.apply(t6).toArray(Token[]::new);
        assertEquals(3, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "end");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 3);
        assertEquals(splitTokArr[1].text(), "-");
        assertEquals(splitTokArr[1].start(), 3);
        assertEquals(splitTokArr[1].end(), 4);
        assertEquals(splitTokArr[2].text(), "-");
        assertEquals(splitTokArr[2].start(), 4);
        assertEquals(splitTokArr[2].end(), 5);
        
        Token t7 = new Token("-", 0, 1);
        splitTokArr = split.apply(t7).toArray(Token[]::new);
        assertEquals(1, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "-");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 1);
        
        Token t8 = new Token("none", 0, 4);
        splitTokArr = split.apply(t8).toArray(Token[]::new);
        assertEquals(1, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "none");
        assertEquals(splitTokArr[0].start(), 0);
        assertEquals(splitTokArr[0].end(), 4);
    }   
    
    @Test
    public void testSlashSplit() {
        Token t = new Token("either/or", 0, 9);
        TokenSplitter split = TokenSplitters.slashes();
        Token[] splitTokArr = split.apply(t).toArray(Token[]::new);
        assertEquals(3, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "either");
        assertEquals(splitTokArr[1].text(), "/");
        assertEquals(splitTokArr[2].text(), "or");
        
        Token t2 = new Token("try\\forward\\slashes", 0, 9);
        splitTokArr = split.apply(t2).toArray(Token[]::new);
        assertEquals(5, splitTokArr.length);
        assertEquals(splitTokArr[0].text(), "try");
        assertEquals(splitTokArr[1].text(), "\\");
        assertEquals(splitTokArr[2].text(), "forward");
        assertEquals(splitTokArr[3].text(), "\\");
        assertEquals(splitTokArr[4].text(), "slashes");
    }
    
}
