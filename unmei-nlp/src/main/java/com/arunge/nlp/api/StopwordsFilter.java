package com.arunge.nlp.api;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.core.util.Charsets;

import com.arunge.nlp.api.TokenFilters.TokenFilter;
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