package com.arunge.nlp.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.TokenFilters;
import com.arunge.nlp.api.TokenFilters.TokenFilter;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.google.common.collect.Streams;

/**
 * 
 *<p>A <code>TextDocumentTokenizer</code> tokenizes a document and then applies additional filtering and annotation
 *   to the resulting tokens. Documents containing multiple text fields may have the resulting tokens prefixed with
 *   the name of the field for distinguishing them in downstream applications.<p>
 *
 * @author Andrew Runge
 *
 */
public class TextDocumentTokenizer {

    private Tokenizer tokenizer;
    private List<TokenFilter> tokenFilters;
    private boolean splitFields;
    private FilterOp filterOp;
    
    public enum FilterOp {
        REMOVE,
        REPLACE
    }
    
    public TextDocumentTokenizer() {
        this.tokenizer = Tokenizers.getDefault();
        this.tokenFilters = TokenFilters.getDefaultFilters();
        this.splitFields = false;
        this.filterOp = FilterOp.REMOVE;
    }
    
    public TextDocumentTokenizer(Tokenizer tokenizer, List<TokenFilter> tokenFilters, boolean splitFields) {
        this.tokenizer = tokenizer;
        this.tokenFilters = tokenFilters;
        this.splitFields = splitFields;
        this.filterOp = FilterOp.REMOVE;
    }

    public void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void setTokenFilters(List<TokenFilter> tokenFilters) {
        this.tokenFilters = tokenFilters;
    }

    public void addTokenFilter(TokenFilter filter) {
        this.tokenFilters.add(filter);
    }
    
    public void setSplitFields(boolean splitFields) {
        this.splitFields = splitFields;
    }
    
    /**
     * Specify whether filtering operations should remove the filtered tokens, or replace them with the <NULL> symbol.
     * @param operation
     */
    public void setFilterOp(FilterOp operation) {
        this.filterOp = operation;
    }
    
    /**
     * Tokenizes the input doc, filters the resulting tokens, and annotates them with field names if necessary.
     * @param doc
     * @return
     */
    public Stream<Token> tokenize(TextDocument doc) {
        Stream<Token> tokens = Stream.empty();
        
        //Annotate tokens with their field name
        if(splitFields) {
            Map<String, String> fields = doc.getTextFields();
            for(Map.Entry<String, String> field : fields.entrySet()) {
                Stream<Token> fieldTokens = tokenizer.tokenize(field.getValue());
                for(TokenFilter filter : tokenFilters) {
                    fieldTokens = fieldTokens.filter(filter);
                }
                tokens = Streams.concat(tokens, fieldTokens.map(t -> new Token(field.getKey() + "_" + t.text(), t.end(), t.start())));
            }
        } else {
            tokens = tokenizer.tokenize(doc.getText());
            for(TokenFilter filter : tokenFilters) {
                tokens = tokens.filter(filter);
            }
        }
        return tokens;
    }
    
    public PreprocessedTextDocument filter(PreprocessedTextDocument doc) {
        for(String fieldName : doc.getTextFields().keySet()) {
            PreprocessedTextField field = doc.getField(fieldName);
            List<List<AnnotatedToken>> filteredSentences = new ArrayList<>();
            for(List<AnnotatedToken> sentence : field.getSentences()) {
                Stream<AnnotatedToken> tokens = sentence.stream();
                for(TokenFilter filter : tokenFilters) {
                    if(filterOp == FilterOp.REMOVE) {
                        tokens = tokens.filter(filter);
                    } else {
                        //Replace filtered tokens with empty strings
                        tokens = tokens.map(t -> {
                            if(filter.test(t)) {
                                AnnotatedToken rem = new AnnotatedToken("", t.start(), t.end());
                                return rem;
                            }
                            return t;
                        });
                    }
                }
//                if(splitFields) {
//                    tokens = tokens.map(t -> {
//                        t.addAnnotation(Annotator.SEGMENT, fieldName);
//                        return t;
//                    });
//                }
                filteredSentences.add(tokens.collect(Collectors.toList()));
            }
            PreprocessedTextField filteredField = new PreprocessedTextField(filteredSentences);
            doc.addTextField(fieldName, filteredField);
        }
        return doc;
    }
}
