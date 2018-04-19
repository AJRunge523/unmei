package com.arunge.nlp.api;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.arunge.nlp.stanford.Tokenizers;

public class TestTokens {

    @Test
    public void testTokenToString() {
        
        String text = "This here, (my good Dr. Watson), is a test    of the tokenizer.";
        Tokenizer tok = Tokenizers.getDefault();
        List<Token> toks = tok.tokenizeToList(text);
        String reconstructed = Tokens.asString(toks);
        assertEquals(reconstructed, text);
        
    }
    
}
