package com.arunge.nlp.text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;

/**
 * 
 *<p>Simple POJO representing a tokenized and annotated text field.<p>
 *
 * @author Andrew Runge
 *
 */
public class AnnotatedTextField {

    private List<List<AnnotatedToken>> sentences;
    
    public AnnotatedTextField() {
        this.sentences = new ArrayList<>();
    }

    public AnnotatedTextField(List<List<AnnotatedToken>> sentences) {
        this.sentences = sentences;
    }
    
    public List<AnnotatedToken> getText() { 
        return getSentences().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
    }
    
    public List<List<AnnotatedToken>> getSentences() {
        return sentences;
    }

    public void setSentences(List<List<AnnotatedToken>> sentences) {
        this.sentences = sentences;
    }
    
    public void addSentence(List<AnnotatedToken> sentence) {
        this.sentences.add(sentence);
    }
    
    public int getLength() { 
        return sentences.stream().map(s -> s.size()).reduce(0, (a, b) -> a + b);
    }
    
    @Override
    public String toString() {
        return render();
    }
    
    public String render(Annotator...annotators) {
        StringBuilder sb = new StringBuilder();
        for(List<AnnotatedToken> sentence : sentences) { 
            String sent = sentence.stream().map(t -> t.render(annotators)).reduce((a, b) -> a + " " + b).get();
            sb.append(sent + "\n");
        }
        return sb.toString();
    }
}
