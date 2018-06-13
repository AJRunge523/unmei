package com.arunge.nlp.text;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.TokenForms.TokenForm;

public class NGramIterator implements Iterator<String[]> {

    private TokenForm tokenFormExtractor;
    private int maxOrder;
    private LinkedList<String[]> ngramQueue; 
    private LinkedList<String> prevWords;
    private boolean crossSentences;
    
    private Iterator<AnnotatedTextField> fieldIter;
    private Iterator<List<AnnotatedToken>> sentIter;
    private Iterator<AnnotatedToken> tokenIter;
    
    /**
     * Creates an NGramIterator that iterates through the text of a document, creating ngrams from all fields
     * in the document without creating ngrams that cross sentences.
     * @param doc The document to iterate over
     * @param tokenFormExtractor Extractor for the forms of each token.
     * @param maxOrder Maximum ngram order to iterate over
     */
    public NGramIterator(AnnotatedTextDocument doc, TokenForm tokenFormExtractor, int maxOrder) { 
        this(doc, tokenFormExtractor, maxOrder, false);
    }
    
    /**
     * Creates an NGramIterator that iterates through the text of a document, creating ngrams from all fields
     * in the document. Ngrams do not cross text field boundaries, but may cross sentence boundaries using the
     * crossSentences parameter.
     * @param doc The document to iterate over
     * @param tokenFormExtractor Extractor for the forms of each token.
     * @param maxOrder Maximum ngram order to iterate over
     * @param crossSentences Whether to create ngrams that cross sentence boundaries.
     */
    public NGramIterator(AnnotatedTextDocument doc, TokenForm tokenFormExtractor, int maxOrder, boolean crossSentences) { 
        this.tokenFormExtractor = tokenFormExtractor;
        this.maxOrder = maxOrder;
        this.ngramQueue = new LinkedList<>();
        this.prevWords = new LinkedList<>();
        this.fieldIter = doc.getTextFields().values().iterator();
        this.crossSentences = crossSentences;
    }
    
    @Override
    public boolean hasNext() {
        if(ngramQueue.size() == 0) { 
            loadNext();
        }
        return ngramQueue.size() != 0;
    }

    @Override
    public String[] next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        return ngramQueue.poll();
    }
    
    private void loadNext() {
        while(tokenIter == null || !tokenIter.hasNext()) {
            while((sentIter == null || !sentIter.hasNext()) && fieldIter.hasNext()) {
                sentIter = fieldIter.next().getSentences().iterator();
            }
            if(sentIter == null || !sentIter.hasNext()) {
                return;
            }
            if(!crossSentences) { 
                prevWords.clear();
            }
            tokenIter = sentIter.next().iterator();
        } 
        AnnotatedToken nextTok = tokenIter.next();
        String form = tokenFormExtractor.apply(nextTok);
        if(prevWords.size() == maxOrder) { 
            prevWords.poll();
        }
        prevWords.add(form);
        String[] maxGram = prevWords.toArray(new String[prevWords.size()]);
        for(int i = maxGram.length - 1; i >= 0; i--) { 
            ngramQueue.add(Arrays.copyOfRange(maxGram, i, maxGram.length));
        }
    }
    
}
