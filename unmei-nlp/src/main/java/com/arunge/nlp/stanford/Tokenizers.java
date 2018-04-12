package com.arunge.nlp.stanford;

import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.tokenization.TokenFilters;

public class Tokenizers {

    public static Tokenizer getDefault() {
        return new StanfordTokenizer();
    }
    
    public static Tokenizer getDefaultFiltered() {
        return new FilteredTokenizer(getDefault(), TokenFilters.getDefaultFilters());
    }
    
}
