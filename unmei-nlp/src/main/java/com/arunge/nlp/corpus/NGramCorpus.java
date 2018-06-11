package com.arunge.nlp.corpus;

import com.arunge.nlp.vocab.NGramIndexer;

public abstract class NGramCorpus extends Corpus {

    private static final long serialVersionUID = 1L;

    public abstract NGramIndexer getNgramIndexer();
    
}
