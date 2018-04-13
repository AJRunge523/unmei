package com.arunge.nlp.tokenization;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arunge.nlp.api.Token;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class StopwordsFilter implements TokenFilter{

    private Set<String> stopwords;
    
    public StopwordsFilter(File stopwordsFile) throws IOException {
        List<String> lines = Files.readLines(stopwordsFile, Charsets.UTF_8);
        this.stopwords = new HashSet<>();
        stopwords.addAll(lines);
    }
    
    @Override
    public boolean test(Token t) {
        return !stopwords.contains(t.text().toLowerCase());
    }

}
