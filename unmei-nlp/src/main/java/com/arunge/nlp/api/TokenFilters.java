package com.arunge.nlp.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class TokenFilters {

    private static Logger LOG = LoggerFactory.getLogger(TokenFilters.class);
    
    private static StopwordsFilter DEFAULT_STOPWORDS;
    
    static {
        try {
            DEFAULT_STOPWORDS = new StopwordsFilter(new File("src/main/resources/stopwords.txt"));
//            CharSource source = Resources.asCharSource(TokenFilters.class.getResource("stopwords.txt"), Charsets.UTF_8);
//            List<String> lines = source.readLines();
        } catch (IOException e) {
            LOG.warn("Unable to load stopwords list");
        }
    }
    
    public interface TokenFilter extends Predicate<Token> {
    }
    
    //[0-9e\\s,\\-\\+\\.\\p{Punct}]*
    private static Pattern NUM_PATTERN = Pattern.compile(".*[0-9]+.*");
    private static Pattern PUNCTUATION = Pattern.compile("\\p{Punct}+");
    private static Pattern ASCII = Pattern.compile("[ -~]*");
    
    public static TokenFilter numberFilter(){ 
        return t -> !(NUM_PATTERN.matcher(t.text()).matches());
    }
    
    public static TokenFilter maxLength(int maxLength){ 
        return t -> t.text().length() <= maxLength;
    }
    
    public static TokenFilter stopwords() {
        return DEFAULT_STOPWORDS;
    }
    
    public static TokenFilter punctuation() { 
        return t -> !(PUNCTUATION.matcher(t.text()).matches());
    }
   
    public static TokenFilter ascii() { 
        return t -> ASCII.matcher(t.text()).matches();
    }
    
    public static List<TokenFilter> getDefaultFilters() {
        return Lists.newArrayList(maxLength(20), numberFilter(), stopwords(), punctuation(), ascii());
    }
}
