package com.arunge.nlp.stanford;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.arunge.nlp.api.Range;
import com.arunge.nlp.api.SentenceSplitter;
import com.arunge.nlp.api.Token;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class StanfordSentenceSplitter implements SentenceSplitter {

    @Override
    public List<Range> split(String text) {
        Reader reader = new StringReader(text);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        List<String> sentenceList = new ArrayList<String>();
        List<Range> sentences = new ArrayList<>();
        for (List<HasWord> sentence : dp) {
            CoreLabel first = (CoreLabel) sentence.get(0);
            CoreLabel last = (CoreLabel) sentence.get(sentence.size() - 1);
            //Stanford labels are inclusive end
            Range r = new Range(first.beginPosition(), last.endPosition() + 1);
            sentences.add(r);
            System.out.println(r);
            String sentenceString = SentenceUtils.listToOriginalTextString(sentence);
            sentenceList.add(sentenceString);
        }
        for (String sentence : sentenceList) {
            System.out.println(sentence);
        }
        return sentences;
    }
    

    @Override
    public List<Range> split(List<Token> tokens) {
        return null;
    }

}
