package com.arunge.nlp.stanford;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.PreprocessedTextField;
import com.arunge.nlp.text.TextDocument;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLPPreprocessingPipeline implements NLPPreprocessingPipeline {

    private StanfordCoreNLP pipeline;
    
    private boolean segmentAnn;
    
    public StanfordNLPPreprocessingPipeline(Annotator...annotators) {
        String annotatorList = "tokenize,ssplit";
        Arrays.sort(annotators);
        for(Annotator annotator : annotators) {
            switch(annotator) {
            case POS:
                annotatorList += ",pos";
                break;
            case LEMMA:
                annotatorList += ",lemma";
                break;
            case SEGMENT:
                segmentAnn = true;
                break;
            case NER:
                annotatorList += ",ner";
                break;
            case DEPPARSE:
                annotatorList += ",depparse";
                break;
            }
        }
        Properties props = new Properties();
        props.setProperty("annotators",  annotatorList);
        pipeline = new StanfordCoreNLP(props);
    }
    
    @Override
    public PreprocessedTextDocument apply(TextDocument doc) {
        PreprocessedTextDocument processed = new PreprocessedTextDocument(doc);
        for(String fieldName : doc.getFieldNames()) {
            PreprocessedTextField field = new PreprocessedTextField();
            String fieldText = doc.getTextField(fieldName);
            Annotation document = pipeline.process(fieldText);
            for(CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<AnnotatedToken> sentenceToks = new ArrayList<>();
                for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    AnnotatedToken token = new AnnotatedToken(label.word(), label.beginPosition(), label.endPosition());
                    String pos = sentence.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if(pos != null) {
                        token.addAnnotation(Annotator.POS, pos);
                    }
                    String lemma = label.lemma();
                    if(lemma != null) {
                        token.addAnnotation(Annotator.LEMMA, lemma);
                    }
                    if(segmentAnn) {
                        token.addAnnotation(Annotator.SEGMENT, fieldName);
                    }
                    sentenceToks.add(token);
                }
                field.addSentence(sentenceToks);
            }
            processed.addTextField(fieldName, field);
        }
        return processed;
    }

    
}
