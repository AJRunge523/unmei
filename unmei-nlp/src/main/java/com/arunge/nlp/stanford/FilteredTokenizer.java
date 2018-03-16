package com.arunge.nlp.stanford;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.TokenFilters.TokenFilter;
import com.arunge.nlp.api.Tokenizer;

public class FilteredTokenizer implements Tokenizer {

    private Tokenizer tokenizer;
    private List<TokenFilter> filters;
    
    public FilteredTokenizer(Tokenizer tokenizer, List<TokenFilter> filters) {
        this.tokenizer = tokenizer;
        this.filters = filters;
    }
    
    @Override
    public Stream<Token> tokenize(String text) {
        Stream<Token> tokens = tokenizer.tokenize(text);
        for(TokenFilter filter : filters) {
            tokens = tokens.filter(filter);
        }
        return tokens;
    }

    @Override
    public List<Token> tokenizeToList(String text) {
        return tokenize(text).collect(Collectors.toList());
    }

}
