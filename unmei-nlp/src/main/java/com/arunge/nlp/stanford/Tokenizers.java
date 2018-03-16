package com.arunge.nlp.stanford;

import com.arunge.nlp.api.TokenFilters;
import com.arunge.nlp.api.Tokenizer;

public class Tokenizers {

    public static Tokenizer getDefault() {
        return new StanfordTokenizer();
    }
    
    public static Tokenizer getDefaultFiltered() {
        return new FilteredTokenizer(getDefault(), TokenFilters.getDefaultFilters());
    }
    
}
