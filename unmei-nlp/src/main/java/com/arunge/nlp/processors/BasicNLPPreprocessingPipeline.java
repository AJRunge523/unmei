package com.arunge.nlp.processors;

import java.util.ArrayList;
import java.util.Collection;

import com.arunge.nlp.api.Lemmatizer;
import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.api.POSTagger;
import com.arunge.nlp.api.Stemmer;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.TextDocument;
import com.arunge.nlp.tokenization.TokenFilters;
import com.arunge.nlp.tokenization.TokenSplitter;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;

public class BasicNLPPreprocessingPipeline implements NLPPreprocessingPipeline {

    protected POSTagger tagger;
    protected Lemmatizer lemma;
    protected Stemmer stemmer;
    
    protected Collection<TokenSplitter> tokenSplitters;
    protected Collection<TokenFilter> tokenFilters;
    
    public BasicNLPPreprocessingPipeline() { 
        this.tokenSplitters = new ArrayList<>();
        this.tokenFilters = new ArrayList<>();
    }
    
    public BasicNLPPreprocessingPipeline withPOSTagger(POSTagger tagger) { 
        this.tagger = tagger;
        return this;
    }
    
    public BasicNLPPreprocessingPipeline withLemmatizer(Lemmatizer lemma) {
        this.lemma = lemma;
        return this;
    }

    public BasicNLPPreprocessingPipeline withStemmer(Stemmer stemmer) {
        this.stemmer = stemmer;
        return this;
    }
    
    public BasicNLPPreprocessingPipeline withTokenSplitters(Collection<TokenSplitter> splitters) {
        this.tokenSplitters = splitters;
        return this;
    }
    
    public BasicNLPPreprocessingPipeline withTokenFilters(Collection<TokenFilter> filters) { 
        this.tokenFilters = filters;
        return this;
    }
    
    @Override
    public AnnotatedTextDocument apply(TextDocument doc) {
        return null;
    }

}
