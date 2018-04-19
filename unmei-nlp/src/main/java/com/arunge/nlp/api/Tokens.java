package com.arunge.nlp.api;

import java.util.Collection;

public class Tokens {

    public static String asString(Collection<? extends Token> tokens) {
        StringBuilder sb = new StringBuilder();
        int currentIndex = tokens.iterator().next().start();
        for(Token t : tokens) {
            while(t.start() > currentIndex) {
                sb.append(" ");
                currentIndex += 1;
            }
            sb.append(t.text());
            currentIndex = t.end();
        }
        return sb.toString();
    }
    
}
