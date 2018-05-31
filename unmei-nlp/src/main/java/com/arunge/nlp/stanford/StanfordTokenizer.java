package com.arunge.nlp.stanford;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.Tokenizer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class StanfordTokenizer implements Tokenizer {

    @Override
    public Stream<Token> tokenize(String text) {
        PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(text), new CoreLabelTokenFactory(), "asciiQuotes=true,normalizeParentheses=false,normalizeOtherBrackets=false");
        List<Token> tokens = new ArrayList<>();
        while (tokenizer.hasNext()) {
            CoreLabel sToken = tokenizer.next();
            Token token = new Token(sToken.word(), sToken.beginPosition(), sToken.endPosition());
            tokens.add(token);
        }
        return tokens.stream();
    }
    
    @Override
    public List<Token> tokenizeToList(String text) {
        return tokenize(text).collect(Collectors.toList());
    }

}
