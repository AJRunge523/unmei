package com.arunge.nlp.stanford;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.arunge.nlp.api.SentenceSplitter;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.Tokenizer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.WordToSentenceProcessor;

public class StanfordSentenceSplitter implements SentenceSplitter {

    private WordToSentenceProcessor<CoreLabel> splitter;
    
    
    public StanfordSentenceSplitter() {
        this.splitter = new WordToSentenceProcessor<CoreLabel>();
    }
    
    @Override
    public List<List<Token>> split(String text) {
        Reader reader = new StringReader(text);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        List<List<Token>> sentences = new ArrayList<>();
        for (List<HasWord> sentence : dp) {
            List<Token> tokSentence = new ArrayList<>();
            for(int i = 0; i < sentence.size(); i++) {
                CoreLabel tok = (CoreLabel) sentence.get(i);
                tokSentence.add(new Token(tok.word(), tok.beginPosition(), tok.endPosition()));
            }
            sentences.add(tokSentence);
        }
        return sentences;
    }
    

    @Override
    public List<List<Token>> split(List<Token> tokens) {
        List<CoreLabel> labels = StanfordTypeConversion.toCoreLabels(tokens);
        List<List<CoreLabel>> sentences = splitter.process(labels);
        List<List<Token>> tokSentences = new ArrayList<>();
        for (List<CoreLabel> sentence : sentences) {
            List<Token> tokSentence = new ArrayList<>();
            for(int i = 0; i < sentence.size(); i++) {
                CoreLabel tok = (CoreLabel) sentence.get(i);
                tokSentence.add(new Token(tok.word(), tok.beginPosition(), tok.endPosition()));
            }
            tokSentences.add(tokSentence);
        }
        return tokSentences;
    }

    public static void main(String[] args) { 
        String test = "That is a dog. This is a cat.";
        Tokenizer tokenizer = Tokenizers.getDefault();
        List<Token> tokens = tokenizer.tokenizeToList(test);
        SentenceSplitter ss = new StanfordSentenceSplitter();
        List<List<Token>> sentences = ss.split(tokens);
        for(List<Token> tok : sentences) { 
            for(Token t : tok) {
                System.out.println(t);
            }
        }
    }
    
}
