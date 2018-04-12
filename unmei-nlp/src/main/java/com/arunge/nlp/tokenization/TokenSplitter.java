package com.arunge.nlp.tokenization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.arunge.nlp.api.Token;

public class TokenSplitter implements Function<Token, Stream<Token>>{

    private Pattern pattern;
    
    public TokenSplitter(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }
    
    @Override
    public Stream<Token> apply(Token t) {
        Matcher m = pattern.matcher(t.text());
        Collection<Token> tokens = new ArrayList<>();
        int prev = 0;
        while(m.find()) {
            int start = m.start();
            int end = m.end();
            if(start > prev) {
                Token leftSplit = new Token(t.text().substring(prev, start), prev, start);
                tokens.add(leftSplit);
            }
            Token patternSplit = new Token(t.text().substring(start, end), start, end);
            prev = end;
            tokens.add(patternSplit);
        }
        if(prev != t.text().length()) {
            tokens.add(new Token(t.text().substring(prev, t.text().length()), prev, t.text().length()));
        }
        if(tokens.isEmpty()) {
            tokens.add(t);
        }
        return tokens.stream();
    }
    
}
