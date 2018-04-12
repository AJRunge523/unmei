package com.arunge.nlp.stanford;

import java.util.List;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.POSTagger;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.text.TextDocument;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;

public class StanfordPOSTagger implements POSTagger {

    private POSTaggerAnnotator tagger;
    
    public StanfordPOSTagger() { 
        tagger = new POSTaggerAnnotator(); 
    }
    
    public void tag(List<AnnotatedToken> sentence) {
        Annotation annotation = StanfordTypeConversion.toAnnotation(sentence);
        tagger.annotate(annotation);
        List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
        for(int i = 0; i < tokens.size(); i++) {
            sentence.get(i).addAnnotation(Annotator.POS, tokens.get(i).tag());
        }
    }
    
    public void tag(AnnotatedTextDocument doc) {
        for(String fieldName : doc.getFieldNames()) { 
            tag(doc.getField(fieldName));
        }
    }
    
    public void tag(AnnotatedTextField field) {
        for(List<AnnotatedToken> sentence : field.getSentences()) {
            tag(sentence);
        }
    }
    
    public static void main(String[] args) {
        StanfordNLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
        TextDocument doc = new TextDocument("a", "This is a document designed to test how to correctly create core labels.");
        AnnotatedTextDocument procDoc = pipeline.apply(doc);
        StanfordPOSTagger tagger = new StanfordPOSTagger();
        tagger.tag(procDoc.getDefaultField().getSentences().get(0));
        System.out.println(procDoc.render(Annotator.POS));
    }
    
}
